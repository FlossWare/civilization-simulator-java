package org.flossware.civilization.scenarios;

import org.flossware.civilization.model.Scenario;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ScenarioRegistryTest {

    @Test
    void getAllRegisteredScenarios() {
        List<String> ids = ScenarioRegistry.availableIds();
        assertEquals(6, ids.size());
        assertTrue(ids.contains("rome"));
        assertTrue(ids.contains("sumerian"));
        assertTrue(ids.contains("carolingian"));
        assertTrue(ids.contains("ming"));
        assertTrue(ids.contains("british"));
        assertTrue(ids.contains("inca"));
    }

    @Test
    void getEachScenarioById() {
        for (String id : ScenarioRegistry.availableIds()) {
            Scenario s = ScenarioRegistry.get(id);
            assertNotNull(s, "Scenario '" + id + "' should not be null");
            assertNotNull(s.name());
            assertNotNull(s.initialState());
            assertTrue(s.startYear() < s.endYear(),
                "startYear must be before endYear for " + id);
            assertFalse(s.techTree().isEmpty(),
                "techTree must not be empty for " + id);
        }
    }

    @Test
    void defaultScenarioIsRome() {
        Scenario s1 = ScenarioRegistry.getOrDefault(null);
        Scenario s2 = ScenarioRegistry.getOrDefault("");
        Scenario s3 = ScenarioRegistry.getOrDefault("  ");
        // All should return Rome scenario
        assertTrue(s1.name().contains("Rome"));
        assertTrue(s2.name().contains("Rome"));
        assertTrue(s3.name().contains("Rome"));
    }

    @Test
    void unknownScenarioThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> ScenarioRegistry.get("atlantis"));
    }

    @Test
    void listAllReturnsCorrectMetadata() {
        var infos = ScenarioRegistry.listAll();
        assertEquals(6, infos.size());
        // First should be rome (LinkedHashMap preserves order)
        assertEquals("rome", infos.get(0).id());
        // Each should have valid data
        for (var info : infos) {
            assertNotNull(info.name());
            assertNotNull(info.description());
            assertTrue(info.startYear() < info.endYear());
        }
    }

    @Test
    void registryOrderIsStable() {
        List<String> ids = ScenarioRegistry.availableIds();
        assertEquals("rome", ids.get(0));
        assertEquals("sumerian", ids.get(1));
        assertEquals("carolingian", ids.get(2));
        assertEquals("ming", ids.get(3));
        assertEquals("british", ids.get(4));
        assertEquals("inca", ids.get(5));
    }
}
