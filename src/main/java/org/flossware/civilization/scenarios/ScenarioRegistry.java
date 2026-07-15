package org.flossware.civilization.scenarios;

import org.flossware.civilization.model.Scenario;

import java.util.*;
import java.util.function.Supplier;

/**
 * Central registry of all built-in scenarios.
 * Provides lookup by short ID, listing, and default fallback.
 */
public final class ScenarioRegistry {
    private static final LinkedHashMap<String, Supplier<Scenario>> REGISTRY = new LinkedHashMap<>();

    static {
        register("rome", RomeEnduresScenario::create);
        register("sumerian", SumerianScenario::create);
        register("carolingian", CarolingianScenario::create);
        register("ming", MingDynastyScenario::create);
        register("british", BritishEmpireScenario::create);
        register("inca", IncaScenario::create);
    }

    private static void register(String id, Supplier<Scenario> factory) {
        REGISTRY.put(id, factory);
    }

    public static Scenario get(String id) {
        Supplier<Scenario> factory = REGISTRY.get(id.toLowerCase());
        if (factory == null) {
            throw new IllegalArgumentException(
                "Unknown scenario: '" + id + "'. Available: " + availableIds());
        }
        return factory.get();
    }

    public static Scenario getOrDefault(String id) {
        if (id == null || id.isBlank()) return get("rome");
        return get(id);
    }

    public static List<String> availableIds() {
        return List.copyOf(REGISTRY.keySet());
    }

    public static List<ScenarioInfo> listAll() {
        return REGISTRY.entrySet().stream()
            .map(e -> {
                Scenario s = e.getValue().get();
                return new ScenarioInfo(e.getKey(), s.name(), s.description(), s.startYear(), s.endYear());
            })
            .toList();
    }

    public record ScenarioInfo(String id, String name, String description, int startYear, int endYear) {}
}
