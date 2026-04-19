package org.graph.api.core.merge;

import org.graph.api.core.GraphState;

/**
 * Strategy that always uses incoming state and ignores saved state.
 *
 * @param <S> graph state type
 */
public final class UseIncomingStateStrategy<S extends GraphState> implements StateMergeStrategy<S> {

    @Override
    public S merge(S savedState, S incomingState) {
        return incomingState;
    }
}
