package org.graph.api.core.subgraph;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.node.AbstractNode;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class RunnableSubgraph<S extends GraphState, SS extends GraphState> extends AbstractNode<S> implements Subgraph<S>  {

    private final Function<S, SS> stateInitial;
    private final GraphExecutor<SS> subGraphExecutor;
    private final BiConsumer<S, SS> merge;

    public RunnableSubgraph(Function<S, SS> stateInitial, GraphExecutor<SS> subGraphExecutor, BiConsumer<S, SS> merge) {
        this.stateInitial = stateInitial;
        this.subGraphExecutor = subGraphExecutor;
        this.merge = merge;
    }

    @Override
    public void call(S state) {
        SS subState = stateInitial.apply(state);
        SS subStateUpdated = subGraphExecutor.execute(subState, state.getSessionId());
        merge.accept(state, subStateUpdated);
    }

    @Override
    public String getName() {
        return subGraphExecutor.getName();
    }
}
