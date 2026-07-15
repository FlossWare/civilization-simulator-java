package org.flossware.civilization.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val year: Int,
    val civilizationId: String,
    val type: EventType,
    val severity: EventSeverity,
    val description: String,
    val data: String? = null
)

@Serializable
enum class EventType {
    TECHNOLOGY_UNLOCKED,
    POPULATION_MILESTONE,
    ECONOMIC_BOOM,
    ECONOMIC_COLLAPSE,
    WAR_DECLARED,
    WAR_ENDED,
    REBELLION,
    SUCCESSION_CRISIS,
    PLAGUE,
    CLIMATE_DISASTER,
    RELIGIOUS_SCHISM,
    TRADE_ROUTE_ESTABLISHED,
    GOVERNMENT_CHANGE,
    SUCCESSION_RESOLVED,
    RULER_DEATH,
    METEOR_STRIKE,
    GREAT_PANDEMIC,
    GOLDEN_AGE,
    GREAT_FAMINE,
    FOREIGN_INVASION,
    RENAISSANCE,
    VOLCANIC_ERUPTION,
    GREAT_FLOOD,
    CIVIL_WAR,
    DIPLOMATIC_MARRIAGE
}

@Serializable
enum class EventSeverity {
    MINOR,
    MAJOR,
    CRITICAL
}
