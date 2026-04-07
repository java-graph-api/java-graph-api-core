package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

@FunctionalInterface
public interface NodeAction<I, O, S extends GraphState> {

    O action(I input, S state);
}
