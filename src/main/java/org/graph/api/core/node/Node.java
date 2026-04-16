package org.graph.api.core.node;

import org.graph.api.core.GraphState;

public interface Node<S extends GraphState> extends TypedNode<Void, Void, S> {

    void call(S state);

    @Override
    default Void call(Void input, S state) {
        call(state);
        return null;
    }
}
