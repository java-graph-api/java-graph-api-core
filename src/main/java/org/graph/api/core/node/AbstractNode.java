package org.graph.api.core.node;

import org.graph.api.core.GraphState;

import java.util.UUID;

public abstract class AbstractNode<S extends GraphState> implements Node<S> {

    private final UUID id = UUID.randomUUID();

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }
}
