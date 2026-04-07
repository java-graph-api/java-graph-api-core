package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.RunnableNodeAction;

public class RunnableNode<S extends GraphState> extends ConsumerNode<Void, S> {

    public RunnableNode(String name, RunnableNodeAction<S> nodeAction) {
        super(name, nodeAction);
    }

    public RunnableNode(String name, RunnableNodeAction<S> nodeAction, int callLimit) {
        super(name, nodeAction, callLimit);
    }

}
