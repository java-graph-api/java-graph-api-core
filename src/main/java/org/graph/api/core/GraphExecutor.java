package org.graph.api.core;

import lombok.Builder;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.merge.GraphStateMerger;
import org.graph.api.core.memory.SavePoint;
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
    private final NodeExecutor<S> nodeExecutor = new NodeExecutor<>();

    public GraphExecutor(NodeRouting<S> nodeRouting, GraphMemory memory, GraphOptions options) {
        this.memory = memory;
        this.options = options;
        this.nodeRouting = nodeRouting;
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

        nodeExecutor.complete(beginNode, state);

        if (state.isGraphInterrupted()) {
            return state;
        }

        Route<S> route = nextRoute(beginNode, state);

        while (!route.isEnd()) {
            Node<S> currentNode = (Node<S>) route.getTarget();
            nodeExecutor.execute(currentNode, state);

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
                        .state((S) GraphStateMerger.merge(sp.state(), state))
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
