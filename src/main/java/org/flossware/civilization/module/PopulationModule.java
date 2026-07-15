package org.flossware.civilization.module;

import org.flossware.civilization.engine.TickContext;
import org.flossware.civilization.model.CivilizationState;
import org.flossware.civilization.model.PopulationState;
import org.flossware.civilization.model.Event;
import org.flossware.civilization.model.Event.EventSeverity;
import org.flossware.civilization.model.Event.EventType;
import org.flossware.civilization.util.SeedManager;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Population dynamics module with logistic growth and carrying capacity.
 *
 * Pure function: (state, params, random) → (newState, events)
 */
public final class PopulationModule implements SimulationModule {

    private static final double BASE_BIRTH_RATE = 0.03;
    private static final double BASE_DEATH_RATE = 0.02;
    private static final double PLAGUE_DEATH_MULTIPLIER = 3.0;
    private static final double PLAGUE_END_PROBABILITY = 0.2;
    private static final long MINIMUM_POPULATION = 1000;
    private static final long POPULATION_MILESTONE_THRESHOLD = 10_000_000;
    private static final double BASE_CARRYING_CAPACITY = 50_000_000;
    private static final double MIN_RESOURCE_ABUNDANCE = 0.1;

    @Override
    public String moduleName() {
        return "population";
    }

    @Override
    public ModuleResult<?> tick(TickContext context) {
        var random = SeedManager.getModuleRandom(context.yearSeed(), moduleName());
        return tick(
            context.state().population(),
            context.state().climate().getResourceAbundance(),
            context.scenario().worldConstraints().plagueProbability(),
            random
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public CivilizationState applyResult(CivilizationState state, ModuleResult<?> result) {
        return state.withPopulation(((ModuleResult<PopulationState>) result).state());
    }

    /**
     * Ticks population forward by one time step.
     *
     * @param current Current population state
     * @param resourceAbundance From climate/environment (0.0 to 1.5+)
     * @param plagueProbability Probability of plague outbreak this tick
     * @param random Module-specific random generator
     * @return Updated state and events
     */
    public static ModuleResult<PopulationState> tick(
        PopulationState current,
        double resourceAbundance,
        double plagueProbability,
        SplittableRandom random
    ) {
        List<Event> events = new ArrayList<>();
        int year = 0; // Will be set by engine

        // Check for plague
        boolean plagueActive = current.plagueActive() || random.nextDouble() < plagueProbability;
        if (plagueActive && !current.plagueActive()) {
            events.add(new Event(year, "", EventType.PLAGUE, EventSeverity.CRITICAL,
                "Plague outbreak!", null));
        }

        // Plague ends after some time
        if (plagueActive && random.nextDouble() < PLAGUE_END_PROBABILITY) {
            plagueActive = false;
        }

        // Calculate carrying capacity
        double carryingCapacity = calculateCarryingCapacity(resourceAbundance);

        // Logistic growth
        double growthRate = logisticGrowth(current.population(), carryingCapacity);
        double births = current.population() * BASE_BIRTH_RATE * growthRate;

        // Deaths
        double mortalityRate = plagueActive ? BASE_DEATH_RATE * PLAGUE_DEATH_MULTIPLIER : BASE_DEATH_RATE;
        double deaths = current.population() * mortalityRate;

        // Apply delta
        long newPopulation = Math.max(MINIMUM_POPULATION, current.population() + (long)(births - deaths));

        // Guard against long overflow in extended simulations (#37)
        newPopulation = Math.min(newPopulation, Long.MAX_VALUE / 2);

        // Population milestones
        if (newPopulation >= POPULATION_MILESTONE_THRESHOLD && current.population() < POPULATION_MILESTONE_THRESHOLD) {
            events.add(new Event(year, "", EventType.POPULATION_MILESTONE, EventSeverity.MAJOR,
                "Population reaches 10 million!", newPopulation));
        }

        PopulationState newState = new PopulationState(
            newPopulation,
            BASE_BIRTH_RATE,
            mortalityRate,
            carryingCapacity,
            plagueActive
        );

        return new ModuleResult<>(newState, events);
    }

    /**
     * Logistic growth formula: r * (1 - P/K)
     * Returns growth rate multiplier between 0 and 1.
     */
    private static double logisticGrowth(long population, double carryingCapacity) {
        if (carryingCapacity <= 0) {
            return 0.0;
        }
        double ratio = population / carryingCapacity;
        return Math.max(0, 1.0 - ratio);
    }

    /**
     * Carrying capacity based on resource abundance.
     * Base capacity ~50M, scaled by resources.
     */
    private static double calculateCarryingCapacity(double resourceAbundance) {
        return BASE_CARRYING_CAPACITY * Math.max(MIN_RESOURCE_ABUNDANCE, resourceAbundance);
    }
}
