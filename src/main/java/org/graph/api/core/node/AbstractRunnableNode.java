package org.graph.api.core.node;

import org.graph.api.core.GraphState;

public abstract class AbstractRunnableNode<S extends GraphState> extends AbstractNode<Void, Void, S> {

    @Override
    public Void call(Void input, S state) {
        call(state);
        return null;
    }

    @Override
    public Metadata getMetadata() {
        return Metadata.empty();
    }

    abstract protected void call(S state);
}
