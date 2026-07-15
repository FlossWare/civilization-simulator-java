package org.flossware.civilization;

import org.flossware.civilization.model.Event;
import org.flossware.civilization.model.PopulationState;
import org.flossware.civilization.module.ModuleResult;
import org.flossware.civilization.module.PopulationModule;
import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PopulationModule covering growth, plague, carrying capacity, and milestones.
 */
class PopulationModuleTest {

    @Test
    void populationGrowsWithGoodResources() {
        PopulationState state = new PopulationState(500_000, 0.03, 0.02, 50_000_000, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PopulationState> result = PopulationModule.tick(state, 1.0, 0.0, random);

        assertTrue(result.state().population() > 500_000,
            "Population should grow when resources are good and no plague is active");
    }

    @Test
    void populationDeclinesWithActivePlague() {
        // Start with high population near carrying capacity and plague already active.
        // Near capacity, births are low while plague triples death rate.
        PopulationState state = new PopulationState(49_000_000, 0.03, 0.02, 50_000_000, true);

        boolean foundDecline = false;
        for (int seed = 0; seed < 100; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<PopulationState> result = PopulationModule.tick(state, 1.0, 0.0, random);

            if (result.state().plagueActive() && result.state().population() < 49_000_000) {
                foundDecline = true;
                break;
            }
        }

        assertTrue(foundDecline,
            "Population should decline when plague is active and population is near carrying capacity");
    }

    @Test
    void plagueOutbreakTriggeredAtFullProbability() {
        PopulationState state = new PopulationState(1_000_000, 0.03, 0.02, 50_000_000, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PopulationState> result = PopulationModule.tick(state, 1.0, 1.0, random);

        boolean hasPlagueEvent = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.PLAGUE);
        assertTrue(hasPlagueEvent,
            "Plague event should be generated when outbreak probability is 1.0");
    }

    @Test
    void plagueNeverOutbreaksAtZeroProbability() {
        PopulationState state = new PopulationState(1_000_000, 0.03, 0.02, 50_000_000, false);

        for (int seed = 0; seed < 50; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<PopulationState> result = PopulationModule.tick(state, 1.0, 0.0, random);

            boolean hasPlagueEvent = result.events().stream()
                .anyMatch(e -> e.type() == Event.EventType.PLAGUE);
            assertFalse(hasPlagueEvent,
                "No plague event should be generated when outbreak probability is 0.0");
        }
    }

    @Test
    void plagueRecoveryOccurs() {
        // Start with active plague; 20% chance of recovery per tick
        PopulationState state = new PopulationState(1_000_000, 0.03, 0.06, 50_000_000, true);

        boolean recovered = false;
        for (int seed = 0; seed < 100; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<PopulationState> result = PopulationModule.tick(state, 1.0, 0.0, random);

            if (!result.state().plagueActive()) {
                recovered = true;
                break;
            }
        }

        assertTrue(recovered,
            "Plague should eventually recover with 20% chance per tick");
    }

    @Test
    void carryingCapacityScalesWithResources() {
        PopulationState state = new PopulationState(1_000_000, 0.03, 0.02, 50_000_000, false);
        SplittableRandom random1 = new SplittableRandom(42);
        SplittableRandom random2 = new SplittableRandom(42);

        ModuleResult<PopulationState> highResource = PopulationModule.tick(state, 1.5, 0.0, random1);
        ModuleResult<PopulationState> lowResource = PopulationModule.tick(state, 0.5, 0.0, random2);

        // Carrying capacity = 50M * max(0.1, resourceAbundance)
        assertEquals(50_000_000 * 1.5, highResource.state().carryingCapacity(), 0.01,
            "Carrying capacity should be 75M with resourceAbundance=1.5");
        assertEquals(50_000_000 * 0.5, lowResource.state().carryingCapacity(), 0.01,
            "Carrying capacity should be 25M with resourceAbundance=0.5");
        assertTrue(highResource.state().carryingCapacity() > lowResource.state().carryingCapacity(),
            "Higher resources should yield higher carrying capacity");
    }

    @Test
    void carryingCapacityHasMinimumFloor() {
        PopulationState state = new PopulationState(1_000_000, 0.03, 0.02, 50_000_000, false);
        SplittableRandom random = new SplittableRandom(42);

        // Resource abundance of 0.0 should clamp to 0.1
        ModuleResult<PopulationState> result = PopulationModule.tick(state, 0.0, 0.0, random);

        assertEquals(50_000_000 * 0.1, result.state().carryingCapacity(), 0.01,
            "Carrying capacity should use floor of 0.1 for resource abundance");
    }

    @Test
    void populationFloorIsEnforced() {
        PopulationState state = new PopulationState(1000, 0.03, 0.02, 50_000_000, true);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PopulationState> result = PopulationModule.tick(state, 0.1, 0.0, random);

        assertTrue(result.state().population() >= 1000,
            "Population must never drop below the floor of 1000");
    }

    @Test
    void populationMilestoneEventAt10Million() {
        // Start just below 10M so growth crosses the threshold
        PopulationState state = new PopulationState(9_990_000, 0.03, 0.02, 50_000_000, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PopulationState> result = PopulationModule.tick(state, 1.0, 0.0, random);

        assertTrue(result.state().population() >= 10_000_000,
            "Population should cross 10M threshold");

        boolean hasMilestone = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.POPULATION_MILESTONE);
        assertTrue(hasMilestone,
            "Should generate POPULATION_MILESTONE event when crossing 10M");
    }

    @Test
    void populationDoesNotOverflowWithLargeValues() {
        // Start with a very large population to verify overflow protection (#37)
        long largePopulation = Long.MAX_VALUE / 4;
        PopulationState state = new PopulationState(largePopulation, 0.03, 0.02, 50_000_000, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PopulationState> result = PopulationModule.tick(state, 1.5, 0.0, random);

        assertTrue(result.state().population() > 0,
            "Population must remain positive (no overflow to negative)");
        assertTrue(result.state().population() <= Long.MAX_VALUE / 2,
            "Population must be clamped to Long.MAX_VALUE/2");
    }

    @Test
    void noMilestoneWhenAlreadyAbove10Million() {
        // Start above 10M -- no milestone should fire
        PopulationState state = new PopulationState(15_000_000, 0.03, 0.02, 50_000_000, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PopulationState> result = PopulationModule.tick(state, 1.0, 0.0, random);

        boolean hasMilestone = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.POPULATION_MILESTONE);
        assertFalse(hasMilestone,
            "No milestone event when population was already above 10M");
    }
}
