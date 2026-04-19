package org.graph.api.core.merge;

import org.graph.api.core.GraphState;

/**
 * Strategy that combines persisted graph state loaded from memory with the incoming state
 * provided to {@code GraphExecutor#execute(state, sessionId)} before graph execution is resumed.
 *
 * @param <S> graph state type
 */
@FunctionalInterface
public interface StateMergeStrategy<S extends GraphState> {

    /**
     * Merges two state instances into the state that should be used for resumed execution.
     *
     * @param savedState state loaded from {@code GraphMemory}; never {@code null} when strategy is called
     * @param incomingState state passed by caller into executor; never {@code null} when strategy is called
     * @return final state for resumed graph execution. Implementations may return one of arguments directly
     * or create a new object.
     */
    S merge(S savedState, S incomingState);
}
