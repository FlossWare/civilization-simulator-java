package org.flossware.civilization;

import org.flossware.civilization.model.Event;
import org.flossware.civilization.model.TechGraph;
import org.flossware.civilization.model.Technology;
import org.flossware.civilization.model.TechnologyState;
import org.flossware.civilization.module.ModuleResult;
import org.flossware.civilization.module.TechnologyModule;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TechnologyModule covering research accumulation, tech unlocking,
 * prerequisite enforcement, and cost-based research ordering.
 */
class TechnologyModuleTest {

    @Test
    void researchPointsAccumulate() {
        Technology tech = new Technology("advanced_tech", "era1", List.of(), 500.0, 0.1);
        TechGraph graph = new TechGraph(List.of(tech));
        TechnologyState state = new TechnologyState(Set.of(), Map.of(), 0.5, 1);

        // researchPoints = 10 * (1+0.5) * (1+1*0.5) * min(1, 1M/1M) = 10 * 1.5 * 1.5 * 1 = 22.5
        SplittableRandom random = new SplittableRandom(42);
        ModuleResult<TechnologyState> result = TechnologyModule.tick(
            state, graph, 0.0, 1_000_000, random);

        assertTrue(result.state().researchProgress().containsKey("advanced_tech"),
            "Should start accumulating research progress");
        assertEquals(22.5, result.state().researchProgress().get("advanced_tech"), 0.01,
            "Research points should be 10 * 1.5 * 1.5 * 1.0 = 22.5");
    }

    @Test
    void researchPointsAccumulateOverMultipleTicks() {
        Technology tech = new Technology("slow_tech", "era1", List.of(), 100.0, 0.1);
        TechGraph graph = new TechGraph(List.of(tech));
        TechnologyState state = new TechnologyState(Set.of(), Map.of(), 0.5, 1);

        // Each tick adds 22.5 points. After 4 ticks: 90. After 5: 112.5 >= 100 → unlocked
        TechnologyState current = state;
        for (int i = 0; i < 4; i++) {
            SplittableRandom random = new SplittableRandom(i);
            ModuleResult<TechnologyState> result = TechnologyModule.tick(
                current, graph, 0.0, 1_000_000, random);
            current = result.state();
            assertFalse(current.unlockedTechs().contains("slow_tech"),
                "Tech should not be unlocked after " + (i + 1) + " ticks");
        }

        // 5th tick should unlock it
        SplittableRandom random = new SplittableRandom(4);
        ModuleResult<TechnologyState> result = TechnologyModule.tick(
            current, graph, 0.0, 1_000_000, random);
        assertTrue(result.state().unlockedTechs().contains("slow_tech"),
            "Tech should be unlocked after 5 ticks (22.5 * 5 = 112.5 >= 100)");
    }

