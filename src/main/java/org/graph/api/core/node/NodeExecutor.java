package org.graph.api.core.node;

import org.graph.api.core.GraphState;

public class NodeExecutor<S extends GraphState> {

    public void execute(Node<S> node, S state) {
        node.call(state);
    }
}
