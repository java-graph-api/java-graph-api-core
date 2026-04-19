package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

@FunctionalInterface
public interface NodeAction<S extends GraphState> {

    void action(S state);
}
