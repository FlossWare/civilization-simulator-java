package org.flossware.civilization.scenarios

import org.flossware.civilization.model.*

object SumerianScenario {
    fun create(): Scenario {
        return Scenario(
            scenarioId = "sumerian-cradle",
            name = "Cradle of Civilization",
            description = "What if the Sumerian city-states unified and endured?",
            startYear = -3500,
            endYear = -539,
            initialState = createInitialState(),
            techTree = StandardTechTree.create(),
            worldConstraints = createWorldConstraints(),
            simulationRules = createSimulationRules()
        )
    }

    private fun createInitialState(): CivilizationState {
        val population = PopulationState(200_000, 0.035, 0.025, 500_000.0, false)
        val tradeRoutes = listOf(
            TradeRoute("Uruk", "Ur", listOf("grain", "textiles"), 200.0, 0.1),
            TradeRoute("Uruk", "Elam", listOf("copper", "stone"), 150.0, 0.15)
        )
        val economy = EconomyState(5_000_000.0, 10_000.0, 8_000.0, 50_000, 0.05, 10_000.0, tradeRoutes)
        val initialTechs = setOf("agriculture", "copper_smelting", "writing")
        val technology = TechnologyState(initialTechs, emptyMap(), 0.05, 1)
        val politics = PoliticsState(0.5, "City-States", 40, 0.0, false, false)
        val military = MilitaryState(20_000, 5_000, 0.8, 0.5, false, null)
        val climate = ClimateState(0.0, 0.4, 0.2, 0.0)
        val religionShares = mapOf("Sumerian Pantheon" to 0.95, "Local Cults" to 0.05)
        val religion = ReligionState(religionShares, 0.95, 0.19, 0.03)

        return CivilizationState("sumer", "Sumerian Civilization",
            listOf("Mesopotamia", "Elam", "Assyria"),
            "Uruk", -3500, population, economy, technology, politics, military, climate, religion,
            RandomEventState(Int.MIN_VALUE))
    }

    private fun createWorldConstraints() = WorldConstraints(0.4, 0.55, 0.4, 0.02, 0.7, 1.0)

    private fun createSimulationRules() = SimulationRules("adaptive", true, 23456L, 50, 8)
}
