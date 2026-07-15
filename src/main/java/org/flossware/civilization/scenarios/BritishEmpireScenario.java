package org.flossware.civilization.scenarios;

import org.flossware.civilization.model.*;
import org.flossware.civilization.util.ScenarioBuilder;

import java.util.*;

/**
 * "British Empire Ascendant" - What if the British Empire adapted and endured into the 21st century?
 */
public final class BritishEmpireScenario {

    /**
     * Creates the British Empire scenario.
     */
    public static Scenario create() {
        return new ScenarioBuilder()
            .withId("british-empire")
            .withName("British Empire Ascendant")
            .withDescription("What if the British Empire adapted and endured into the 21st century?")
            .withTimeRange(1750, 2000)
            .withInitialState(createInitialState())
            .withTechTree(StandardTechTree.create())
            .withWorldConstraints(createWorldConstraints())
            .withSimulationRules(createSimulationRules())
            .build();
    }

    private static CivilizationState createInitialState() {
        PopulationState population = new PopulationState(
            15_000_000,
            0.025,
            0.015,
            80_000_000,
            false
        );

        List<TradeRoute> tradeRoutes = List.of(
            new TradeRoute("London", "Calcutta", List.of("tea", "cotton"), 1000.0, 0.05),
            new TradeRoute("London", "Canton", List.of("silk", "porcelain"), 700.0, 0.08),
            new TradeRoute("London", "Jamaica", List.of("sugar", "rum"), 400.0, 0.06)
        );

        EconomyState economy = new EconomyState(
            100_000_000,
            200_000,
            170_000,
            3_000_000,
            0.15,
            200_000,
            tradeRoutes
        );

        Set<String> initialTechs = new HashSet<>(Arrays.asList(
            "agriculture", "mining", "iron_working", "metallurgy_advanced",
            "coal_mining", "steam_engine", "combustion", "chemistry_basic",
            "optics", "mathematics", "writing"
        ));

        TechnologyState technology = new TechnologyState(
            initialTechs,
            new HashMap<>(),
            0.35,
            6
        );

        PoliticsState politics = new PoliticsState(
            0.70,
            "Constitutional Monarchy",
            60,
            0.0,
            false,
            false
        );

        MilitaryState military = new MilitaryState(
            250_000,
            150_000,
            1.2,
            0.8,
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
        religionShares.put("Anglicanism", 0.55);
        religionShares.put("Catholicism", 0.15);
        religionShares.put("Presbyterianism", 0.10);
        religionShares.put("Hinduism", 0.15);
        religionShares.put("Islam", 0.05);

        ReligionState religion = new ReligionState(
            religionShares,
            0.55,
            0.11,
            0.04
        );

        return new CivilizationState(
            "british",
            "British Empire",
            Arrays.asList("England", "Scotland", "Wales", "Ireland", "India", "Canada", "Australia"),
            "London",
            1750,
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
            0.65,
            0.45,
            0.25,
            0.01,
            0.7
        );
    }

    private static SimulationRules createSimulationRules() {
        return new SimulationRules(
            "adaptive",
            true,
            56789L,
            50,
            8
        );
    }
}
