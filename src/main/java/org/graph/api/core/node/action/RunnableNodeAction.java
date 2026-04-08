package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

@FunctionalInterface
public interface RunnableNodeAction<S extends GraphState> extends ConsumerNodeAction<Void, S> {

    void complete(S state);

    default void complete(Void input, S state) {
        action(input, state);
    }

    default Void action(Void input, S state) {
        complete(state);
        return null;
    }

    static <S extends GraphState> RunnableNodeAction<S> noop() {
        return (state) -> {
            // void
        };
    }
}
