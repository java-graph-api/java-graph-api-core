package org.graph.api.core;

import lombok.Builder;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.point.SavePoint;
import org.graph.api.core.memory.point.merge.StateMergeStrategy;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.graph.api.core.route.Route;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unchecked")
public final class GraphExecutor<S extends GraphState> {

    private final GraphMemory memory;
    private final GraphOptions options;
    private final NodeRouting<S> nodeRouting;
    private final StateMergeStrategy<S> mergeStrategy;

    public GraphExecutor(NodeRouting<S> nodeRouting, GraphMemory memory, GraphOptions options, StateMergeStrategy<S> mergeStrategy) {
        this.memory = memory;
        this.options = options;
        this.nodeRouting = nodeRouting;
        this.mergeStrategy = Objects.requireNonNull(mergeStrategy, "mergeStrategy cannot be null");
    }

    public String getName() {
        return options.getGraphName();
    }

    public S execute(S state, String sessionId) {
        Objects.requireNonNull(sessionId, "sessionId cannot be null");
        state.init(sessionId);
        return internalExecute(state);
    }

    private S internalExecute(S state) {
        StartPoint<S> startPoint = loadStartNodeAndState(state);

        Node<S> beginNode = startPoint.node();
        state = startPoint.state();

        beginNode.call(state);

        if (state.isGraphInterrupted()) {
            return state;
        }

        Route<S> route = nextRoute(beginNode, state);

        while (!route.isEnd()) {
            Node<S> currentNode = (Node<S>) nodeRouting.getNode(route.getTarget());
            currentNode.call(state);

            if (state.isGraphInterrupted()) {
                return state;
            }

            route = nextRoute(currentNode, state);
        }

        return complete(state);
    }

    private StartPoint<S> loadStartNodeAndState(S state) {
        return getSavePoint(state)
                .map(sp -> StartPoint.<S>builder()
                        .node((Node<S>) nodeRouting.getNode(sp.nodeName()))
                        .state(mergeStrategy.merge((S) sp.state(), state))
                        .build()
                ).orElseGet(() -> StartPoint.<S>builder()
                        .node((Node<S>) nodeRouting.getBeginNode())
                        .state(state)
                        .build());
    }

    private Optional<SavePoint> getSavePoint(S state) {
        if (memory == null) {
            return Optional.empty();
        }
        return memory.get(options.getGraphName(), state.getSessionId());
    }

    private Route<S> nextRoute(Node<S> node, S state) {
        return nodeRouting.getRoute(node, state);
    }

    private S complete(S state) {
        state.completed();
        return state;
    }

    @Builder
    private record StartPoint<S extends GraphState>(Node<S> node, S state) {
    }
}
