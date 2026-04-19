package org.graph.api.core.subgraph;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.node.AbstractNode;

public  class InnerSubgraph<S extends GraphState> extends AbstractNode<S> implements Subgraph<S> {

    private final GraphExecutor<S> graphExecutor;

    public InnerSubgraph(GraphExecutor<S> graphExecutor) {
        this.graphExecutor = graphExecutor;
    }

    @Override
    public String getName() {
        return graphExecutor.getName();
    }

    @Override
    public void call(S state) {
        graphExecutor.execute(state, state.getSessionId());
    }
}
