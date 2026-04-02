package org.graph.api.core.node;

import org.graph.api.core.GraphState;

import java.util.UUID;

public abstract class AbstractNode<I, O, S extends GraphState> implements Node<I, O, S> {

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
