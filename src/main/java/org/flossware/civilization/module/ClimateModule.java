package org.flossware.civilization.module;

import org.flossware.civilization.model.ClimateState;
import org.flossware.civilization.model.Event;
import org.flossware.civilization.model.Event.EventType;
import org.flossware.civilization.model.Event.EventSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Climate simulation module using random walk dynamics.
 * Implements tick function to evolve climate state over time.
 */
public final class ClimateModule {

    // Disaster threshold constants - calibrated for realistic frequencies
    // Thresholds set to trigger ~2-3% of years (roughly 1 disaster per 30-50 years)
    private static final double STORM_FREQUENCY_THRESHOLD = 2.8;  // Out of 0-5 range
    private static final double DROUGHT_INDEX_THRESHOLD = 0.97;   // Very severe drought only

    private ClimateModule() {
        // Utility class
    }

    /**
     * Advance climate state by one tick using random walk.
     *
     * @param current Current climate state
     * @param volatility Controls magnitude of random fluctuations
     * @param random Random number generator
     * @return ModuleResult containing new state and any generated events
     */
    public static ModuleResult<ClimateState> tick(
            ClimateState current,
            double volatility,
            SplittableRandom random) {

        // Random walk on each dimension
        double newTemperatureAnomaly = current.temperatureAnomaly()
            + (random.nextDouble() - 0.5) * 2 * volatility;

        double newDroughtIndex = current.droughtIndex()
            + (random.nextDouble() - 0.5) * volatility;

        double newStormFrequency = current.stormFrequency()
            + (random.nextDouble() - 0.5) * volatility * 0.5;

        // Clamp values to valid ranges BEFORE using them in calculations
        newTemperatureAnomaly = clamp(newTemperatureAnomaly, -10.0, 10.0);
        newDroughtIndex = clamp(newDroughtIndex, 0.0, 1.0);
        newStormFrequency = clamp(newStormFrequency, 0.0, 5.0);  // Max 5 major storms/year

        // Sea level rise based on CLAMPED temperature anomaly
        double newSeaLevelRise = current.seaLevelRise_mm()
            + Math.max(0, newTemperatureAnomaly * 0.5);
        newSeaLevelRise = Math.max(0.0, newSeaLevelRise);

        // Create new state
        ClimateState newState = new ClimateState(
            newTemperatureAnomaly,
            newDroughtIndex,
            newStormFrequency,
            newSeaLevelRise
        );

        // Generate events
        List<Event> events = new ArrayList<>();

        // Disaster thresholds calibrated for realistic frequency (~2-3% of years)
        if (newStormFrequency > STORM_FREQUENCY_THRESHOLD || newDroughtIndex > DROUGHT_INDEX_THRESHOLD) {
            String description = buildDisasterDescription(newStormFrequency, newDroughtIndex);
            EventSeverity severity = determineSeverity(newStormFrequency, newDroughtIndex);

            Event disasterEvent = new Event(
                0, // Year will be set by the simulation engine
                "GLOBAL", // Climate affects all civilizations
                EventType.CLIMATE_DISASTER,
                severity,
                description,
                newState
            );
            events.add(disasterEvent);
        }

        return new ModuleResult<>(newState, events);
    }

    /**
     * Clamp a value to a specified range.
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Build a descriptive message for climate disaster events.
     */
    private static String buildDisasterDescription(double stormFrequency, double droughtIndex) {
        if (stormFrequency > STORM_FREQUENCY_THRESHOLD && droughtIndex > DROUGHT_INDEX_THRESHOLD) {
            return String.format(
                "Extreme climate conditions: severe storms (%.2f) and severe drought (%.2f)",
                stormFrequency, droughtIndex
            );
        } else if (stormFrequency > STORM_FREQUENCY_THRESHOLD) {
            return String.format("Severe storm activity detected (frequency: %.2f)", stormFrequency);
        } else {
            return String.format("Severe drought conditions (index: %.2f)", droughtIndex);
        }
    }

    /**
     * Determine event severity based on climate conditions.
     */
    private static EventSeverity determineSeverity(double stormFrequency, double droughtIndex) {
        boolean extremeStorms = stormFrequency > 3.5;
        boolean extremeDrought = droughtIndex > 0.985;

        if (extremeStorms && extremeDrought) {
            return EventSeverity.CRITICAL;
        } else if (stormFrequency > STORM_FREQUENCY_THRESHOLD && droughtIndex > DROUGHT_INDEX_THRESHOLD) {
            return EventSeverity.CRITICAL;
        } else if (stormFrequency > 3.2 || droughtIndex > 0.98) {
            return EventSeverity.MAJOR;
        } else {
            return EventSeverity.MAJOR;
        }
    }
}
