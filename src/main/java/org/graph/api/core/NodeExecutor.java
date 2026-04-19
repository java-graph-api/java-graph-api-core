package org.graph.api.core;

import org.graph.api.core.node.Node;

public class NodeExecutor<S extends GraphState> {

    public void execute(Node<S> node, S state) {
        complete(node, state);
    }

    public void complete(Node<S> node, S state) {
        node.call(state);
    }
}
