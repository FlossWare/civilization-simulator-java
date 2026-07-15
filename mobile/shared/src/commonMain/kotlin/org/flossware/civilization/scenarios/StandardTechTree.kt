package org.flossware.civilization.scenarios

import org.flossware.civilization.model.Technology

object StandardTechTree {
    fun create(): List<Technology> = listOf(
        Technology("agriculture", "neolithic", emptyList(), 50.0, 0.1),
        Technology("mining", "neolithic", emptyList(), 50.0, 0.1),
        Technology("iron_working", "classical", listOf("mining"), 100.0, 0.08),
        Technology("metallurgy_advanced", "classical", listOf("iron_working"), 150.0, 0.06),
        Technology("magnetism", "classical", emptyList(), 60.0, 0.1),
        Technology("copper_smelting", "classical", listOf("mining"), 80.0, 0.09),
        Technology("coal_mining", "medieval", listOf("mining"), 120.0, 0.07),
        Technology("chemistry_basic", "medieval", emptyList(), 80.0, 0.09),
        Technology("steam_engine", "industrial", listOf("metallurgy_advanced", "coal_mining"), 300.0, 0.05),
        Technology("combustion", "industrial", listOf("steam_engine", "chemistry_basic"), 250.0, 0.05),
        Technology("electricity", "industrial", listOf("magnetism", "copper_smelting"), 350.0, 0.04),
        Technology("materials_science", "modern", listOf("metallurgy_advanced"), 400.0, 0.03),
        Technology("semiconductor", "modern", listOf("electricity", "materials_science"), 500.0, 0.02),
        Technology("writing", "neolithic", emptyList(), 40.0, 0.12),
        Technology("mathematics", "classical", listOf("writing"), 90.0, 0.08),
        Technology("optics", "medieval", listOf("mathematics"), 110.0, 0.07),
        Technology("telescope", "industrial", listOf("optics"), 200.0, 0.06),
        Technology("radio", "industrial", listOf("electricity"), 280.0, 0.05),
        Technology("computing", "modern", listOf("electricity", "mathematics"), 450.0, 0.03),
        Technology("internet", "modern", listOf("computing", "radio"), 550.0, 0.02)
    )
}
