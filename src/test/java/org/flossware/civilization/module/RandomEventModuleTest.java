package org.flossware.civilization.module;

import org.flossware.civilization.model.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class RandomEventModuleTest {

    private CivilizationState createTestState(int year, int lastEventYear) {
        PopulationState population = new PopulationState(5_000_000, 0.025, 0.015, 100_000_000, false);

        EconomyState economy = new EconomyState(
            50_000_000, 100_000, 80_000, 1_000_000, 0.1, 100_000,
            List.of(new TradeRoute("Rome", "Alexandria", List.of("grain"), 500.0, 0.1)));

        TechnologyState technology = new TechnologyState(
            new HashSet<>(Arrays.asList("agriculture", "mining", "iron_working")),
            new HashMap<>(), 0.15, 2);

        PoliticsState politics = new PoliticsState(0.6, "Republic", 45, 0.0, false, false);
        MilitaryState military = new MilitaryState(100_000, 20_000, 1.0, 0.7, false, null);
        ClimateState climate = new ClimateState(0.0, 0.3, 0.5, 0.0);

        Map<String, Double> shares = new HashMap<>();
        shares.put("Roman Polytheism", 0.8);
        shares.put("Christianity", 0.2);
        ReligionState religion = new ReligionState(shares, 0.7, 0.14, 0.05);

        RandomEventState randomEvent = new RandomEventState(lastEventYear);

        return new CivilizationState(
            "rome", "Roman Empire", List.of("Italia", "Gaul"), "Rome", year,
            population, economy, technology, politics, military, climate, religion, randomEvent);
    }

    @Test
    void noEventDuringCooldown() {
        CivilizationState state = createTestState(100, 90); // only 10 years since last event
        ModuleResult<CivilizationState> result = RandomEventModule.tick(state, 1.0, new SplittableRandom(42));
        assertTrue(result.events().isEmpty(), "No events should fire during cooldown");
    }

    @Test
    void noEventWhenFrequencyIsZero() {
        CivilizationState state = createTestState(100, Integer.MIN_VALUE);
        ModuleResult<CivilizationState> result = RandomEventModule.tick(state, 0.0, new SplittableRandom(42));
        assertTrue(result.events().isEmpty(), "No events when frequency is 0");
    }

    @Test
    void eventsAreDeterministicWithSameSeed() {
        CivilizationState state = createTestState(500, Integer.MIN_VALUE);
        ModuleResult<CivilizationState> result1 = RandomEventModule.tick(state, 2.0, new SplittableRandom(12345));
        ModuleResult<CivilizationState> result2 = RandomEventModule.tick(state, 2.0, new SplittableRandom(12345));

        assertEquals(result1.events().size(), result2.events().size(), "Same seed should produce same events");
        if (!result1.events().isEmpty()) {
            assertEquals(result1.events().get(0).type(), result2.events().get(0).type(),
                "Same seed should produce same event type");
            assertEquals(result1.state().population().population(),
                result2.state().population().population(),
                "Same seed should produce same population");
        }
    }

    @Test
    void eventUpdatesLastEventYear() {
        // Try many seeds until we find one that triggers an event with high frequency
        for (long seed = 0; seed < 1000; seed++) {
            CivilizationState state = createTestState(500, Integer.MIN_VALUE);
            ModuleResult<CivilizationState> result = RandomEventModule.tick(state, 2.0, new SplittableRandom(seed));
            if (!result.events().isEmpty()) {
                assertEquals(500, result.state().randomEvent().lastEventYear(),
                    "lastEventYear should be updated to current year");
                return;
            }
        }
        fail("Should have found at least one seed that triggers an event with 2.0 frequency");
    }

    @Test
    void populationNeverGoesNegative() {
        // Create state with very small population
        PopulationState tinyPop = new PopulationState(500, 0.025, 0.015, 1000, false);
        CivilizationState state = createTestState(500, Integer.MIN_VALUE);
        state = new CivilizationState(
            state.id(), state.name(), state.coreRegions(), state.capital(), state.year(),
            tinyPop, state.economy(), state.technology(), state.politics(),
            state.military(), state.climate(), state.religion(), state.randomEvent());

        for (long seed = 0; seed < 1000; seed++) {
            ModuleResult<CivilizationState> result = RandomEventModule.tick(state, 2.0, new SplittableRandom(seed));
            assertTrue(result.state().population().population() > 0,
                "Population must stay positive for seed " + seed);
        }
    }

    @Test
    void goldenAgeRequiresPeaceAndStability() {
        // State with low stability and at war - should never get golden age
        PoliticsState lowStab = new PoliticsState(0.2, "Republic", 45, 0.5, true, false);
        MilitaryState atWar = new MilitaryState(100_000, 20_000, 1.0, 0.7, true, "Rival");
        CivilizationState state = createTestState(500, Integer.MIN_VALUE);
        state = new CivilizationState(
            state.id(), state.name(), state.coreRegions(), state.capital(), state.year(),
            state.population(), state.economy(), state.technology(), lowStab,
            atWar, state.climate(), state.religion(), state.randomEvent());

        for (long seed = 0; seed < 1000; seed++) {
            ModuleResult<CivilizationState> result = RandomEventModule.tick(state, 2.0, new SplittableRandom(seed));
            for (Event e : result.events()) {
                assertNotEquals(Event.EventType.GOLDEN_AGE, e.type(),
                    "Golden age should not fire when at war with low stability");
            }
        }
    }

    @Test
    void pandemicRequiresMinimumPopulation() {
        // State with tiny population - should never get pandemic
        PopulationState smallPop = new PopulationState(500_000, 0.025, 0.015, 1_000_000, false);
        CivilizationState state = createTestState(500, Integer.MIN_VALUE);
        state = new CivilizationState(
            state.id(), state.name(), state.coreRegions(), state.capital(), state.year(),
            smallPop, state.economy(), state.technology(), state.politics(),
            state.military(), state.climate(), state.religion(), state.randomEvent());

        for (long seed = 0; seed < 1000; seed++) {
            ModuleResult<CivilizationState> result = RandomEventModule.tick(state, 2.0, new SplittableRandom(seed));
            for (Event e : result.events()) {
                assertNotEquals(Event.EventType.GREAT_PANDEMIC, e.type(),
                    "Pandemic should not fire with population under 1M");
            }
        }
    }

    @Test
    void atMostOneEventPerTick() {
        CivilizationState state = createTestState(500, Integer.MIN_VALUE);
        for (long seed = 0; seed < 1000; seed++) {
            ModuleResult<CivilizationState> result = RandomEventModule.tick(state, 2.0, new SplittableRandom(seed));
            assertTrue(result.events().size() <= 1,
                "At most one event per tick, seed=" + seed);
        }
    }
}
