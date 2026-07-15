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

    // Random walk scaling factors
    private static final double TEMPERATURE_VOLATILITY_SCALE = 2.0;
    private static final double STORM_VOLATILITY_SCALE = 0.5;

    // Climate value ranges
    private static final double MIN_TEMPERATURE_ANOMALY = -10.0;
    private static final double MAX_TEMPERATURE_ANOMALY = 10.0;
    private static final double MIN_DROUGHT_INDEX = 0.0;
    private static final double MAX_DROUGHT_INDEX = 1.0;
    private static final double MIN_STORM_FREQUENCY = 0.0;
    private static final double MAX_STORM_FREQUENCY = 5.0;

    // Sea level rise coefficient per degree of temperature anomaly
    private static final double SEA_LEVEL_RISE_COEFFICIENT = 0.5;

    // Severity determination thresholds
    private static final double EXTREME_STORM_THRESHOLD = 3.5;
    private static final double EXTREME_DROUGHT_THRESHOLD = 0.985;
    private static final double MAJOR_STORM_THRESHOLD = 3.2;
    private static final double MAJOR_DROUGHT_THRESHOLD = 0.98;

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
            + (random.nextDouble() - 0.5) * TEMPERATURE_VOLATILITY_SCALE * volatility;

        double newDroughtIndex = current.droughtIndex()
            + (random.nextDouble() - 0.5) * volatility;

        double newStormFrequency = current.stormFrequency()
            + (random.nextDouble() - 0.5) * volatility * STORM_VOLATILITY_SCALE;

        // Clamp values to valid ranges BEFORE using them in calculations
        newTemperatureAnomaly = clamp(newTemperatureAnomaly, MIN_TEMPERATURE_ANOMALY, MAX_TEMPERATURE_ANOMALY);
        newDroughtIndex = clamp(newDroughtIndex, MIN_DROUGHT_INDEX, MAX_DROUGHT_INDEX);
        newStormFrequency = clamp(newStormFrequency, MIN_STORM_FREQUENCY, MAX_STORM_FREQUENCY);

        // Sea level rise based on CLAMPED temperature anomaly
        double newSeaLevelRise = current.seaLevelRise_mm()
            + Math.max(0, newTemperatureAnomaly * SEA_LEVEL_RISE_COEFFICIENT);
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
        boolean extremeStorms = stormFrequency > EXTREME_STORM_THRESHOLD;
        boolean extremeDrought = droughtIndex > EXTREME_DROUGHT_THRESHOLD;

        if (extremeStorms && extremeDrought) {
            return EventSeverity.CRITICAL;
        } else if (stormFrequency > STORM_FREQUENCY_THRESHOLD && droughtIndex > DROUGHT_INDEX_THRESHOLD) {
            return EventSeverity.CRITICAL;
        } else if (stormFrequency > MAJOR_STORM_THRESHOLD || droughtIndex > MAJOR_DROUGHT_THRESHOLD) {
            return EventSeverity.MAJOR;
        } else {
            return EventSeverity.MAJOR;
        }
    }
}
