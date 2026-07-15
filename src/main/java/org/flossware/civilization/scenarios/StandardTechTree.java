package org.flossware.civilization.scenarios;

import org.flossware.civilization.model.Technology;

import java.util.Arrays;
import java.util.List;

/**
 * Shared technology tree used by all built-in scenarios.
 * Contains 21 technologies spanning neolithic through modern eras.
 */
public final class StandardTechTree {
    private StandardTechTree() {}

    public static List<Technology> create() {
        return Arrays.asList(
            new Technology("agriculture", "neolithic", List.of(), 50, 0.1),
            new Technology("mining", "neolithic", List.of(), 50, 0.1),
            new Technology("iron_working", "classical", List.of("mining"), 100, 0.08),
            new Technology("metallurgy_advanced", "classical", List.of("iron_working"), 150, 0.06),
            new Technology("magnetism", "classical", List.of(), 60, 0.1),
            new Technology("copper_smelting", "classical", List.of("mining"), 80, 0.09),
            new Technology("coal_mining", "medieval", List.of("mining"), 120, 0.07),
            new Technology("chemistry_basic", "medieval", List.of(), 80, 0.09),
            new Technology("steam_engine", "industrial", List.of("metallurgy_advanced", "coal_mining"), 300, 0.05),
            new Technology("combustion", "industrial", List.of("steam_engine", "chemistry_basic"), 250, 0.05),
            new Technology("electricity", "industrial", List.of("magnetism", "copper_smelting"), 350, 0.04),
            new Technology("materials_science", "modern", List.of("metallurgy_advanced"), 400, 0.03),
            new Technology("semiconductor", "modern", List.of("electricity", "materials_science"), 500, 0.02),
            new Technology("writing", "neolithic", List.of(), 40, 0.12),
            new Technology("mathematics", "classical", List.of("writing"), 90, 0.08),
            new Technology("optics", "medieval", List.of("mathematics"), 110, 0.07),
            new Technology("telescope", "industrial", List.of("optics"), 200, 0.06),
            new Technology("radio", "industrial", List.of("electricity"), 280, 0.05),
            new Technology("computing", "modern", List.of("electricity", "mathematics"), 450, 0.03),
            new Technology("internet", "modern", List.of("computing", "radio"), 550, 0.02)
        );
    }
}
