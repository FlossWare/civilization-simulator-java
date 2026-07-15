package org.flossware.civilization.util;

import java.util.SplittableRandom;

/**
 * Hierarchical seed management for reproducible simulations.
 *
 * Seed Hierarchy:
 * - Base Seed: User-provided root seed
 * - Run Seed: hash(baseSeed + runIndex) for Monte Carlo isolation
 * - Year Seed: hash(runSeed + year + tickType) for year-to-year reproducibility
 * - Module Seed: hash(yearSeed + moduleName) for module isolation
 *
 * Guarantees identical results across threads, JVMs, and machines for the same base seed.
 */
public final class SeedManager {
    private final long baseSeed;

    public SeedManager(long baseSeed) {
        this.baseSeed = baseSeed;
    }

    /**
     * Derives a run-specific seed for Monte Carlo isolation.
     */
    public SplittableRandom getRunRandom(int runIndex) {
        long runSeed = hash(baseSeed, runIndex);
        return new SplittableRandom(runSeed);
    }

    /**
     * Computes a deterministic year-level seed from the base seed, run index,
     * year, and tick type. This seed is independent of iteration order.
     */
    public long getYearSeed(int runIndex, int year, String tickType) {
        long runSeed = hash(baseSeed, runIndex);
        long yearSeed = hash(runSeed, year);
        yearSeed = hash(yearSeed, tickType);
        return yearSeed;
    }

    /**
     * Derives a year-specific random generator.
     * Deterministic: same (baseSeed, runIndex, year, tickType) always
     * produces the same generator, regardless of call order.
     */
    public SplittableRandom getYearRandom(int runIndex, int year, String tickType) {
        return new SplittableRandom(getYearSeed(runIndex, year, tickType));
    }

    /**
     * Derives a module-specific random generator to prevent correlation artifacts.
     * Deterministic: same (yearSeed, moduleName) always produces the same
     * generator, regardless of call order.
     */
    public static SplittableRandom getModuleRandom(long yearSeed, String moduleName) {
        long moduleSeed = hash(yearSeed, moduleName);
        return new SplittableRandom(moduleSeed);
    }

    /**
     * Hash function for deterministic seed derivation.
     * Uses a simple but effective mixing function.
     */
    private static long hash(long seed, long value) {
        long h = seed;
        h ^= value;
        h *= 0x9e3779b97f4a7c15L; // Knuth's golden ratio
        h ^= h >>> 32;
        h *= 0x9e3779b97f4a7c15L;
        h ^= h >>> 32;
        return h;
    }

    /**
     * Hash with string parameter.
     */
    public static long hash(long seed, String value) {
        return hash(seed, value.hashCode());
    }

    public long getBaseSeed() {
        return baseSeed;
    }
}
