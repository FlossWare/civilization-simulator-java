package org.flossware.civilization.engine;

import org.flossware.civilization.model.CivilizationState;
import org.flossware.civilization.model.Event;

import java.util.List;

/**
 * Result of executing a single simulation tick.
 */
record TickResult(CivilizationState state, List<Event> events) {}
