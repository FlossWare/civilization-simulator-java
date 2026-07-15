package org.flossware.civilization.engine;

import org.flossware.civilization.model.CivilizationState;
import org.flossware.civilization.model.Scenario;
import org.flossware.civilization.model.TechGraph;

/**
 * Immutable context passed to each simulation module during a tick.
 *
 * @param state     Current civilization state
 * @param scenario  Active scenario with world constraints
 * @param techGraph Technology dependency graph
 * @param tickType  Current tick granularity (monthly/yearly/decade)
 * @param yearSeed  Deterministic seed for this year
 */
public record TickContext(
    CivilizationState state,
    Scenario scenario,
    TechGraph techGraph,
    TickType tickType,
    long yearSeed
) {}
