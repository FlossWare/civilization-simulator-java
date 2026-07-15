package org.flossware.civilization.scenarios;

import org.flossware.civilization.model.*;
import org.flossware.civilization.util.ScenarioBuilder;

import java.util.*;

/**
 * "Inca Dominion" - What if the Inca Empire resisted European contact and expanded?
 */
public final class IncaScenario {

    /**
     * Creates the Inca Dominion scenario.
     */
    public static Scenario create() {
        return new ScenarioBuilder()
            .withId("inca-dominion")
            .withName("Inca Dominion")
            .withDescription("What if the Inca Empire resisted European contact and expanded?")
            .withTimeRange(1200, 1600)
            .withInitialState(createInitialState())
            .withTechTree(StandardTechTree.create())
            .withWorldConstraints(createWorldConstraints())
            .withSimulationRules(createSimulationRules())
            .build();
    }

    private static CivilizationState createInitialState() {
        PopulationState population = new PopulationState(
            1_000_000,
            0.030,
            0.020,
            5_000_000,
            false
        );

        List<TradeRoute> tradeRoutes = List.of(
            new TradeRoute("Cusco", "Quito", List.of("gold", "textiles"), 250.0, 0.1),
            new TradeRoute("Cusco", "Titicaca", List.of("grain", "wool"), 180.0, 0.08)
        );

        EconomyState economy = new EconomyState(
            8_000_000,
            16_000,
            12_000,
            200_000,
            0.06,
            16_000,
            tradeRoutes
        );

        Set<String> initialTechs = new HashSet<>(Arrays.asList(
            "agriculture", "copper_smelting", "mathematics"
        ));

        TechnologyState technology = new TechnologyState(
            initialTechs,
            new HashMap<>(),
            0.03,
            1
        );

        PoliticsState politics = new PoliticsState(
            0.70,
            "Imperial Theocracy",
            35,
            0.0,
            false,
            false
        );

        MilitaryState military = new MilitaryState(
            50_000,
            2_000,
            0.7,
            0.6,
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
        religionShares.put("Inca Religion", 0.90);
        religionShares.put("Local Animism", 0.10);

        ReligionState religion = new ReligionState(
            religionShares,
            0.90,
            0.18,
            0.03
        );

        return new CivilizationState(
            "inca",
            "Inca Empire",
            Arrays.asList("Peru", "Bolivia", "Ecuador", "Chile"),
            "Cusco",
            1200,
            population,
            economy,
            technology,
            politics,
            military,
            climate,
            religion,
            new RandomEventState(Integer.MIN_VALUE)
        );
    }

    private static WorldConstraints createWorldConstraints() {
        return new WorldConstraints(
            0.6,
            0.40,
            0.35,
            0.012,
            0.65,
            1.0
        );
    }

    private static SimulationRules createSimulationRules() {
        return new SimulationRules(
            "adaptive",
            true,
            67890L,
            50,
            8
        );
    }
}
