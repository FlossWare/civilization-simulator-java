package org.flossware.civilization;

import org.flossware.civilization.model.Event;
import org.flossware.civilization.model.PoliticsState;
import org.flossware.civilization.module.ModuleResult;
import org.flossware.civilization.module.PoliticsModule;
import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PoliticsModule covering stability, war exhaustion, rebellion,
 * succession crises, and ruler aging.
 */
class PoliticsModuleTest {

    @Test
    void stabilityStaysWithinBounds() {
        PoliticsState state = new PoliticsState(0.5, "Monarchy", 40, 0.0, false, false);

        for (int seed = 0; seed < 100; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<PoliticsState> result = PoliticsModule.tick(
                state, 0.5, 0.5, false, 1.0, random);

            assertTrue(result.state().stability() >= 0.0 && result.state().stability() <= 1.0,
                "Stability must be clamped to [0, 1], got: " + result.state().stability());
        }
    }

    @Test
    void stabilityDoesNotRatchetToOneUnderStress() {
        // With ongoing war, stability should not drift to 1.0
        PoliticsState current = new PoliticsState(0.5, "Monarchy", 40, 0.0, false, false);

        for (int i = 0; i < 30; i++) {
            SplittableRandom random = new SplittableRandom(i);
            ModuleResult<PoliticsState> result = PoliticsModule.tick(
                current, 0.3, 0.3, true, 1.0, random);
            current = result.state();
        }

        // After 30 ticks of war with moderate support, war exhaustion maxes out (1.0)
        // which counteracts the positive factors, keeping stability below 1.0
        assertTrue(current.stability() < 1.0,
            "Stability should not reach 1.0 with ongoing war and moderate inputs");
    }

    @Test
    void stabilityDecreasesUnderPoorConditions() {
        PoliticsState state = new PoliticsState(0.8, "Monarchy", 40, 0.8, false, false);

        PoliticsState current = state;
        for (int i = 0; i < 10; i++) {
            SplittableRandom random = new SplittableRandom(i);
            ModuleResult<PoliticsState> result = PoliticsModule.tick(
                current, 0.0, 0.0, true, 1.0, random);
            current = result.state();
        }

        assertTrue(current.stability() < 0.8,
            "Stability should decrease with zero economic/religious support and war");
    }

