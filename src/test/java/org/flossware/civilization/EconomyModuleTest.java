package org.flossware.civilization;

import org.flossware.civilization.model.EconomyState;
import org.flossware.civilization.model.Event;
import org.flossware.civilization.model.TradeRoute;
import org.flossware.civilization.module.EconomyModule;
import org.flossware.civilization.module.ModuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EconomyModule covering production, consumption, technology effects,
 * trade surplus, economic events, and edge cases.
 */
class EconomyModuleTest {

    @Test
    void basicProductionConsumptionBalance() {
        EconomyState state = new EconomyState(1_000_000, 0, 0, 0, 0, 0, List.of());

        ModuleResult<EconomyState> result = EconomyModule.tick(
            state, 1.0, Set.of(), 1_000_000, new SplittableRandom(42));

        assertTrue(result.state().production() > 0, "Production should be positive");
        assertTrue(result.state().consumption() > 0, "Consumption should be positive");
        assertEquals(150_000, result.state().workers(),
            "Workers should be 15% of population");
        // GDP = production * 1.0 (average price level)
        assertEquals(result.state().production(), result.state().gdp(), 0.01,
            "GDP should equal production times price level (1.0)");
    }

    @Test
    void ironWorkingProvides1Point2xMultiplier() {
        EconomyState state = new EconomyState(1_000_000, 0, 0, 0, 0, 0, List.of());
        long population = 1_000_000;

        ModuleResult<EconomyState> without = EconomyModule.tick(
            state, 1.0, Set.of(), population, new SplittableRandom(42));
        ModuleResult<EconomyState> with = EconomyModule.tick(
            state, 1.0, Set.of("iron_working"), population, new SplittableRandom(42));

        assertEquals(without.state().production() * 1.2, with.state().production(), 0.01,
            "iron_working should provide exactly 1.2x production multiplier");
    }

    @Test
    void multipleTechMultipliersStack() {
        EconomyState state = new EconomyState(1_000_000, 0, 0, 0, 0, 0, List.of());
        long population = 1_000_000;

        ModuleResult<EconomyState> base = EconomyModule.tick(
            state, 1.0, Set.of(), population, new SplittableRandom(42));
        ModuleResult<EconomyState> withTechs = EconomyModule.tick(
            state, 1.0, Set.of("agriculture", "iron_working"), population, new SplittableRandom(42));

        // agriculture=1.2x, iron_working=1.2x, combined=1.44x
        double expectedMultiplier = 1.2 * 1.2;
        assertEquals(base.state().production() * expectedMultiplier,
            withTechs.state().production(), 0.01,
            "Tech multipliers should stack multiplicatively");
    }

    @Test
    void wealthBasedProductivityBonus() {
        long population = 1_000_000;

        // Low wealth: negligible bonus
        EconomyState lowWealth = new EconomyState(100, 0, 0, 0, 0, 0, List.of());
        ModuleResult<EconomyState> lowResult = EconomyModule.tick(
            lowWealth, 1.0, Set.of(), population, new SplittableRandom(42));

        // High wealth: significant bonus
        EconomyState highWealth = new EconomyState(50_000_000, 0, 0, 0, 0, 0, List.of());
        ModuleResult<EconomyState> highResult = EconomyModule.tick(
            highWealth, 1.0, Set.of(), population, new SplittableRandom(42));

        assertTrue(highResult.state().production() > lowResult.state().production(),
            "Higher initial wealth should provide a productivity bonus");
    }

    @Test
    void tradeSurplusCalculation() {
        TradeRoute route = new TradeRoute("Rome", "Carthage", List.of("grain"), 100.0, 0.1);
        EconomyState state = new EconomyState(1_000_000, 0, 0, 0, 0, 0, List.of(route));

        ModuleResult<EconomyState> result = EconomyModule.tick(
            state, 1.0, Set.of(), 1_000_000, new SplittableRandom(42));

        // tradeSurplus = avg(volume * (1 - tariff)) = 100 * 0.9 = 90
        assertEquals(90.0, result.state().tradeSurplus(), 0.01,
            "Trade surplus should be volume * (1 - tariff) averaged over routes");
    }

    @Test
    void multipleTradeRoutesAveraged() {
        TradeRoute route1 = new TradeRoute("A", "B", List.of("grain"), 100.0, 0.1);
        TradeRoute route2 = new TradeRoute("A", "C", List.of("wine"), 200.0, 0.2);
        EconomyState state = new EconomyState(1_000_000, 0, 0, 0, 0, 0, List.of(route1, route2));

        ModuleResult<EconomyState> result = EconomyModule.tick(
            state, 1.0, Set.of(), 1_000_000, new SplittableRandom(42));

        // route1: 100 * 0.9 = 90, route2: 200 * 0.8 = 160, avg = (90+160)/2 = 125
        assertEquals(125.0, result.state().tradeSurplus(), 0.01,
            "Trade surplus should be averaged across all routes");
    }

    @Test
    void economicBoomEventOnLargeWealthIncrease() {
        // Very low initial wealth + good resources = huge percentage increase
        EconomyState state = new EconomyState(100, 0, 0, 0, 0, 0, List.of());

        ModuleResult<EconomyState> result = EconomyModule.tick(
            state, 1.5, Set.of(), 10_000_000, new SplittableRandom(42));

        boolean hasBoom = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.ECONOMIC_BOOM);
        assertTrue(hasBoom,
            "Should generate ECONOMIC_BOOM event when wealth increases by >50%");
    }

    @Test
    void economicCollapseEventOnLargeWealthDecrease() {
        // High initial wealth but massive population with scarce resources
        // causes consumption to vastly exceed production
        EconomyState state = new EconomyState(1_000_000, 0, 0, 0, 0, 0, List.of());

        ModuleResult<EconomyState> result = EconomyModule.tick(
            state, 0.1, Set.of(), 100_000_000, new SplittableRandom(42));

        boolean hasCollapse = result.events().stream()
            .anyMatch(e -> e.type() == Event.EventType.ECONOMIC_COLLAPSE);
        assertTrue(hasCollapse,
            "Should generate ECONOMIC_COLLAPSE event when wealth decreases by >30%");
    }

    @Test
    void wealthCannotGoNegative() {
        EconomyState state = new EconomyState(100, 0, 0, 0, 0, 0, List.of());

        // Large population with scarce resources drains all wealth
        ModuleResult<EconomyState> result = EconomyModule.tick(
            state, 0.1, Set.of(), 100_000_000, new SplittableRandom(42));

        assertTrue(result.state().wealth() >= 0,
            "Wealth should be clamped to a minimum of 0");
    }

    @Test
    void zeroPopulationHandledGracefully() {
        EconomyState state = new EconomyState(1_000, 0, 0, 0, 0, 0, List.of());

        // Zero population is valid; workers becomes max(1, 0) = 1
        ModuleResult<EconomyState> result = EconomyModule.tick(
            state, 1.0, Set.of(), 0, new SplittableRandom(42));

        assertEquals(1, result.state().workers(),
            "Zero population should yield minimum of 1 worker");
        assertTrue(result.state().production() > 0,
            "Production should still be positive with 1 worker");
    }

    @Test
    void negativePopulationThrows() {
        EconomyState state = new EconomyState(1_000, 0, 0, 0, 0, 0, List.of());

        assertThrows(IllegalArgumentException.class,
            () -> EconomyModule.tick(state, 1.0, Set.of(), -1, new SplittableRandom(42)),
            "Negative population should throw IllegalArgumentException");
    }
}
