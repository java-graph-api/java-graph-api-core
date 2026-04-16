package org.graph.api.core;

import org.graph.api.core.node.TypedNode;

public class NodeExecutor {

    public <I, R, S extends GraphState> R execute(TypedNode<I, R, S> node, I input, S state) {
        return complete(node, input, state);
    }

    public <I, R, S extends GraphState> R complete(TypedNode<I, R, S> node, I input, S state) {
        return node.call(input, state);
    }
}
