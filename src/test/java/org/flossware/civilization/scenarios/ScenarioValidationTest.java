package org.flossware.civilization.scenarios;

import org.flossware.civilization.engine.SimulationEngine;
import org.flossware.civilization.engine.SimulationResult;
import org.flossware.civilization.model.Scenario;
import org.flossware.civilization.model.TechGraph;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class ScenarioValidationTest {

    static Stream<String> scenarioIds() {
        return ScenarioRegistry.availableIds().stream();
    }

    @ParameterizedTest
    @MethodSource("scenarioIds")
    void scenarioCompletesWithoutException(String scenarioId) {
        Scenario scenario = ScenarioRegistry.get(scenarioId);
        SimulationEngine engine = new SimulationEngine(scenario, 42L);
        SimulationResult result = engine.run(0);

        assertNotNull(result);
        assertNotNull(result.finalState());
        assertTrue(result.finalState().population().population() > 0,
            scenarioId + ": final population should be positive");
        assertTrue(result.finalState().economy().wealth() >= 0,
            scenarioId + ": final wealth should be non-negative");
        assertFalse(result.events().isEmpty(),
            scenarioId + ": should generate at least one event");
    }

    @ParameterizedTest
    @MethodSource("scenarioIds")
    void scenarioTechTreeIsValid(String scenarioId) {
        Scenario scenario = ScenarioRegistry.get(scenarioId);
        // TechGraph constructor validates DAG (no cycles, valid prereqs)
        assertDoesNotThrow(() -> new TechGraph(scenario.techTree()),
            scenarioId + ": tech tree should be a valid DAG");
    }

    @ParameterizedTest
    @MethodSource("scenarioIds")
    void initialTechsExistInTechTree(String scenarioId) {
        Scenario scenario = ScenarioRegistry.get(scenarioId);
        var techIds = scenario.techTree().stream()
            .map(t -> t.id())
            .toList();
        for (String tech : scenario.initialState().technology().unlockedTechs()) {
            assertTrue(techIds.contains(tech),
                scenarioId + ": initial tech '" + tech + "' must exist in tech tree");
        }
    }

    @ParameterizedTest
    @MethodSource("scenarioIds")
    void scenarioIsReproducible(String scenarioId) {
        Scenario scenario = ScenarioRegistry.get(scenarioId);
        SimulationEngine engine1 = new SimulationEngine(scenario, 42L);
        SimulationEngine engine2 = new SimulationEngine(scenario, 42L);
        SimulationResult result1 = engine1.run(0);
        SimulationResult result2 = engine2.run(0);

        assertEquals(result1.finalState().population().population(),
            result2.finalState().population().population(),
            scenarioId + ": same seed should produce same population");
        assertEquals(result1.events().size(), result2.events().size(),
            scenarioId + ": same seed should produce same event count");
    }
}
