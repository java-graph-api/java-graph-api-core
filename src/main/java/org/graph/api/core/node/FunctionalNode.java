package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.NodeAction;

import java.util.Objects;
import java.util.UUID;

public class FunctionalNode<I, O, S extends GraphState> implements Node<I, O, S> {

    private final String name;
    private final NodeAction<I, O, S> nodeAction;
    private final UUID id = UUID.randomUUID();
    private final int callLimit;

    public FunctionalNode(String name, NodeAction<I, O, S> nodeAction) {
        this(name, nodeAction, 0);
    }

    public FunctionalNode(String name, NodeAction<I, O, S> nodeAction, int callLimit) {
        validate(name, nodeAction);
        this.name = name;
        this.nodeAction = nodeAction;
        this.callLimit = callLimit;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public O call(I input, S state) {
        return nodeAction.action(input, state);
    }

    @Override
    public int callLimit() {
        return callLimit;
    }

    @Override
    public UUID getId() {
        return id;
    }

    private void validate(String name, NodeAction<I, O, S> nodeAction) {
        Objects.requireNonNull(name, "Node name cannot be null");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Node name cannot be empty");
        }
        Objects.requireNonNull(nodeAction, "Node action cannot be null");
    }
}
