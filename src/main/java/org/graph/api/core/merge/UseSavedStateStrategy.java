package org.graph.api.core.merge;

import org.graph.api.core.GraphState;

/**
 * Strategy that always uses state loaded from memory and ignores incoming state.
 *
 * @param <S> graph state type
 */
public final class UseSavedStateStrategy<S extends GraphState> implements StateMergeStrategy<S> {

    @Override
    public S merge(S savedState, S incomingState) {
        return savedState;
    }
}
