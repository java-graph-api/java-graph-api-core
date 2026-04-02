package org.graph.api.core.aspect;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.NodeInfo;
import org.graph.api.core.options.GraphOptions;

import java.util.function.Supplier;

public class ProcessingJoinPoint<S extends GraphState> extends JoinPoint<S> {

    private final Supplier<?> action;

    public ProcessingJoinPoint(S state, GraphOptions options, NodeInfo nodeInfo, Supplier<?> action) {
        super(state, options, nodeInfo);
        this.action = action;
    }

    public Object action() {
        return action.get();
    }

}
