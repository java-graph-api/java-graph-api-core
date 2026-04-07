package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.ConsumerNodeAction;

public class ConsumerNode<I, S extends GraphState> extends FunctionalNode<I, Void, S> {

    public ConsumerNode(String name, ConsumerNodeAction<I, S> nodeAction) {
        super(name, nodeAction);
    }

    public ConsumerNode(String name, ConsumerNodeAction<I, S> nodeAction, int callLimit) {
        super(name, nodeAction, callLimit);
    }

}
