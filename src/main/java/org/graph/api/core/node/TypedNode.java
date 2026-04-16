package org.graph.api.core.node;

import org.graph.api.core.GraphState;

import java.util.UUID;

public interface TypedNode<I, O, S extends GraphState> {

    String getName();

    O call(I input, S state);

    UUID getId();

    default int callLimit() {
        return 0;
    }
}

