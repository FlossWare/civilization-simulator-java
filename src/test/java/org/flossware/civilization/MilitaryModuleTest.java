package org.flossware.civilization;

import org.flossware.civilization.model.Event;
import org.flossware.civilization.model.MilitaryState;
import org.flossware.civilization.module.MilitaryModule;
import org.flossware.civilization.module.ModuleResult;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MilitaryModule covering army sizing, war declaration,
 * war resolution, and technology advantages.
 */
class MilitaryModuleTest {

    @Test
    void armySizeLimitedByWealth() {
        // Army of 10,000 but only enough wealth for 5 soldiers
        MilitaryState state = new MilitaryState(10_000, 5_000, 1.0, 0.5, false, null);
        double wealth = 5_000; // 5000 / 1000 = max 5 soldiers

        ModuleResult<MilitaryState> result = MilitaryModule.tick(
            state, wealth, Set.of(), new SplittableRandom(42));

        long maxSoldiers = (long) (wealth / 1000.0);
        assertTrue(result.state().armySize() <= maxSoldiers,
            "Army size should be capped by wealth / 1000");
    }

    @Test
    void armyGrowsGraduallyWhenWealthPermits() {
        // Small army with large wealth allows growth
        MilitaryState state = new MilitaryState(1_000, 500, 1.0, 0.5, false, null);
        double wealth = 1_000_000_000; // Can sustain 1M soldiers

        // Find a seed where no war is declared
        for (int seed = 0; seed < 100; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<MilitaryState> result = MilitaryModule.tick(
                state, wealth, Set.of(), random);

            if (!result.state().atWar()) {
                assertTrue(result.state().armySize() > 1_000,
                    "Army should grow when wealth can sustain more soldiers");
                return;
            }
        }
        fail("Could not find a seed where war was not declared");
    }

    @Test
    void navySizeLimitedByHalfMaxSoldiers() {
        MilitaryState state = new MilitaryState(10_000, 10_000, 1.0, 0.5, false, null);
        double wealth = 10_000; // max 10 soldiers, max 5 navy

        ModuleResult<MilitaryState> result = MilitaryModule.tick(
            state, wealth, Set.of(), new SplittableRandom(42));

        long maxSoldiers = (long) (wealth / 1000.0);
        assertTrue(result.state().navySize() <= maxSoldiers / 2,
            "Navy size should be limited to half of max soldiers");
    }

    @Test
    void warDeclarationOccursWithSomeProbability() {
        MilitaryState state = new MilitaryState(10_000, 5_000, 1.0, 0.5, false, null);
        double wealth = 100_000_000;

        boolean warDeclared = false;
        for (int seed = 0; seed < 100; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<MilitaryState> result = MilitaryModule.tick(
                state, wealth, Set.of(), random);

            if (result.state().atWar()) {
                warDeclared = true;

                boolean hasEvent = result.events().stream()
                    .anyMatch(e -> e.type() == Event.EventType.WAR_DECLARED);
                assertTrue(hasEvent, "WAR_DECLARED event should be generated");

                assertNotNull(result.state().warOpponent(),
                    "War opponent should be set when war is declared");
                break;
            }
        }

        assertTrue(warDeclared,
            "War should be declared with ~5% probability over many ticks");
    }

    @Test
    void warResolvesInVictory() {
        // When at war, the defender is modeled as half attacker's strength.
        // With tech advantage >= 1.0 and random factor >= 0.8,
        // ratio = 2 * randomFactor >= 1.6 > 1.5, so victory is guaranteed.
        MilitaryState state = new MilitaryState(100_000, 50_000, 1.0, 0.5, true, "Barbarians");
        double wealth = 1_000_000_000;

        SplittableRandom random = new SplittableRandom(42);
        ModuleResult<MilitaryState> result = MilitaryModule.tick(
            state, wealth, Set.of(), random);

        assertFalse(result.state().atWar(),
            "War should end after resolution");
        assertNull(result.state().warOpponent(),
            "War opponent should be cleared after victory");

        boolean hasWarEnded = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.WAR_ENDED);
        assertTrue(hasWarEnded, "WAR_ENDED event should be generated on victory");
    }

    @Test
    void noWarWhenAlreadyAtWar() {
        MilitaryState state = new MilitaryState(100_000, 50_000, 1.0, 0.5, true, "Enemy");
        double wealth = 1_000_000_000;

        ModuleResult<MilitaryState> result = MilitaryModule.tick(
            state, wealth, Set.of(), new SplittableRandom(42));

        // Should not have WAR_DECLARED event (only WAR_ENDED)
        boolean hasDeclared = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.WAR_DECLARED);
        assertFalse(hasDeclared,
            "Should not declare new war while already at war");
    }

    @Test
    void techAdvantageWithNoMilitaryTechs() {
        MilitaryState state = new MilitaryState(10_000, 5_000, 1.0, 0.5, false, null);

        ModuleResult<MilitaryState> result = MilitaryModule.tick(
            state, 100_000_000, Set.of(), new SplittableRandom(42));

        assertEquals(1.0, result.state().techAdvantage(), 0.001,
            "Base tech advantage should be 1.0 with no military techs");
    }

    @Test
    void techAdvantageIncreasesWithMilitaryTechs() {
        MilitaryState state = new MilitaryState(10_000, 5_000, 1.0, 0.5, false, null);

        // bronze_working and iron_working are military techs, each adding 0.1
        ModuleResult<MilitaryState> result = MilitaryModule.tick(
            state, 100_000_000, Set.of("bronze_working", "iron_working"),
            new SplittableRandom(42));

        assertEquals(1.2, result.state().techAdvantage(), 0.001,
            "Two military techs should give 1.0 + 2*0.1 = 1.2 advantage");
    }

    @Test
    void nonMilitaryTechsDoNotAffectAdvantage() {
        MilitaryState state = new MilitaryState(10_000, 5_000, 1.0, 0.5, false, null);

        // agriculture and currency are NOT military techs
        ModuleResult<MilitaryState> result = MilitaryModule.tick(
            state, 100_000_000, Set.of("agriculture", "currency"),
            new SplittableRandom(42));

        assertEquals(1.0, result.state().techAdvantage(), 0.001,
            "Non-military techs should not affect military tech advantage");
    }

    @Test
    void allMilitaryTechsRecognized() {
        MilitaryState state = new MilitaryState(10_000, 5_000, 1.0, 0.5, false, null);

        Set<String> allMilitaryTechs = Set.of(
            "bronze_working", "iron_working", "horseback_riding", "archery",
            "siege_weapons", "gunpowder", "steel", "military_tactics",
            "naval_warfare", "fortification"
        );

        ModuleResult<MilitaryState> result = MilitaryModule.tick(
            state, 100_000_000, allMilitaryTechs, new SplittableRandom(42));

        // 10 military techs * 0.1 each = 1.0 bonus, total = 2.0
        assertEquals(2.0, result.state().techAdvantage(), 0.001,
            "All 10 military techs should give 1.0 + 10*0.1 = 2.0 advantage");
    }
}
