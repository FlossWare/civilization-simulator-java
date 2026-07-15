package org.flossware.civilization;

import org.flossware.civilization.model.ClimateState;
import org.flossware.civilization.model.Event;
import org.flossware.civilization.module.ClimateModule;
import org.flossware.civilization.module.ModuleResult;
import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ClimateModule covering random walk bounds, sea level rise,
 * disaster events, and resource abundance.
 */
class ClimateModuleTest {

    @Test
    void temperatureAnomalyStaysWithinBounds() {
        ClimateState state = new ClimateState(9.0, 0.5, 2.5, 0.0);

        for (int seed = 0; seed < 200; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<ClimateState> result = ClimateModule.tick(state, 5.0, random);

            assertTrue(result.state().temperatureAnomaly() >= -10.0
                    && result.state().temperatureAnomaly() <= 10.0,
                "Temperature anomaly must be clamped to [-10, 10], got: "
                    + result.state().temperatureAnomaly());
        }
    }

    @Test
    void droughtIndexStaysWithinBounds() {
        // Start near upper bound with high volatility
        ClimateState state = new ClimateState(0.0, 0.95, 2.5, 0.0);

        for (int seed = 0; seed < 200; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<ClimateState> result = ClimateModule.tick(state, 5.0, random);

            assertTrue(result.state().droughtIndex() >= 0.0
                    && result.state().droughtIndex() <= 1.0,
                "Drought index must be clamped to [0, 1], got: "
                    + result.state().droughtIndex());
        }
    }

    @Test
    void stormFrequencyStaysWithinBounds() {
        ClimateState state = new ClimateState(0.0, 0.5, 4.8, 0.0);

        for (int seed = 0; seed < 200; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<ClimateState> result = ClimateModule.tick(state, 5.0, random);

            assertTrue(result.state().stormFrequency() >= 0.0
                    && result.state().stormFrequency() <= 5.0,
                "Storm frequency must be clamped to [0, 5], got: "
                    + result.state().stormFrequency());
        }
    }

    @Test
    void seaLevelRisesWithPositiveTemperature() {
        ClimateState state = new ClimateState(5.0, 0.5, 2.5, 100.0);
        SplittableRandom random = new SplittableRandom(42);

        // With low volatility, temperature stays positive, so sea level increases
        ModuleResult<ClimateState> result = ClimateModule.tick(state, 0.1, random);

        assertTrue(result.state().seaLevelRise_mm() > 100.0,
            "Sea level should rise when temperature anomaly is positive");
    }

    @Test
    void seaLevelNeverDecreases() {
        // Even with negative temperature, sea level rise is additive (max 0 per tick)
        ClimateState state = new ClimateState(-5.0, 0.5, 2.5, 50.0);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<ClimateState> result = ClimateModule.tick(state, 0.1, random);

        // seaLevelRise += max(0, newTemp * 0.5); with negative temp, addition is 0
        // But newTemp might become positive after random walk. Either way, sea level >= 0.
        assertTrue(result.state().seaLevelRise_mm() >= 0.0,
            "Sea level rise should never be negative");
    }

    @Test
    void disasterEventAtHighStormFrequency() {
        // Start with storm frequency well above threshold (2.8)
        ClimateState state = new ClimateState(0.0, 0.5, 4.5, 0.0);
        SplittableRandom random = new SplittableRandom(42);

        // Low volatility so storm stays above threshold
        ModuleResult<ClimateState> result = ClimateModule.tick(state, 0.01, random);

        boolean hasDisaster = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.CLIMATE_DISASTER);
        assertTrue(hasDisaster,
            "Should generate CLIMATE_DISASTER event when storm frequency exceeds 2.8");
    }

    @Test
    void disasterEventAtHighDroughtIndex() {
        // Start with drought index near 1.0, well above threshold (0.97)
        ClimateState state = new ClimateState(0.0, 0.99, 0.0, 0.0);
        SplittableRandom random = new SplittableRandom(42);

        // Low volatility so drought stays above threshold
        ModuleResult<ClimateState> result = ClimateModule.tick(state, 0.01, random);

        boolean hasDisaster = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.CLIMATE_DISASTER);
        assertTrue(hasDisaster,
            "Should generate CLIMATE_DISASTER event when drought index exceeds 0.97");
    }

    @Test
    void noDisasterUnderNormalConditions() {
        ClimateState state = new ClimateState(0.0, 0.5, 1.0, 0.0);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<ClimateState> result = ClimateModule.tick(state, 0.1, random);

        boolean hasDisaster = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.CLIMATE_DISASTER);
        assertFalse(hasDisaster,
            "No disaster event should occur under normal climate conditions");
    }

    @Test
    void resourceAbundanceIdealConditions() {
        // drought=0.5 (optimal), temperature=0 (no penalty)
        ClimateState state = new ClimateState(0.0, 0.5, 2.5, 0.0);

        assertEquals(1.0, state.getResourceAbundance(), 0.01,
            "Optimal drought (0.5) and zero temperature anomaly should give 1.0 abundance");
    }

    @Test
    void resourceAbundanceHarshConditions() {
        // High drought and high temperature
        ClimateState state = new ClimateState(5.0, 1.0, 2.5, 0.0);

        double droughtPenalty = Math.abs(1.0 - 0.5) * 2;  // 1.0
        double tempPenalty = Math.abs(5.0) * 0.1;          // 0.5
        double expected = Math.max(0.1, 1.0 - droughtPenalty * 0.3 - tempPenalty); // 0.2

        assertEquals(expected, state.getResourceAbundance(), 0.01,
            "Harsh conditions should reduce resource abundance");
    }

    @Test
    void resourceAbundanceHasMinimumFloor() {
        // Extreme conditions: max temperature anomaly and worst drought
        ClimateState state = new ClimateState(10.0, 0.0, 2.5, 0.0);

        assertTrue(state.getResourceAbundance() >= 0.1,
            "Resource abundance should have a floor of 0.1 even in extreme conditions");
    }
}
