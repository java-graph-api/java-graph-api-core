package org.graph.api.core.route;

import org.graph.api.core.GraphState;

@FunctionalInterface
public interface RouteConditional<S extends GraphState> {

    boolean test(S state);
}
