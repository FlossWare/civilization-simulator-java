package org.flossware.civilization.scenarios

import org.flossware.civilization.model.*

object MingDynastyScenario {
    fun create(): Scenario {
        return Scenario(
            scenarioId = "ming-dynasty",
            name = "Ming Dynastic Glory",
            description = "What if the Ming Dynasty never closed its borders?",
            startYear = 1368,
            endYear = 1800,
            initialState = createInitialState(),
            techTree = StandardTechTree.create(),
            worldConstraints = createWorldConstraints(),
            simulationRules = createSimulationRules()
        )
    }

    private fun createInitialState(): CivilizationState {
        val population = PopulationState(60_000_000, 0.025, 0.018, 150_000_000.0, false)
        val tradeRoutes = listOf(
            TradeRoute("Beijing", "Malacca", listOf("silk", "porcelain"), 800.0, 0.08),
            TradeRoute("Beijing", "Japan", listOf("silver", "copper"), 500.0, 0.1),
            TradeRoute("Beijing", "India", listOf("spices", "cotton"), 600.0, 0.1)
        )
        val economy = EconomyState(200_000_000.0, 400_000.0, 350_000.0, 10_000_000, 0.12, 400_000.0, tradeRoutes)
        val initialTechs = setOf("agriculture", "mining", "iron_working", "magnetism", "coal_mining", "mathematics", "metallurgy_advanced", "writing")
        val technology = TechnologyState(initialTechs, emptyMap(), 0.12, 5)
        val politics = PoliticsState(0.65, "Imperial Bureaucracy", 40, 0.0, false, false)
        val military = MilitaryState(1_000_000, 200_000, 1.0, 0.7, false, null)
        val climate = ClimateState(0.0, 0.5, 0.3, 0.0)
        val religionShares = mapOf("Confucianism" to 0.50, "Buddhism" to 0.30, "Taoism" to 0.15, "Islam" to 0.05)
        val religion = ReligionState(religionShares, 0.50, 0.10, 0.04)

        return CivilizationState("ming", "Ming Dynasty",
            listOf("Zhili", "Jiangnan", "Sichuan", "Guangdong", "Fujian", "Yunnan"),
            "Beijing", 1368, population, economy, technology, politics, military, climate, religion,
            RandomEventState(Int.MIN_VALUE))
    }

    private fun createWorldConstraints() = WorldConstraints(0.55, 0.35, 0.35, 0.015, 0.75, 1.0)

    private fun createSimulationRules() = SimulationRules("adaptive", true, 45678L, 50, 8)
}
