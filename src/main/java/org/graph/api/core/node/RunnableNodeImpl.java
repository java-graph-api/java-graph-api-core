package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.RunnableNodeAction;

public class RunnableNodeImpl<S extends GraphState> extends ConsumerNode<Void, S> {

    public RunnableNodeImpl(String name, RunnableNodeAction<S> nodeAction) {
        super(name, nodeAction);
    }

    public RunnableNodeImpl(String name, RunnableNodeAction<S> nodeAction, int callLimit) {
        super(name, nodeAction, callLimit);
    }

}
