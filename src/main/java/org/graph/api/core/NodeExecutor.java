package org.graph.api.core;

import org.graph.api.core.node.Node;

public class NodeExecutor {

    public <I, S extends GraphState> S execute(Node<S> node, I input, S state) {
        complete(node, input, state);
        return state;
    }

    public <I, S extends GraphState> S complete(Node<S> node, I input, S state) {
        node.call(state);
        return state;
    }
}
