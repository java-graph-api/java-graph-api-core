package org.graph.api.core;

import lombok.Builder;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.SavePoint;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.graph.api.core.route.Route;
import org.graph.api.core.util.GraphStateMapper;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unchecked")
public final class GraphExecutor<S extends GraphState> {

    private final GraphMemory memory;
    private final GraphOptions options;
    private final NodeRouting<S> nodeRouting;
    private final NodeExecutor nodeExecutor = new NodeExecutor();

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
        StartPoint<Object, Object, S> startPoint = loadStartNodeAndState(state);

        Node<Object, Object, S> beginNode = startPoint.node();
        state = startPoint.state();

        Object currentResult = nodeExecutor.complete(beginNode, null, state);

        if (state.isGraphInterrupted()) {
            return complete(state);
        }

        Route<S> route = nextRoute(beginNode, currentResult, state);

        while (!route.isEnd()) {
            Node<Object, Object, S> currentNode = (Node<Object, Object, S>) route.getTarget();
            currentResult = nodeExecutor.execute(currentNode, currentResult, state);

            if (state.isGraphInterrupted()) {
                return complete(state);
            }

            route = nextRoute(currentNode, currentResult, state);
        }

        return complete(state);
    }

    private <O> StartPoint<Object, Object, S> loadStartNodeAndState(S state) {
        return (StartPoint<Object, Object, S>) getSavePoint(state)
                .map(sp -> StartPoint.<Object, O, S>builder()
                        .node((Node<Object, O, S>) nodeRouting.getNode(sp.nodeName()))
                        .state((S) GraphStateMapper.merge(sp.state(), state))
                        .build()
                ).orElseGet(() -> StartPoint.<Object, O, S>builder()
                        .node((Node<Object, O, S>) nodeRouting.getBeginNode())
                        .state(state)
                        .build());
    }

    private Optional<SavePoint> getSavePoint(S state) {
        if (memory == null) {
            return Optional.empty();
        }
        return memory.get(options.getGraphName(), state.getSessionId());
    }

    private Route<S> nextRoute(Node<Object, Object, S> node, Object currentResult, S state) {
        return nodeRouting.getRoute(node, currentResult, state);
    }

    private S complete(S state) {
        state.completed();
        return state;
    }

    @Builder
    private record StartPoint<I, O, S extends GraphState>(Node<I, O, S> node, S state) {
    }
}