    @Test
    void techUnlocksWhenResearchCostMet() {
        Technology tech = new Technology("cheap_tech", "era1", List.of(), 20.0, 0.1);
        TechGraph graph = new TechGraph(List.of(tech));
        TechnologyState state = new TechnologyState(Set.of(), Map.of(), 0.5, 1);

        SplittableRandom random = new SplittableRandom(42);
        ModuleResult<TechnologyState> result = TechnologyModule.tick(
            state, graph, 0.0, 1_000_000, random);

        // 22.5 research points >= 20 cost → should unlock
        assertTrue(result.state().unlockedTechs().contains("cheap_tech"),
            "Tech should unlock when accumulated research points meet cost");

        boolean hasEvent = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.TECHNOLOGY_UNLOCKED);
        assertTrue(hasEvent, "TECHNOLOGY_UNLOCKED event should be generated");
    }

    @Test
    void researchProgressClearedAfterUnlock() {
        Technology tech = new Technology("quick_tech", "era1", List.of(), 15.0, 0.1);
        TechGraph graph = new TechGraph(List.of(tech));
        TechnologyState state = new TechnologyState(Set.of(), Map.of(), 0.5, 1);

        SplittableRandom random = new SplittableRandom(42);
        ModuleResult<TechnologyState> result = TechnologyModule.tick(
            state, graph, 0.0, 1_000_000, random);

        assertFalse(result.state().researchProgress().containsKey("quick_tech"),
            "Research progress should be cleared after tech is unlocked");
    }

    @Test
    void prerequisitesMustBeMetBeforeResearch() {
        Technology techA = new Technology("base_tech", "era1", List.of(), 10.0, 0.1);
        Technology techB = new Technology("advanced_tech", "era2", List.of("base_tech"), 10.0, 0.1);
        TechGraph graph = new TechGraph(List.of(techA, techB));

        // No techs unlocked -- only base_tech is researchable
        TechnologyState state = new TechnologyState(Set.of(), Map.of(), 0.5, 1);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<TechnologyState> result = TechnologyModule.tick(
            state, graph, 0.0, 1_000_000, random);

        assertTrue(result.state().unlockedTechs().contains("base_tech"),
            "base_tech (no prerequisites) should be unlocked first");
        assertFalse(result.state().unlockedTechs().contains("advanced_tech"),
            "advanced_tech should NOT be unlocked (prerequisite base_tech just unlocked this tick)");
    }

    @Test
    void prerequisiteTechUnlocksSequentially() {
        Technology techA = new Technology("step1", "era1", List.of(), 10.0, 0.1);
        Technology techB = new Technology("step2", "era2", List.of("step1"), 10.0, 0.1);
        TechGraph graph = new TechGraph(List.of(techA, techB));

        TechnologyState state = new TechnologyState(Set.of(), Map.of(), 0.5, 1);

        // First tick: unlock step1
        SplittableRandom r1 = new SplittableRandom(42);
        ModuleResult<TechnologyState> first = TechnologyModule.tick(
            state, graph, 0.0, 1_000_000, r1);
        assertTrue(first.state().unlockedTechs().contains("step1"));
        assertFalse(first.state().unlockedTechs().contains("step2"));

        // Second tick: now step2 becomes researchable
        SplittableRandom r2 = new SplittableRandom(42);
        ModuleResult<TechnologyState> second = TechnologyModule.tick(
            first.state(), graph, 0.0, 1_000_000, r2);
        assertTrue(second.state().unlockedTechs().contains("step2"),
            "step2 should be unlocked once its prerequisite step1 is met");
    }

    @Test
    void cheapestAvailableTechResearchedFirst() {
        Technology expensive = new Technology("expensive", "era1", List.of(), 100.0, 0.1);
        Technology cheap = new Technology("cheap", "era1", List.of(), 10.0, 0.1);
        TechGraph graph = new TechGraph(List.of(expensive, cheap));

        TechnologyState state = new TechnologyState(Set.of(), Map.of(), 0.5, 1);
        SplittableRandom random = new SplittableRandom(42);

        // 22.5 research points >= 10 (cheap) but < 100 (expensive)
        ModuleResult<TechnologyState> result = TechnologyModule.tick(
            state, graph, 0.0, 1_000_000, random);

        assertTrue(result.state().unlockedTechs().contains("cheap"),
            "Cheapest available tech should be researched and unlocked first");
        assertFalse(result.state().unlockedTechs().contains("expensive"),
            "More expensive tech should not be unlocked yet");
    }

    @Test
    void alreadyUnlockedTechsSkipped() {
        Technology techA = new Technology("known_tech", "era1", List.of(), 10.0, 0.1);
        Technology techB = new Technology("new_tech", "era1", List.of(), 15.0, 0.1);
        TechGraph graph = new TechGraph(List.of(techA, techB));

        // tech_a already unlocked
        TechnologyState state = new TechnologyState(Set.of("known_tech"), Map.of(), 0.5, 1);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<TechnologyState> result = TechnologyModule.tick(
            state, graph, 0.0, 1_000_000, random);

        // Should research new_tech instead of re-researching known_tech
        assertTrue(result.state().unlockedTechs().contains("new_tech"),
            "Should skip already-unlocked techs and research the next available one");
    }

    @Test
    void populationAffectsResearchSpeed() {
        Technology tech = new Technology("pop_test", "era1", List.of(), 100.0, 0.1);
        TechGraph graph = new TechGraph(List.of(tech));
        TechnologyState state = new TechnologyState(Set.of(), Map.of(), 0.5, 1);

        // Small population: factor = min(1.0, 100000/1000000) = 0.1
        SplittableRandom r1 = new SplittableRandom(42);
        ModuleResult<TechnologyState> smallPop = TechnologyModule.tick(
            state, graph, 0.0, 100_000, r1);

        // Large population: factor = min(1.0, 5000000/1000000) = 1.0
        SplittableRandom r2 = new SplittableRandom(42);
        ModuleResult<TechnologyState> largePop = TechnologyModule.tick(
            state, graph, 0.0, 5_000_000, r2);

        double smallProgress = smallPop.state().researchProgress().get("pop_test");
        double largeProgress = largePop.state().researchProgress().get("pop_test");

        assertTrue(largeProgress > smallProgress,
            "Larger population should produce more research points per tick");
    }

    @Test
    void noResearchWhenAllTechsUnlocked() {
        Technology tech = new Technology("only_tech", "era1", List.of(), 10.0, 0.1);
        TechGraph graph = new TechGraph(List.of(tech));

        // Already unlocked the only tech
        TechnologyState state = new TechnologyState(Set.of("only_tech"), Map.of(), 0.5, 1);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<TechnologyState> result = TechnologyModule.tick(
            state, graph, 0.0, 1_000_000, random);

        assertTrue(result.state().researchProgress().isEmpty(),
            "No research progress should accumulate when all techs are unlocked");
        assertTrue(result.events().isEmpty(),
            "No events should be generated when no research is possible");
    }
}
