package org.flossware.civilization.scenarios;

import org.flossware.civilization.model.*;
import org.flossware.civilization.util.ScenarioBuilder;

import java.util.*;

/**
 * "Cradle of Civilization" - What if the Sumerian city-states unified and endured?
 */
public final class SumerianScenario {

    /**
     * Creates the Sumerian Cradle scenario.
     */
    public static Scenario create() {
        return new ScenarioBuilder()
            .withId("sumerian-cradle")
            .withName("Cradle of Civilization")
            .withDescription("What if the Sumerian city-states unified and endured?")
            .withTimeRange(-3500, -539)
            .withInitialState(createInitialState())
            .withTechTree(StandardTechTree.create())
            .withWorldConstraints(createWorldConstraints())
            .withSimulationRules(createSimulationRules())
            .build();
    }

    private static CivilizationState createInitialState() {
        PopulationState population = new PopulationState(
            200_000,
            0.035,
            0.025,
            500_000,
            false
        );

        List<TradeRoute> tradeRoutes = List.of(
            new TradeRoute("Uruk", "Ur", List.of("grain", "textiles"), 200.0, 0.1),
            new TradeRoute("Uruk", "Elam", List.of("copper", "stone"), 150.0, 0.15)
        );

        EconomyState economy = new EconomyState(
            5_000_000,
            10_000,
            8_000,
            50_000,
            0.05,
            10_000,
            tradeRoutes
        );

        Set<String> initialTechs = new HashSet<>(Arrays.asList(
            "agriculture", "copper_smelting", "writing"
        ));

        TechnologyState technology = new TechnologyState(
            initialTechs,
            new HashMap<>(),
            0.05,
            1
        );

        PoliticsState politics = new PoliticsState(
            0.5,
            "City-States",
            40,
            0.0,
            false,
            false
        );

        MilitaryState military = new MilitaryState(
            20_000,
            5_000,
            0.8,
            0.5,
            false,
            null
        );

        ClimateState climate = new ClimateState(
            0.0,
            0.4,
            0.2,
            0.0
        );

        Map<String, Double> religionShares = new HashMap<>();
        religionShares.put("Sumerian Pantheon", 0.95);
        religionShares.put("Local Cults", 0.05);

        ReligionState religion = new ReligionState(
            religionShares,
            0.95,
            0.19,
            0.03
        );

        return new CivilizationState(
            "sumer",
            "Sumerian Civilization",
            Arrays.asList("Mesopotamia", "Elam", "Assyria"),
            "Uruk",
            -3500,
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
            0.4,
            0.55,
            0.4,
            0.02,
            0.7
        );
    }

    private static SimulationRules createSimulationRules() {
        return new SimulationRules(
            "adaptive",
            true,
            23456L,
            50,
            8
        );
    }
}
