package org.flossware.civilization.module;

import org.flossware.civilization.model.PoliticsState;
import org.flossware.civilization.model.Event;
import org.flossware.civilization.model.Event.EventSeverity;
import org.flossware.civilization.model.Event.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Politics module handling stability, rebellions, and succession crises.
 *
 * Pure function: (state, params, random) → (newState, events)
 */
public final class PoliticsModule {

    private static final double REBELLION_THRESHOLD = 0.2;
    private static final double REBELLION_PROBABILITY = 0.3;
    private static final int SUCCESSION_AGE_THRESHOLD = 70;
    private static final double SUCCESSION_PROBABILITY = 0.2;
    private static final int RULER_AGE_INCREMENT = 1;
    private static final double RULER_DEATH_BASE_PROBABILITY = 0.02;
    private static final int RULER_DEATH_AGE_THRESHOLD = 50;
    private static final double RULER_DEATH_MAX_PROBABILITY = 0.5;
    private static final double SUCCESSION_RESOLVE_PROBABILITY = 0.2;
    private static final int NEW_RULER_MIN_AGE = 25;
    private static final int NEW_RULER_MAX_AGE = 45;
    private static final double STABILITY_BLEND_RATE = 0.15;
    private static final double WAR_EXHAUSTION_GAIN_RATE = 0.05;
    private static final double WAR_EXHAUSTION_DECAY_RATE = 0.1;
    private static final double REBELLION_VOLATILITY = 0.3;
    private static final double NORMAL_VOLATILITY = 0.1;
    private static final double ECONOMIC_HEALTH_WEIGHT = 0.4;
    private static final double RELIGIOUS_UNITY_WEIGHT = 0.3;
    private static final double BASE_STABILITY_FLOOR = 0.3;
    private static final double WAR_EXHAUSTION_STABILITY_PENALTY = 0.1;
    private static final double REBELLION_RECOVERY_THRESHOLD = 0.5;
    private static final double RULER_DEATH_AGE_SCALING_FACTOR = 20.0;

    /**
     * Ticks politics forward by one time step.
     *
     * @param current Current politics state
     * @param economicHealth Economic health factor (typically 0.0 to 1.0)
     * @param religiousUnity Religious unity factor (typically 0.0 to 1.0)
     * @param atWar Whether civilization is currently at war
     * @param yearsPerTick Duration of this tick in years (for adaptive time steps)
     * @param random Module-specific random generator
     * @return Updated state and events
     */
    public static ModuleResult<PoliticsState> tick(
        PoliticsState current,
        double economicHealth,
        double religiousUnity,
        boolean atWar,
        double yearsPerTick,
        SplittableRandom random
    ) {
        List<Event> events = new ArrayList<>();
        int year = 0; // Will be set by engine

        // Calculate base stability from current state
        double baseStability = current.stability();

        // Accumulate war exhaustion if at war, decay if at peace
        // Rates are per-year, scaled by tick duration
        double warExhaustion = current.warExhaustion();
        if (atWar) {
            warExhaustion = Math.min(1.0, warExhaustion + WAR_EXHAUSTION_GAIN_RATE * yearsPerTick);
        } else {
            warExhaustion = Math.max(0.0, warExhaustion - WAR_EXHAUSTION_DECAY_RATE * yearsPerTick);
        }

        // Calculate volatility component (random factor)
        double volatility = current.inRebellion() ? REBELLION_VOLATILITY : NORMAL_VOLATILITY;

        // Calculate target stability from current conditions (mean-reverting)
        double targetStability = economicHealth * ECONOMIC_HEALTH_WEIGHT + religiousUnity * RELIGIOUS_UNITY_WEIGHT + BASE_STABILITY_FLOOR;
        // Blend toward target (mean-reverting)
        double newStability = baseStability + (targetStability - baseStability) * STABILITY_BLEND_RATE;
        // Apply penalties
        newStability -= warExhaustion * WAR_EXHAUSTION_STABILITY_PENALTY;
        newStability -= random.nextDouble() * volatility;
        // Clamp stability to [0, 1]
        newStability = Math.max(0.0, Math.min(1.0, newStability));

        // Age the ruler by tick duration
        int newRulerAge = current.rulerAge() + (int)Math.ceil(yearsPerTick);

        // Check for rebellion
        boolean rebellion = current.inRebellion();
        if (newStability < REBELLION_THRESHOLD && random.nextDouble() < REBELLION_PROBABILITY) {
            if (!rebellion) {
                rebellion = true;
                events.add(new Event(year, "", EventType.REBELLION, EventSeverity.CRITICAL,
                    "Popular rebellion breaks out due to low stability!", newStability));
            }
        } else if (rebellion && newStability > REBELLION_RECOVERY_THRESHOLD) {
            // Rebellion ends if stability recovers
            rebellion = false;
        }

        // Check for ruler death (age > 50: increasing probability)
        boolean successionCrisis = current.inSuccessionCrisis();
        boolean rulerDied = false;
        if (newRulerAge > RULER_DEATH_AGE_THRESHOLD) {
            double deathProbability = RULER_DEATH_BASE_PROBABILITY
                * (newRulerAge - RULER_DEATH_AGE_THRESHOLD) / RULER_DEATH_AGE_SCALING_FACTOR;
            deathProbability = Math.min(RULER_DEATH_MAX_PROBABILITY, deathProbability);
            if (random.nextDouble() < deathProbability) {
                rulerDied = true;
                events.add(new Event(year, "", EventType.RULER_DEATH, EventSeverity.CRITICAL,
                    "The ruler has died at age " + newRulerAge + "!", newRulerAge));
                if (!successionCrisis) {
                    successionCrisis = true;
                    events.add(new Event(year, "", EventType.SUCCESSION_CRISIS, EventSeverity.MAJOR,
                        "Succession crisis triggered by the ruler's death!", newRulerAge));
                }
            }
        }

        // Check for succession crisis from old age (existing mechanic)
        if (!rulerDied && newRulerAge > SUCCESSION_AGE_THRESHOLD
                && random.nextDouble() < SUCCESSION_PROBABILITY) {
            if (!successionCrisis) {
                successionCrisis = true;
                events.add(new Event(year, "", EventType.SUCCESSION_CRISIS, EventSeverity.MAJOR,
                    "Succession crisis emerges as the ruler ages!", newRulerAge));
            }
        }

        // Resolve succession crisis: 20% chance per year, new ruler age 25-45
        if (successionCrisis && random.nextDouble() < SUCCESSION_RESOLVE_PROBABILITY) {
            successionCrisis = false;
            newRulerAge = NEW_RULER_MIN_AGE
                + random.nextInt(NEW_RULER_MAX_AGE - NEW_RULER_MIN_AGE + 1);
            events.add(new Event(year, "", EventType.SUCCESSION_RESOLVED, EventSeverity.MAJOR,
                "Succession crisis resolved! New ruler ascends at age " + newRulerAge + ".",
                newRulerAge));
        }

        // Build new state
        PoliticsState newState = new PoliticsState(
            newStability,
            current.government(),
            newRulerAge,
            warExhaustion,
            rebellion,
            successionCrisis
        );

        return new ModuleResult<>(newState, events);
    }
}
