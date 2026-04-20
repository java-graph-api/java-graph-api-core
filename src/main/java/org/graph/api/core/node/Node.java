package org.graph.api.core.node;

import org.graph.api.core.GraphState;

import java.util.UUID;

public interface Node<S extends GraphState> {

    String getName();

    void call(S state);

    UUID getId();

    default int invocationLimit() {
        return 0;
    }
}
