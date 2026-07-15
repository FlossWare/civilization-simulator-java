package org.flossware.civilization.scenarios;

import org.flossware.civilization.model.*;
import org.flossware.civilization.util.ScenarioBuilder;

import java.util.*;

/**
 * "Holy Empire" - What if the Carolingian Empire held together after Charlemagne?
 */
public final class CarolingianScenario {

    /**
     * Creates the Carolingian Empire scenario.
     */
    public static Scenario create() {
        return new ScenarioBuilder()
            .withId("carolingian-empire")
            .withName("Holy Empire")
            .withDescription("What if the Carolingian Empire held together after Charlemagne?")
            .withTimeRange(800, 1500)
            .withInitialState(createInitialState())
            .withTechTree(StandardTechTree.create())
            .withWorldConstraints(createWorldConstraints())
            .withSimulationRules(createSimulationRules())
            .build();
    }

    private static CivilizationState createInitialState() {
        PopulationState population = new PopulationState(
            10_000_000,
            0.028,
            0.022,
            25_000_000,
            false
        );

        List<TradeRoute> tradeRoutes = List.of(
            new TradeRoute("Aachen", "Constantinople", List.of("silk", "spices"), 400.0, 0.12),
            new TradeRoute("Aachen", "Venice", List.of("glass", "cloth"), 300.0, 0.1)
        );

        EconomyState economy = new EconomyState(
            30_000_000,
            60_000,
            50_000,
            1_500_000,
            0.08,
            60_000,
            tradeRoutes
        );

        Set<String> initialTechs = new HashSet<>(Arrays.asList(
            "agriculture", "mining", "iron_working", "metallurgy_advanced", "mathematics", "writing"
        ));

        TechnologyState technology = new TechnologyState(
            initialTechs,
            new HashMap<>(),
            0.08,
            2
        );

        PoliticsState politics = new PoliticsState(
            0.55,
            "Feudal Monarchy",
            58,
            0.0,
            false,
            false
        );

        MilitaryState military = new MilitaryState(
            100_000,
            10_000,
            0.9,
            0.6,
            false,
            null
        );

        ClimateState climate = new ClimateState(
            0.0,
            0.5,
            0.3,
            0.0
        );

        Map<String, Double> religionShares = new HashMap<>();
        religionShares.put("Christianity", 0.90);
        religionShares.put("Norse Paganism", 0.08);
        religionShares.put("Judaism", 0.02);

        ReligionState religion = new ReligionState(
            religionShares,
            0.90,
            0.18,
            0.06
        );

        return new CivilizationState(
            "carolingian",
            "Carolingian Empire",
            Arrays.asList("Francia", "Bavaria", "Lombardy", "Saxony", "Burgundy"),
            "Aachen",
            800,
            population,
            economy,
            technology,
            politics,
            military,
            climate,
            religion
        );
    }

    private static WorldConstraints createWorldConstraints() {
        return new WorldConstraints(
            0.5,
            0.50,
            0.3,
            0.018,
            0.6
        );
    }

    private static SimulationRules createSimulationRules() {
        return new SimulationRules(
            "adaptive",
            true,
            34567L,
            50,
            8
        );
    }
}
