package org.flossware.civilization.module

import org.flossware.civilization.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object RandomEventModule {

    private const val COOLDOWN_YEARS = 25

    private const val METEOR_PROB = 0.002
    private const val VOLCANIC_PROB = 0.005
    private const val PANDEMIC_PROB = 0.008
    private const val FAMINE_PROB = 0.015
    private const val FLOOD_PROB = 0.01
    private const val INVASION_PROB = 0.012
    private const val CIVIL_WAR_PROB = 0.02
    private const val GOLDEN_AGE_PROB = 0.01
    private const val RENAISSANCE_PROB = 0.008
    private const val MARRIAGE_PROB = 0.01

    fun tick(
        state: CivilizationState,
        frequency: Double,
        random: Random
    ): ModuleResult<CivilizationState> {
        if (frequency <= 0.0) {
            return ModuleResult(state, emptyList())
        }

        val currentYear = state.year
        // Use Long arithmetic to avoid Int overflow with Int.MIN_VALUE
        if (currentYear.toLong() - state.randomEvent.lastEventYear.toLong() < COOLDOWN_YEARS) {
            return ModuleResult(state, emptyList())
        }

        val roll = random.nextDouble()
        var cumulative = 0.0

        // Meteor Strike - no preconditions
        cumulative += METEOR_PROB * frequency
        if (roll < cumulative) {
            return applyMeteorStrike(state, currentYear)
        }

        // Volcanic Eruption - no preconditions
        cumulative += VOLCANIC_PROB * frequency
        if (roll < cumulative) {
            return applyVolcanicEruption(state, currentYear)
        }

        // Great Pandemic - requires pop > 1M and no active plague
        if (state.population.population > 1_000_000 && !state.population.plagueActive) {
            cumulative += PANDEMIC_PROB * frequency
            if (roll < cumulative) {
                return applyGreatPandemic(state, currentYear)
            }
        }

        // Great Famine - requires high drought
        if (state.climate.droughtIndex > 0.6) {
            cumulative += FAMINE_PROB * frequency
            if (roll < cumulative) {
                return applyGreatFamine(state, currentYear)
            }
        }

        // Great Flood - requires high storm frequency
        if (state.climate.stormFrequency > 1.5) {
            cumulative += FLOOD_PROB * frequency
            if (roll < cumulative) {
                return applyGreatFlood(state, currentYear)
            }
        }

        // Foreign Invasion - weak military or low stability
        if (state.military.armySize < state.population.population * 0.01 ||
            state.politics.stability < 0.3
        ) {
            cumulative += INVASION_PROB * frequency
            if (roll < cumulative) {
                return applyForeignInvasion(state, currentYear)
            }
        }

        // Civil War - low stability AND active rebellion
        if (state.politics.stability < 0.3 && state.politics.inRebellion) {
            cumulative += CIVIL_WAR_PROB * frequency
            if (roll < cumulative) {
                return applyCivilWar(state, currentYear)
            }
        }

        // Golden Age - high stability, at peace, no rebellion
        if (state.politics.stability > 0.6 &&
            !state.military.atWar &&
            !state.politics.inRebellion
        ) {
            cumulative += GOLDEN_AGE_PROB * frequency
            if (roll < cumulative) {
                return applyGoldenAge(state, currentYear)
            }
        }

        // Renaissance - literate, has universities, wealthy
        if (state.technology.literacyRate > 0.1 &&
            state.technology.universities > 0 &&
            state.economy.wealth > 5_000_000
        ) {
            cumulative += RENAISSANCE_PROB * frequency
            if (roll < cumulative) {
                return applyRenaissance(state, currentYear)
            }
        }

        // Diplomatic Marriage - at peace, moderate stability
        if (!state.military.atWar && state.politics.stability > 0.4) {
            cumulative += MARRIAGE_PROB * frequency
            if (roll < cumulative) {
                return applyDiplomaticMarriage(state, currentYear)
            }
        }

        return ModuleResult(state, emptyList())
    }

    private fun applyMeteorStrike(state: CivilizationState, year: Int): ModuleResult<CivilizationState> {
        val pop = state.population
        val newPop = max(1000L, (pop.population * 0.85).toLong())
        val newPopState = pop.withPopulation(newPop)

        val eco = state.economy
        val newWealth = max(0.0, eco.wealth * 0.75)
        val newEcoState = eco.withWealth(newWealth)

        val clim = state.climate
        val newClimState = clim.copy(
            temperatureAnomaly = min(10.0, clim.temperatureAnomaly + 2.0),
            droughtIndex = min(1.0, clim.droughtIndex + 0.3),
            stormFrequency = clim.stormFrequency + 1.0
        )

        val newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withClimate(newClimState)
            .withRandomEvent(RandomEventState(year))

        val event = Event(
            0, "", EventType.METEOR_STRIKE, EventSeverity.CRITICAL,
            "A devastating meteor strike causes widespread destruction", null
        )

        return ModuleResult(newState, listOf(event))
    }

    private fun applyVolcanicEruption(state: CivilizationState, year: Int): ModuleResult<CivilizationState> {
        val pop = state.population
        val newPop = max(1000L, (pop.population * 0.95).toLong())
        val newPopState = pop.withPopulation(newPop)

        val eco = state.economy
        val newWealth = max(0.0, eco.wealth * 0.90)
        val newEcoState = eco.withWealth(newWealth)

        val clim = state.climate
        val newClimState = clim.copy(
            temperatureAnomaly = max(-10.0, clim.temperatureAnomaly - 1.5),
            droughtIndex = min(1.0, clim.droughtIndex + 0.2)
        )

        val newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withClimate(newClimState)
            .withRandomEvent(RandomEventState(year))

        val event = Event(
            0, "", EventType.VOLCANIC_ERUPTION, EventSeverity.CRITICAL,
            "A massive volcanic eruption triggers a volcanic winter", null
        )

        return ModuleResult(newState, listOf(event))
    }

    private fun applyGreatPandemic(state: CivilizationState, year: Int): ModuleResult<CivilizationState> {
        val pop = state.population
        val newPop = max(1000L, (pop.population * 0.70).toLong())
        val newPopState = pop.withPopulation(newPop).withPlague(true)

        val eco = state.economy
        val newWealth = max(0.0, eco.wealth * 0.85)
        val newEcoState = eco.withWealth(newWealth)

        val rel = state.religion
        val newUnity = max(0.0, rel.religiousUnity - 0.1)
        val newRelState = rel.copy(religiousUnity = newUnity)

        val newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withReligion(newRelState)
            .withRandomEvent(RandomEventState(year))

        val event = Event(
            0, "", EventType.GREAT_PANDEMIC, EventSeverity.CRITICAL,
            "A great pandemic sweeps through the civilization, killing millions", null
        )

        return ModuleResult(newState, listOf(event))
    }

    private fun applyGreatFamine(state: CivilizationState, year: Int): ModuleResult<CivilizationState> {
        val pop = state.population
        val newPop = max(1000L, (pop.population * 0.80).toLong())
        val newPopState = pop.withPopulation(newPop)

        val eco = state.economy
        val newWealth = max(0.0, eco.wealth * 0.85)
        val newEcoState = eco.withWealth(newWealth)

        val pol = state.politics
        val newStability = max(0.0, pol.stability - 0.2)
        val newPolState = pol.withStability(newStability)

        val newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withPolitics(newPolState)
            .withRandomEvent(RandomEventState(year))

        val event = Event(
            0, "", EventType.GREAT_FAMINE, EventSeverity.CRITICAL,
            "Widespread famine devastates the population", null
        )

        return ModuleResult(newState, listOf(event))
    }

    private fun applyGreatFlood(state: CivilizationState, year: Int): ModuleResult<CivilizationState> {
        val pop = state.population
        val newPop = max(1000L, (pop.population * 0.92).toLong())
        val newPopState = pop.withPopulation(newPop)

        val eco = state.economy
        val newWealth = max(0.0, eco.wealth * 0.85)
        val newEcoState = eco.withWealth(newWealth)

        val clim = state.climate
        val newClimState = clim.copy(
            stormFrequency = clim.stormFrequency + 0.5
        )

        val newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withClimate(newClimState)
            .withRandomEvent(RandomEventState(year))

        val event = Event(
            0, "", EventType.GREAT_FLOOD, EventSeverity.MAJOR,
            "Catastrophic flooding destroys crops and settlements", null
        )

        return ModuleResult(newState, listOf(event))
    }

    private fun applyForeignInvasion(state: CivilizationState, year: Int): ModuleResult<CivilizationState> {
        val pop = state.population
        val newPop = max(1000L, (pop.population * 0.90).toLong())
        val newPopState = pop.withPopulation(newPop)

        val eco = state.economy
        val newWealth = max(0.0, eco.wealth * 0.80)
        val newEcoState = eco.withWealth(newWealth)

        val mil = state.military
        val newArmy = max(100L, (mil.armySize * 0.60).toLong())
        val newMilState = mil.withArmySize(newArmy).withWar(true, "Foreign Horde")

        val newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withMilitary(newMilState)
            .withRandomEvent(RandomEventState(year))

        val event = Event(
            0, "", EventType.FOREIGN_INVASION, EventSeverity.CRITICAL,
            "A massive foreign invasion threatens the heartland", null
        )

        return ModuleResult(newState, listOf(event))
    }

    private fun applyCivilWar(state: CivilizationState, year: Int): ModuleResult<CivilizationState> {
        val pop = state.population
        val newPop = max(1000L, (pop.population * 0.88).toLong())
        val newPopState = pop.withPopulation(newPop)

        val mil = state.military
        val newArmy = max(100L, (mil.armySize * 0.70).toLong())
        val newMilState = mil.withArmySize(newArmy)

        val pol = state.politics
        val newStability = max(0.0, pol.stability - 0.25)
        val newExhaustion = min(1.0, pol.warExhaustion + 0.3)
        val newPolState = pol.withStability(newStability).withWarExhaustion(newExhaustion)

        val newState = state
            .withPopulation(newPopState)
            .withMilitary(newMilState)
            .withPolitics(newPolState)
            .withRandomEvent(RandomEventState(year))

        val event = Event(
            0, "", EventType.CIVIL_WAR, EventSeverity.CRITICAL,
            "Civil war erupts as factions battle for control", null
        )

        return ModuleResult(newState, listOf(event))
    }

    private fun applyGoldenAge(state: CivilizationState, year: Int): ModuleResult<CivilizationState> {
        val eco = state.economy
        val newWealth = eco.wealth * 1.30
        val newEcoState = eco.withWealth(newWealth)

        val tech = state.technology
        val newLiteracy = min(1.0, tech.literacyRate + 0.1)
        val newTechState = tech.withLiteracyRate(newLiteracy)

        val pol = state.politics
        val newStability = min(1.0, pol.stability + 0.15)
        val newPolState = pol.withStability(newStability)

        val newState = state
            .withEconomy(newEcoState)
            .withTechnology(newTechState)
            .withPolitics(newPolState)
            .withRandomEvent(RandomEventState(year))

        val event = Event(
            0, "", EventType.GOLDEN_AGE, EventSeverity.MAJOR,
            "A golden age of prosperity and cultural achievement begins", null
        )

        return ModuleResult(newState, listOf(event))
    }

    private fun applyRenaissance(state: CivilizationState, year: Int): ModuleResult<CivilizationState> {
        val tech = state.technology
        val newLiteracy = min(1.0, tech.literacyRate + 0.15)
        val newTechState = tech.withLiteracyRate(newLiteracy).copy(universities = tech.universities + 1)

        val eco = state.economy
        val newProduction = eco.production * 1.20
        val newEcoState = eco.withProduction(newProduction)

        val newState = state
            .withTechnology(newTechState)
            .withEconomy(newEcoState)
            .withRandomEvent(RandomEventState(year))

        val event = Event(
            0, "", EventType.RENAISSANCE, EventSeverity.MAJOR,
            "A renaissance of learning and innovation transforms society", null
        )

        return ModuleResult(newState, listOf(event))
    }

    private fun applyDiplomaticMarriage(state: CivilizationState, year: Int): ModuleResult<CivilizationState> {
        val pol = state.politics
        val newStability = min(1.0, pol.stability + 0.15)
        val newExhaustion = max(0.0, pol.warExhaustion - 0.1)
        val newPolState = pol.withStability(newStability).withWarExhaustion(newExhaustion)

        val eco = state.economy
        val newWealth = eco.wealth * 1.10
        val newEcoState = eco.withWealth(newWealth)

        val mil = state.military
        val newArmy = (mil.armySize * 1.10).toLong()
        val newMilState = mil.withArmySize(newArmy)

        val newState = state
            .withPolitics(newPolState)
            .withEconomy(newEcoState)
            .withMilitary(newMilState)
            .withRandomEvent(RandomEventState(year))

        val event = Event(
            0, "", EventType.DIPLOMATIC_MARRIAGE, EventSeverity.MAJOR,
            "A diplomatic marriage forges a powerful alliance", null
        )

        return ModuleResult(newState, listOf(event))
    }
}
