package org.flossware.civilization.scenarios;

import org.flossware.civilization.model.*;
import org.flossware.civilization.util.ScenarioBuilder;

import java.util.*;

/**
 * "Ming Dynastic Glory" - What if the Ming Dynasty never closed its borders?
 */
public final class MingDynastyScenario {

    /**
     * Creates the Ming Dynasty scenario.
     */
    public static Scenario create() {
        return new ScenarioBuilder()
            .withId("ming-dynasty")
            .withName("Ming Dynastic Glory")
            .withDescription("What if the Ming Dynasty never closed its borders?")
            .withTimeRange(1368, 1800)
            .withInitialState(createInitialState())
            .withTechTree(StandardTechTree.create())
            .withWorldConstraints(createWorldConstraints())
            .withSimulationRules(createSimulationRules())
            .build();
    }

    private static CivilizationState createInitialState() {
        PopulationState population = new PopulationState(
            60_000_000,
            0.025,
            0.018,
            150_000_000,
            false
        );

        List<TradeRoute> tradeRoutes = List.of(
            new TradeRoute("Beijing", "Malacca", List.of("silk", "porcelain"), 800.0, 0.08),
            new TradeRoute("Beijing", "Japan", List.of("silver", "copper"), 500.0, 0.1),
            new TradeRoute("Beijing", "India", List.of("spices", "cotton"), 600.0, 0.1)
        );

        EconomyState economy = new EconomyState(
            200_000_000,
            400_000,
            350_000,
            10_000_000,
            0.12,
            400_000,
            tradeRoutes
        );

        Set<String> initialTechs = new HashSet<>(Arrays.asList(
            "agriculture", "mining", "iron_working", "magnetism", "coal_mining",
            "mathematics", "metallurgy_advanced", "writing"
        ));

        TechnologyState technology = new TechnologyState(
            initialTechs,
            new HashMap<>(),
            0.12,
            5
        );

        PoliticsState politics = new PoliticsState(
            0.65,
            "Imperial Bureaucracy",
            40,
            0.0,
            false,
            false
        );

        MilitaryState military = new MilitaryState(
            1_000_000,
            200_000,
            1.0,
            0.7,
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
        religionShares.put("Confucianism", 0.50);
        religionShares.put("Buddhism", 0.30);
        religionShares.put("Taoism", 0.15);
        religionShares.put("Islam", 0.05);

        ReligionState religion = new ReligionState(
            religionShares,
            0.50,
            0.10,
            0.04
        );

        return new CivilizationState(
            "ming",
            "Ming Dynasty",
            Arrays.asList("Zhili", "Jiangnan", "Sichuan", "Guangdong", "Fujian", "Yunnan"),
            "Beijing",
            1368,
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
            0.55,
            0.35,
            0.35,
            0.015,
            0.75,
            1.0
        );
    }

    private static SimulationRules createSimulationRules() {
        return new SimulationRules(
            "adaptive",
            true,
            45678L,
            50,
            8
        );
    }
}
