package org.flossware.civilization.engine;

import org.flossware.civilization.model.CivilizationState;
import org.flossware.civilization.model.Event;

import java.util.List;

/**
 * Result of a complete simulation run.
 */
public record SimulationResult(
    CivilizationState finalState,
    List<Event> events,
    List<SimulationSnapshot> snapshots
) {
    public SimulationResult {
        events = List.copyOf(events);
        snapshots = List.copyOf(snapshots);
    }

    public SimulationResult(CivilizationState finalState, List<Event> events) {
        this(finalState, events, List.of());
    }
}
