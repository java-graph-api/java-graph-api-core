package org.graph.api.core.route.conditional;

import org.graph.api.core.GraphState;

@FunctionalInterface
public interface RouteConditional<T, S extends GraphState> {

    boolean test(T output, S state);

    static <T, S extends GraphState> RouteConditional<T, S> ofState(RouteStateConditional<S> stateConditional) {
        return (output, state) -> stateConditional.test(state);
    }
}
