package org.flossware.civilization.scenarios

import org.flossware.civilization.model.*

object IncaScenario {
    fun create(): Scenario {
        return Scenario(
            scenarioId = "inca-dominion",
            name = "Inca Dominion",
            description = "What if the Inca Empire resisted European contact and expanded?",
            startYear = 1200,
            endYear = 1600,
            initialState = createInitialState(),
            techTree = StandardTechTree.create(),
            worldConstraints = createWorldConstraints(),
            simulationRules = createSimulationRules()
        )
    }

    private fun createInitialState(): CivilizationState {
        val population = PopulationState(1_000_000, 0.030, 0.020, 5_000_000.0, false)
        val tradeRoutes = listOf(
            TradeRoute("Cusco", "Quito", listOf("gold", "textiles"), 250.0, 0.1),
            TradeRoute("Cusco", "Titicaca", listOf("grain", "wool"), 180.0, 0.08)
        )
        val economy = EconomyState(8_000_000.0, 16_000.0, 12_000.0, 200_000, 0.06, 16_000.0, tradeRoutes)
        val initialTechs = setOf("agriculture", "copper_smelting", "mathematics")
        val technology = TechnologyState(initialTechs, emptyMap(), 0.03, 1)
        val politics = PoliticsState(0.70, "Imperial Theocracy", 35, 0.0, false, false)
        val military = MilitaryState(50_000, 2_000, 0.7, 0.6, false, null)
        val climate = ClimateState(0.0, 0.4, 0.2, 0.0)
        val religionShares = mapOf("Inca Religion" to 0.90, "Local Animism" to 0.10)
        val religion = ReligionState(religionShares, 0.90, 0.18, 0.03)

        return CivilizationState("inca", "Inca Empire",
            listOf("Peru", "Bolivia", "Ecuador", "Chile"),
            "Cusco", 1200, population, economy, technology, politics, military, climate, religion,
            RandomEventState(Int.MIN_VALUE))
    }

    private fun createWorldConstraints() = WorldConstraints(0.6, 0.40, 0.35, 0.012, 0.65, 1.0)

    private fun createSimulationRules() = SimulationRules("adaptive", true, 67890L, 50, 8)
}
