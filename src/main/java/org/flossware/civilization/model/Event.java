package org.flossware.civilization.model;

/**
 * Represents a significant historical event during simulation.
 */
public record Event(
    int year,
    String civilizationId,
    EventType type,
    EventSeverity severity,
    String description,
    Object data
) {
    public enum EventType {
        TECHNOLOGY_UNLOCKED,
        POPULATION_MILESTONE,
        ECONOMIC_BOOM,
        ECONOMIC_COLLAPSE,
        WAR_DECLARED,
        WAR_ENDED,
        REBELLION,
        SUCCESSION_CRISIS,
        SUCCESSION_RESOLVED,
        RULER_DEATH,
        PLAGUE,
        CLIMATE_DISASTER,
        RELIGIOUS_SCHISM,
        TRADE_ROUTE_ESTABLISHED,
        GOVERNMENT_CHANGE,
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

    public enum EventSeverity {
        MINOR,
        MAJOR,
        CRITICAL
    }
}
