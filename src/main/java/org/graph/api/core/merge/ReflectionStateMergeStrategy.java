package org.graph.api.core.merge;

import org.graph.api.core.GraphState;
import org.graph.api.core.memory.merge.GraphStateMerger;

/**
 * Default merge strategy that keeps existing reflection-based merge behavior via
 * {@link GraphStateMerger}.
 *
 * @param <S> graph state type
 */
public final class ReflectionStateMergeStrategy<S extends GraphState> implements StateMergeStrategy<S> {

    @Override
    @SuppressWarnings("unchecked")
    public S merge(S savedState, S incomingState) {
        return (S) GraphStateMerger.merge(savedState, incomingState);
    }
}
