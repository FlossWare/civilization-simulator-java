package org.flossware.civilization.scenarios

import org.flossware.civilization.model.*

object CarolingianScenario {
    fun create(): Scenario {
        return Scenario(
            scenarioId = "carolingian-empire",
            name = "Holy Empire",
            description = "What if the Carolingian Empire held together after Charlemagne?",
            startYear = 800,
            endYear = 1500,
            initialState = createInitialState(),
            techTree = StandardTechTree.create(),
            worldConstraints = createWorldConstraints(),
            simulationRules = createSimulationRules()
        )
    }

    private fun createInitialState(): CivilizationState {
        val population = PopulationState(10_000_000, 0.028, 0.022, 25_000_000.0, false)
        val tradeRoutes = listOf(
            TradeRoute("Aachen", "Constantinople", listOf("silk", "spices"), 400.0, 0.12),
            TradeRoute("Aachen", "Venice", listOf("glass", "cloth"), 300.0, 0.1)
        )
        val economy = EconomyState(30_000_000.0, 60_000.0, 50_000.0, 1_500_000, 0.08, 60_000.0, tradeRoutes)
        val initialTechs = setOf("agriculture", "mining", "iron_working", "metallurgy_advanced", "mathematics", "writing")
        val technology = TechnologyState(initialTechs, emptyMap(), 0.08, 2)
        val politics = PoliticsState(0.55, "Feudal Monarchy", 58, 0.0, false, false)
        val military = MilitaryState(100_000, 10_000, 0.9, 0.6, false, null)
        val climate = ClimateState(0.0, 0.5, 0.3, 0.0)
        val religionShares = mapOf("Christianity" to 0.90, "Norse Paganism" to 0.08, "Judaism" to 0.02)
        val religion = ReligionState(religionShares, 0.90, 0.18, 0.06)

        return CivilizationState("carolingian", "Carolingian Empire",
            listOf("Francia", "Bavaria", "Lombardy", "Saxony", "Burgundy"),
            "Aachen", 800, population, economy, technology, politics, military, climate, religion,
            RandomEventState(Int.MIN_VALUE))
    }

    private fun createWorldConstraints() = WorldConstraints(0.5, 0.50, 0.3, 0.018, 0.6, 1.0)

    private fun createSimulationRules() = SimulationRules("adaptive", true, 34567L, 50, 8)
}
