package org.graph.api.core.aspect;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.NodeInfo;
import org.graph.api.core.options.GraphOptions;

public class ProcessingJoinPoint<S extends GraphState> extends JoinPoint<S> {

    private final Runnable action;

    public ProcessingJoinPoint(S state, GraphOptions options, NodeInfo nodeInfo, Runnable action) {
        super(state, options, nodeInfo);
        this.action = action;
    }

    public void action() {
        action.run();
    }

}
