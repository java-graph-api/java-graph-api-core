package org.graph.api.core.route.conditional;

import org.graph.api.core.GraphState;

@FunctionalInterface
public interface RouteStateConditional<S extends GraphState> {

    boolean test(S state);
}
