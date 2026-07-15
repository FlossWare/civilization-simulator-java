package org.flossware.civilization;

import org.flossware.civilization.model.Event;
import org.flossware.civilization.model.ReligionState;
import org.flossware.civilization.module.ModuleResult;
import org.flossware.civilization.module.ReligionModule;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ReligionModule covering share normalization, schism mechanics,
 * and trade connectivity effects.
 */
class ReligionModuleTest {

    @Test
    void sharesSumToOneAfterTick() {
        Map<String, Double> shares = Map.of("Paganism", 0.6, "Christianity", 0.3, "Judaism", 0.1);
        ReligionState state = new ReligionState(shares, 0.6, 0.12, 0.05);

        for (int seed = 0; seed < 50; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<ReligionState> result = ReligionModule.tick(state, 0.5, random);

            double sum = result.state().religionShares().values().stream()
                .mapToDouble(d -> d)
                .sum();

            assertEquals(1.0, sum, 0.001,
                "Religion shares should always sum to 1.0 after normalization (seed=" + seed + ")");
        }
    }

    @Test
    void allSharesRemainNonNegative() {
        Map<String, Double> shares = Map.of("A", 0.9, "B", 0.05, "C", 0.05);
        ReligionState state = new ReligionState(shares, 0.9, 0.18, 0.1);

        for (int seed = 0; seed < 50; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<ReligionState> result = ReligionModule.tick(state, 1.0, random);

            for (Map.Entry<String, Double> entry : result.state().religionShares().entrySet()) {
                assertTrue(entry.getValue() >= 0.0,
                    "Religion share for " + entry.getKey() + " should not be negative");
            }
        }
    }

    @Test
    void unityReflectsLargestShare() {
        Map<String, Double> shares = Map.of("A", 0.7, "B", 0.3);
        ReligionState state = new ReligionState(shares, 0.7, 0.14, 0.01);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<ReligionState> result = ReligionModule.tick(state, 0.0, random);

        double maxShare = result.state().religionShares().values().stream()
            .mapToDouble(d -> d)
            .max()
            .orElse(0.0);

        assertEquals(maxShare, result.state().religiousUnity(), 0.001,
            "Religious unity should equal the largest religion's share");
    }

    @Test
    void schismOccursUnderFavorableConditions() {
        // Conditions for schism:
        // 1. A minority religion share > 0.2
        // 2. Unity (max share) < 0.6
        // 3. Random check < 0.05
        // Low spread rate to keep shares close to original after spread
        Map<String, Double> shares = Map.of("A", 0.55, "B", 0.45);
        ReligionState state = new ReligionState(shares, 0.55, 0.11, 0.01);

        boolean schismOccurred = false;
        for (int seed = 0; seed < 2000; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<ReligionState> result = ReligionModule.tick(state, 0.0, random);

            boolean hasSchism = result.events().stream()
                .anyMatch(e -> e.type() == Event.EventType.RELIGIOUS_SCHISM);

            if (hasSchism) {
                schismOccurred = true;
                // Schism should split the largest religion, creating a new faction
                assertTrue(result.state().religionShares().size() > 2,
                    "Schism should create a new religious faction");
                break;
            }
        }

        assertTrue(schismOccurred,
            "Schism should eventually occur with significant minority and low unity (~5% chance)");
    }

    @Test
    void schismSplitsLargestReligion() {
        Map<String, Double> shares = Map.of("A", 0.55, "B", 0.45);
        ReligionState state = new ReligionState(shares, 0.55, 0.11, 0.01);

        for (int seed = 0; seed < 2000; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<ReligionState> result = ReligionModule.tick(state, 0.0, random);

            boolean hasSchism = result.events().stream()
                .anyMatch(e -> e.type() == Event.EventType.RELIGIOUS_SCHISM);

            if (hasSchism) {
                // After schism, should have a "Reformed" faction
                boolean hasReformed = result.state().religionShares().keySet().stream()
                    .anyMatch(name -> name.contains("(Reformed)"));
                assertTrue(hasReformed,
                    "Schism should create a '(Reformed)' faction");

                // Shares should still sum to 1.0
                double sum = result.state().religionShares().values().stream()
                    .mapToDouble(d -> d)
                    .sum();
                assertEquals(1.0, sum, 0.001,
                    "Shares should sum to 1.0 even after schism");
                return;
            }
        }
        fail("Could not trigger a schism within 2000 seeds");
    }

    @Test
    void noSchismWithHighUnity() {
        // Unity > 0.6 prevents schism
        Map<String, Double> shares = Map.of("A", 0.9, "B", 0.1);
        ReligionState state = new ReligionState(shares, 0.9, 0.18, 0.01);

        for (int seed = 0; seed < 100; seed++) {
            SplittableRandom random = new SplittableRandom(seed);
            ModuleResult<ReligionState> result = ReligionModule.tick(state, 0.0, random);

            boolean hasSchism = result.events().stream()
                .anyMatch(e -> e.type() == Event.EventType.RELIGIOUS_SCHISM);
            assertFalse(hasSchism,
                "Schism should not occur when dominant religion has very high share");
        }
    }

    @Test
    void tradeConnectivityAffectsSpreadRate() {
        Map<String, Double> shares = Map.of("A", 0.7, "B", 0.3);
        ReligionState state = new ReligionState(shares, 0.7, 0.14, 0.1);

        // Same seed but different trade connectivity should produce different results
        SplittableRandom noTradeRandom = new SplittableRandom(42);
        SplittableRandom highTradeRandom = new SplittableRandom(42);

        ModuleResult<ReligionState> noTradeResult = ReligionModule.tick(state, 0.0, noTradeRandom);
        ModuleResult<ReligionState> highTradeResult = ReligionModule.tick(state, 2.0, highTradeRandom);

        double noTradeShareA = noTradeResult.state().religionShares().get("A");
        double highTradeShareA = highTradeResult.state().religionShares().get("A");

        assertNotEquals(noTradeShareA, highTradeShareA, 0.0001,
            "Different trade connectivity should produce different share distributions");
    }

    @Test
    void stabilityBonusScalesWithUnity() {
        Map<String, Double> shares = Map.of("A", 0.8, "B", 0.2);
        ReligionState state = new ReligionState(shares, 0.8, 0.16, 0.01);
        SplittableRandom random = new SplittableRandom(42);

        ModuleResult<ReligionState> result = ReligionModule.tick(state, 0.0, random);

        // stabilityBonus = unity * 0.2
        double expectedBonus = result.state().religiousUnity() * 0.2;
        assertEquals(expectedBonus, result.state().stabilityBonus(), 0.001,
            "Stability bonus should equal unity * 0.2");
    }
}
