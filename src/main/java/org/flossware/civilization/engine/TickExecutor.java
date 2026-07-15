package org.flossware.civilization.engine;

import org.flossware.civilization.model.*;
import org.flossware.civilization.module.*;
import org.flossware.civilization.util.SeedManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Executes a single simulation tick by running all modules in sequence.
 *
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
final class TickExecutor {

    private final Scenario scenario;
    private final TechGraph techGraph;

    TickExecutor(Scenario scenario, TechGraph techGraph) {
        this.scenario = Objects.requireNonNull(scenario);
        this.techGraph = Objects.requireNonNull(techGraph);
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

        // 1. Climate Module
        var climateRandom = SeedManager.getModuleRandom(yearSeed, "climate");
        var climateResult = ClimateModule.tick(
            state.climate(),
            scenario.worldConstraints().climateVolatility(),
            climateRandom
        );
        state = state.withClimate(climateResult.state());
        events.addAll(climateResult.events());

        // 2. Migration Module (placeholder)
        // Would go here

        // 3. Population Module
        var popRandom = SeedManager.getModuleRandom(yearSeed, "population");
        var popResult = PopulationModule.tick(
            state.population(),
            state.climate().getResourceAbundance(),
            scenario.worldConstraints().plagueProbability(),
            popRandom
        );
        state = state.withPopulation(popResult.state());
        events.addAll(popResult.events());

        // 4. Economy Module
        var econRandom = SeedManager.getModuleRandom(yearSeed, "economy");
        var econResult = EconomyModule.tick(
            state.economy(),
            state.climate().getResourceAbundance(),
            state.technology().unlockedTechs(),
            state.population().population(),
            econRandom
        );
        state = state.withEconomy(econResult.state());
        events.addAll(econResult.events());

        // 5. Technology Module
        var techRandom = SeedManager.getModuleRandom(yearSeed, "technology");
        var techResult = TechnologyModule.tick(
            state.technology(),
            techGraph,
            calculateTradeConnectivity(state),
            state.population().population(),
            techRandom
        );
        state = state.withTechnology(techResult.state());
        events.addAll(techResult.events());

        // 6. Religion Module
        var relRandom = SeedManager.getModuleRandom(yearSeed, "religion");
        var relResult = ReligionModule.tick(
            state.religion(),
            calculateTradeConnectivity(state),
            relRandom
        );
        state = state.withReligion(relResult.state());
        events.addAll(relResult.events());

        // 7. Politics Module
        var polRandom = SeedManager.getModuleRandom(yearSeed, "politics");
        var polResult = PoliticsModule.tick(
            state.politics(),
            calculateEconomicHealth(state),
            state.religion().religiousUnity(),
            state.military().atWar(),
            tickType.getYears(),
            polRandom
        );
        state = state.withPolitics(polResult.state());
        events.addAll(polResult.events());

        // 8. Military Module
        var milRandom = SeedManager.getModuleRandom(yearSeed, "military");
        var milResult = MilitaryModule.tick(
            state.military(),
            state.economy().wealth(),
            state.technology().unlockedTechs(),
            milRandom
        );
        state = state.withMilitary(milResult.state());
        events.addAll(milResult.events());

        // Update year
        state = state.withYear(year);

        return new TickResult(state, events);
    }

    double calculateTradeConnectivity(CivilizationState state) {
        // Simplified: based on number of trade routes
        return Math.min(1.0, state.economy().tradeRoutes().size() * 0.2);
    }

    double calculateEconomicHealth(CivilizationState state) {
        // Simplified: based on GDP growth
        double wealthPerCapita = state.economy().wealth() / Math.max(1, state.population().population());
        return Math.min(1.0, wealthPerCapita / 1000.0);
    }
}