    @Test
    void warExhaustionAccumulatesDuringWar() {
        PoliticsState state = new PoliticsState(0.5, "Monarchy", 40, 0.0, false, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PoliticsState> result = PoliticsModule.tick(
            state, 0.5, 0.5, true, 1.0, random);

        assertEquals(0.05, result.state().warExhaustion(), 0.001,
            "War exhaustion should increase by 0.05 per year at war");
    }

    @Test
    void warExhaustionAccumulatesProportionalToTickSize() {
        PoliticsState state = new PoliticsState(0.5, "Monarchy", 40, 0.0, false, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PoliticsState> result = PoliticsModule.tick(
            state, 0.5, 0.5, true, 5.0, random);

        assertEquals(0.25, result.state().warExhaustion(), 0.001,
            "War exhaustion should scale with yearsPerTick");
    }

    @Test
    void warExhaustionDecaysDuringPeace() {
        PoliticsState state = new PoliticsState(0.5, "Monarchy", 40, 0.5, false, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PoliticsState> result = PoliticsModule.tick(
            state, 0.5, 0.5, false, 1.0, random);

        assertEquals(0.4, result.state().warExhaustion(), 0.001,
            "War exhaustion should decrease by 0.1 per year at peace");
    }

    @Test
    void warExhaustionClampedToZeroAndOne() {
        // Already at 0, should not go negative
        PoliticsState atZero = new PoliticsState(0.5, "Monarchy", 40, 0.0, false, false);
        SplittableRandom r1 = new SplittableRandom(42);
        ModuleResult<PoliticsState> peaceResult = PoliticsModule.tick(
            atZero, 0.5, 0.5, false, 1.0, r1);
        assertEquals(0.0, peaceResult.state().warExhaustion(), 0.001,
            "War exhaustion should not go below 0");

        // Near 1.0, should not exceed 1.0
        PoliticsState atMax = new PoliticsState(0.5, "Monarchy", 40, 0.98, false, false);
        SplittableRandom r2 = new SplittableRandom(42);
        ModuleResult<PoliticsState> warResult = PoliticsModule.tick(
            atMax, 0.5, 0.5, true, 1.0, r2);
        assertTrue(warResult.state().warExhaustion() <= 1.0,
            "War exhaustion should not exceed 1.0");
    }

    @Test
    void rebellionTriggersAtLowStability() {
        // Force stability to 0: start at 0, eco=0, rel=0, high warExhaustion
        PoliticsState state = new PoliticsState(0.0, "Monarchy", 40, 1.0, false, false);

        boolean rebellionTriggered = false;
        for (int seed = 0; seed < 100; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<PoliticsState> result = PoliticsModule.tick(
                state, 0.0, 0.0, false, 1.0, random);

            if (result.state().inRebellion()) {
                rebellionTriggered = true;

                boolean hasEvent = result.events().stream()
                    .anyMatch(e -> e.type() == Event.EventType.REBELLION);
                assertTrue(hasEvent, "Rebellion event should be generated");
                break;
            }
        }

        assertTrue(rebellionTriggered,
            "Rebellion should trigger when stability is below 0.2 (~30% probability)");
    }

    @Test
    void rebellionEndsWhenStabilityRecoversAboveHalf() {
        // Start in rebellion with high stability and strong positive inputs
        // Mean-reverting formula: target=1.0, blend=0.15, volatility=0.3
        // new = 0.9 + (1.0-0.9)*0.15 = 0.915, worst case: 0.915-0.3 = 0.615 > 0.5
        PoliticsState state = new PoliticsState(0.9, "Monarchy", 40, 0.0, true, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PoliticsState> result = PoliticsModule.tick(
            state, 1.0, 1.0, false, 1.0, random);

        assertFalse(result.state().inRebellion(),
            "Rebellion should end when stability recovers above 0.5");
    }

    @Test
    void noRebellionAtHighStability() {
        PoliticsState state = new PoliticsState(0.9, "Monarchy", 40, 0.0, false, false);

        for (int seed = 0; seed < 50; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<PoliticsState> result = PoliticsModule.tick(
                state, 0.8, 0.8, false, 1.0, random);

            assertFalse(result.state().inRebellion(),
                "Rebellion should not trigger at high stability");
        }
    }

    @Test
    void rulerAgeIncrementsPerTick() {
        PoliticsState state = new PoliticsState(0.5, "Monarchy", 40, 0.0, false, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PoliticsState> result = PoliticsModule.tick(
            state, 0.5, 0.5, false, 1.0, random);

        assertEquals(41, result.state().rulerAge(),
            "Ruler age should increment by ceil(yearsPerTick)");
    }

    @Test
    void rulerAgeIncrementScalesWithTickSize() {
        PoliticsState state = new PoliticsState(0.5, "Monarchy", 40, 0.0, false, false);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<PoliticsState> result = PoliticsModule.tick(
            state, 0.5, 0.5, false, 5.0, random);

        assertEquals(45, result.state().rulerAge(),
            "Ruler age should increment by ceil(yearsPerTick)=5 for 5-year tick");
    }

    @Test
    void successionCrisisWhenRulerIsOld() {
        // Ruler well past 70; high stability so rebellion doesn't interfere
        PoliticsState state = new PoliticsState(0.8, "Monarchy", 80, 0.0, false, false);

        boolean crisisTriggered = false;
        for (int seed = 0; seed < 100; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<PoliticsState> result = PoliticsModule.tick(
                state, 0.8, 0.8, false, 1.0, random);

            if (result.state().inSuccessionCrisis()) {
                crisisTriggered = true;

                boolean hasEvent = result.events().stream()
                    .anyMatch(e -> e.type() == Event.EventType.SUCCESSION_CRISIS);
                assertTrue(hasEvent, "Succession crisis event should be generated");
                break;
            }
        }

        assertTrue(crisisTriggered,
            "Succession crisis should trigger when ruler is >70 (~20% probability)");
    }

    @Test
    void noSuccessionCrisisForYoungRuler() {
        PoliticsState state = new PoliticsState(0.5, "Monarchy", 30, 0.0, false, false);

        for (int seed = 0; seed < 50; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<PoliticsState> result = PoliticsModule.tick(
                state, 0.5, 0.5, false, 1.0, random);

            assertFalse(result.state().inSuccessionCrisis(),
                "Succession crisis should not trigger for rulers under 70");
        }
    }
}
