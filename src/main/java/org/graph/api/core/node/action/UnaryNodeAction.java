package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

@FunctionalInterface
public interface UnaryNodeAction<D, S extends GraphState> extends NodeAction<D, D, S> {

    D action(D input, S state);

}
