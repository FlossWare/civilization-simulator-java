package org.flossware.civilization.scenarios

import org.flossware.civilization.model.Scenario

object ScenarioRegistry {
    private val registry: LinkedHashMap<String, () -> Scenario> = linkedMapOf(
        "rome" to { RomeEnduresScenario.create() },
        "sumerian" to { SumerianScenario.create() },
        "carolingian" to { CarolingianScenario.create() },
        "ming" to { MingDynastyScenario.create() },
        "british" to { BritishEmpireScenario.create() },
        "inca" to { IncaScenario.create() }
    )

    fun get(id: String): Scenario {
        val factory = registry[id.lowercase()]
            ?: throw IllegalArgumentException("Unknown scenario: '$id'. Available: ${availableIds()}")
        return factory()
    }

    fun getOrDefault(id: String?): Scenario {
        if (id.isNullOrBlank()) return get("rome")
        return get(id)
    }

    fun availableIds(): List<String> = registry.keys.toList()

    data class ScenarioInfo(val id: String, val name: String, val description: String, val startYear: Int, val endYear: Int)

    fun listAll(): List<ScenarioInfo> = registry.entries.map { (id, factory) ->
        val s = factory()
        ScenarioInfo(id, s.name, s.description, s.startYear, s.endYear)
    }
}
