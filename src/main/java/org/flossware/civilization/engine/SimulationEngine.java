package org.flossware.civilization.engine;

import org.flossware.civilization.model.*;
import org.flossware.civilization.util.SeedManager;

import java.util.*;

/**
 * Main simulation engine with variable tick loop.
 *
 * Pure function: (scenario, seed) → (finalState, eventLog)
 * Execution order (strictly sequential):
 * 1. EnvironmentModule (Climate, resources, disasters)
 * 2. MigrationModule (Population movement - placeholder for now)
 * 3. PopulationModule (Births, deaths, plague)
 * 4. EconomyModule (Production, consumption, trade)
 * 5. TechnologyModule (Research, diffusion, unlocks)
 * 6. ReligionModule (Spread, conversion, schisms)
 * 7. PoliticsModule (Stability, succession, rebellion)
 * 8. MilitaryModule (War resolution, territorial changes)
 */
public final class SimulationEngine {

    private final Scenario scenario;
    private final SeedManager seedManager;
    private final TickExecutor tickExecutor;

    public SimulationEngine(Scenario scenario, long baseSeed) {
        this.scenario = Objects.requireNonNull(scenario);
        this.seedManager = new SeedManager(baseSeed);
        TechGraph techGraph = new TechGraph(scenario.techTree());
        this.tickExecutor = new TickExecutor(scenario, techGraph);
    }

    /**
     * Runs the complete simulation from start year to end year.
     *
     * @param runIndex Monte Carlo run index for seed isolation
     * @return Final state and complete event log
     */
    public SimulationResult run(int runIndex) {
        CivilizationState state = scenario.initialState();
        List<Event> allEvents = new ArrayList<>();

        int currentYear = scenario.startYear();

        while (currentYear <= scenario.endYear()) {
            TickType tickType = TickType.determineTickType(
                scenario.worldConstraints().climateVolatility(),
                state.politics().stability()
            );

            long yearSeed = seedManager.getYearSeed(runIndex, currentYear, tickType.name());

            // Execute all modules in sequence
            TickResult tickResult = tickExecutor.executeTick(state, currentYear, tickType, yearSeed);
            state = tickResult.state();

            // Add year to all events
            final int eventYear = currentYear;
            final String civId = state.id();
            List<Event> yearEvents = tickResult.events().stream()
                .map(e -> new Event(eventYear, civId, e.type(), e.severity(), e.description(), e.data()))
                .toList();
            allEvents.addAll(yearEvents);

            currentYear += (int) Math.ceil(tickType.getYears());
        }

        return new SimulationResult(state, allEvents);
    }

    /**
     * Runs the complete simulation with periodic snapshots for charting.
     *
     * @param runIndex         Monte Carlo run index for seed isolation
     * @param snapshotInterval How many years between snapshots
     * @return Final state, event log, and periodic snapshots
     */
    public SimulationResult runWithSnapshots(int runIndex, int snapshotInterval) {
        CivilizationState state = scenario.initialState();
        List<Event> allEvents = new ArrayList<>();
        List<SimulationSnapshot> snapshots = new ArrayList<>();

        int currentYear = scenario.startYear();
        int nextSnapshot = currentYear;

        while (currentYear <= scenario.endYear()) {
            TickType tickType = TickType.determineTickType(
                scenario.worldConstraints().climateVolatility(),
                state.politics().stability()
            );

            long yearSeed = seedManager.getYearSeed(runIndex, currentYear, tickType.name());
            TickResult tickResult = tickExecutor.executeTick(state, currentYear, tickType, yearSeed);
            state = tickResult.state();

            final int eventYear = currentYear;
            final String civId = state.id();
            List<Event> yearEvents = tickResult.events().stream()
                .map(e -> new Event(eventYear, civId, e.type(), e.severity(), e.description(), e.data()))
                .toList();
            allEvents.addAll(yearEvents);

            if (currentYear >= nextSnapshot) {
                snapshots.add(new SimulationSnapshot(
                    currentYear,
                    state.population().population(),
                    state.economy().wealth(),
                    state.economy().gdp(),
                    state.technology().unlockedTechs().size(),
                    state.politics().stability(),
                    state.technology().literacyRate()
                ));
                nextSnapshot += snapshotInterval;
            }

            currentYear += (int) Math.ceil(tickType.getYears());
        }

        snapshots.add(new SimulationSnapshot(
            state.year(),
            state.population().population(),
            state.economy().wealth(),
            state.economy().gdp(),
            state.technology().unlockedTechs().size(),
            state.politics().stability(),
            state.technology().literacyRate()
        ));

        return new SimulationResult(state, allEvents, snapshots);
    }
}
