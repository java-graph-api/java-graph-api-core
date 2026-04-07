package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

@FunctionalInterface
public interface ConsumerNodeAction<I, S extends GraphState> extends NodeAction<I, Void, S> {

    void complete(I input, S state);

    default Void action(I input, S state) {
        complete(input, state);
        return null;
    }

    static <I, S extends GraphState> ConsumerNodeAction<I, S> noop() {
        return (input, state) -> {
            // void
        };
    }
}
