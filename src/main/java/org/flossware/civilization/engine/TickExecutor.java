package org.flossware.civilization.engine;

import org.flossware.civilization.model.*;
import org.flossware.civilization.module.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Executes a single simulation tick by running all modules in sequence.
 *
 * Execution order (strictly sequential):
 * 1. ClimateModule (Climate, resources, disasters)
 * 2. PopulationModule (Births, deaths, plague)
 * 3. EconomyModule (Production, consumption, trade)
 * 4. TechnologyModule (Research, diffusion, unlocks)
 * 5. ReligionModule (Spread, conversion, schisms)
 * 6. PoliticsModule (Stability, succession, rebellion)
 * 7. MilitaryModule (War resolution, territorial changes)
 */
final class TickExecutor {

    private final Scenario scenario;
    private final TechGraph techGraph;
    private final List<SimulationModule> modules;

    TickExecutor(Scenario scenario, TechGraph techGraph) {
        this.scenario = Objects.requireNonNull(scenario);
        this.techGraph = Objects.requireNonNull(techGraph);
        this.modules = List.of(
            new ClimateModule(),
            new PopulationModule(),
            new EconomyModule(),
            new TechnologyModule(techGraph),
            new ReligionModule(),
            new PoliticsModule(),
            new MilitaryModule()
        );
    }

    /**
     * Executes one simulation tick with all modules.
     */
    TickResult executeTick(
        CivilizationState state,
        int year,
        TickType tickType,
        long yearSeed
    ) {
        List<Event> events = new ArrayList<>();

        for (SimulationModule module : modules) {
            TickContext context = new TickContext(state, scenario, techGraph, tickType, yearSeed);
            ModuleResult<?> result = module.tick(context);
            state = module.applyResult(state, result);
            events.addAll(result.events());
        }

        // Update year
        state = state.withYear(year);

        return new TickResult(state, events);
    }
}
