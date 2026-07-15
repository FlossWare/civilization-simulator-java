package org.flossware.civilization.scenarios

import org.flossware.civilization.model.*

object BritishEmpireScenario {
    fun create(): Scenario {
        return Scenario(
            scenarioId = "british-empire",
            name = "British Empire Ascendant",
            description = "What if the British Empire adapted and endured into the 21st century?",
            startYear = 1750,
            endYear = 2000,
            initialState = createInitialState(),
            techTree = StandardTechTree.create(),
            worldConstraints = createWorldConstraints(),
            simulationRules = createSimulationRules()
        )
    }

    private fun createInitialState(): CivilizationState {
        val population = PopulationState(15_000_000, 0.025, 0.015, 80_000_000.0, false)
        val tradeRoutes = listOf(
            TradeRoute("London", "Calcutta", listOf("tea", "cotton"), 1000.0, 0.05),
            TradeRoute("London", "Canton", listOf("silk", "porcelain"), 700.0, 0.08),
            TradeRoute("London", "Jamaica", listOf("sugar", "rum"), 400.0, 0.06)
        )
        val economy = EconomyState(100_000_000.0, 200_000.0, 170_000.0, 3_000_000, 0.15, 200_000.0, tradeRoutes)
        val initialTechs = setOf("agriculture", "mining", "iron_working", "metallurgy_advanced",
            "coal_mining", "steam_engine", "combustion", "chemistry_basic",
            "optics", "mathematics", "writing")
        val technology = TechnologyState(initialTechs, emptyMap(), 0.35, 6)
        val politics = PoliticsState(0.70, "Constitutional Monarchy", 60, 0.0, false, false)
        val military = MilitaryState(250_000, 150_000, 1.2, 0.8, false, null)
        val climate = ClimateState(0.0, 0.5, 0.3, 0.0)
        val religionShares = mapOf("Anglicanism" to 0.55, "Catholicism" to 0.15, "Presbyterianism" to 0.10, "Hinduism" to 0.15, "Islam" to 0.05)
        val religion = ReligionState(religionShares, 0.55, 0.11, 0.04)

        return CivilizationState("british", "British Empire",
            listOf("England", "Scotland", "Wales", "Ireland", "India", "Canada", "Australia"),
            "London", 1750, population, economy, technology, politics, military, climate, religion,
            RandomEventState(Int.MIN_VALUE))
    }

    private fun createWorldConstraints() = WorldConstraints(0.65, 0.45, 0.25, 0.01, 0.7, 1.0)

    private fun createSimulationRules() = SimulationRules("adaptive", true, 56789L, 50, 8)
}
