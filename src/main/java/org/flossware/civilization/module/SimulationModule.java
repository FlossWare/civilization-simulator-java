package org.flossware.civilization.module;

import org.flossware.civilization.engine.TickContext;
import org.flossware.civilization.model.CivilizationState;

/**
 * Interface for simulation modules that participate in the tick cycle.
 *
 * Each module encapsulates a domain of the simulation (population, economy,
 * climate, etc.) and provides a uniform contract for the tick executor.
 */
public interface SimulationModule {

    /**
     * Returns the unique name of this module, used for deterministic seed derivation.
     */
    String moduleName();

    /**
     * Executes one tick of this module's simulation logic.
     *
     * @param context the current tick context containing state, scenario, and seed
     * @return the result containing the updated module state and any generated events
     */
    ModuleResult<?> tick(TickContext context);

    /**
     * Applies this module's result back to the civilization state.
     *
     * @param state  the current civilization state
     * @param result the result from {@link #tick(TickContext)}
     * @return the updated civilization state
     */
    CivilizationState applyResult(CivilizationState state, ModuleResult<?> result);
}
