package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.UnaryNodeAction;

public class UnaryNode<D, S extends GraphState> extends FunctionalNode<D, D, S> {

    public UnaryNode(String name, UnaryNodeAction<D, S> nodeAction) {
        this(name, nodeAction, 0);
    }

    public UnaryNode(String name, UnaryNodeAction<D, S> nodeAction, int callLimit) {
        super(name, nodeAction, callLimit);
    }
}
