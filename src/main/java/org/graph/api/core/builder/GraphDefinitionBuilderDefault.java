package org.graph.api.core.builder;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.NodeRouting;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.point.merge.StateMergeStrategy;
import org.graph.api.core.node.Node;
import org.graph.api.core.node.NodeFactory;
import org.graph.api.core.node.NodeMap;
import org.graph.api.core.options.GraphOptions;
import org.graph.api.core.route.Route;
import org.graph.api.core.route.RouteConditional;
import org.graph.api.core.route.RouteMap;

import java.util.Collection;
import java.util.List;

public class GraphDefinitionBuilderDefault<S extends GraphState> implements GraphDefinitionBuilder<S> {

    private final NodeMap<S> nodeMap = new NodeMap<>();
    private final NodeFactory<S> nodeFactory;
    private final RouteMap<S> routeMap = new RouteMap<>();
    private final GraphMemory memory;
    private final GraphOptions options;
    private final List<NodeAspect<? extends GraphState>> aspects;
    private final StateMergeStrategy<S> mergeStrategy;

    public GraphDefinitionBuilderDefault(GraphOptions options, GraphMemory memory, Node<? super S> beginNode,
                                         List<NodeAspect<? extends GraphState>> aspects, StateMergeStrategy<S> mergeStrategy
    ) {
        this.aspects = aspects;
        this.memory = memory;
        this.options = options;
        this.nodeFactory = new NodeFactory<>(options, memory);
        this.mergeStrategy = mergeStrategy;
        this.addBeginRoute(beginNode);
    }

    @Override
    public GraphRouteBuilder<S> from(Node<? super S> node) {
        Route.Builder<S> builder = Route.<S>builder().source(node);
        return new GraphRouteBuilderDefault<>(this, builder);
    }

    @Override
    public void end(Node<? super S> node) {
        Route<S> route = buildEndRoute(node);
        addRoute(route);
    }

    @Override
    public void end(Collection<Node<? super S>> nodes) {
        nodes.stream()
                .map(this::buildEndRoute)
                .forEach(this::addRoute);
    }

    @Override
    public GraphExecutor<S> done() {
        return buildGraphExecutor();
    }

    private Route<S> buildEndRoute(Node<? super S> node) {
        return Route.<S>builder()
                .source(node)
                .type(Route.Type.END)
                .build();
    }

    private void addBeginRoute(Node<? super S> beginNode) {
        addNode(beginNode);
        Route<S> route = Route.<S>builder()
                .target(beginNode)
                .type(Route.Type.BEGIN)
                .build();
        addRoute(route);
    }

    private void addRoute(Route<S> route) {
        routeMap.put(route);
    }

    private GraphExecutor<S> buildGraphExecutor() {
        NodeRouting<S> nodeRouting = new NodeRouting<>(routeMap, nodeMap);
        return new GraphExecutor<>(nodeRouting, memory, options, mergeStrategy);
    }

    private void addNode(Node<? super S> node) {
        if (!nodeMap.containsKey(node)) {
            Node<? super S> proxy = createNodeProxy(node);
            nodeMap.put(proxy);
        }
    }

    private Node<? super S> createNodeProxy(Node<? super S> target) {
        return nodeFactory.createProxy(target, aspects);
    }


    public static class GraphRouteBuilderDefault<S extends GraphState> implements GraphRouteBuilder<S> {

        private final Route.Builder<S> builder;
        private final GraphDefinitionBuilderDefault<S> graphDefinitionBuilder;

        public GraphRouteBuilderDefault(GraphDefinitionBuilderDefault<S> graphDefinitionBuilder, Route.Builder<S> builder) {
            this.builder = builder;
            this.graphDefinitionBuilder = graphDefinitionBuilder;
        }

        @Override
        public ConditionalBuilder<S> to(Node<? super S> node) {
            graphDefinitionBuilder.addNode(node);
            builder.target(node);
            return new ConditionalBuilderDefault<>(this, builder);
        }

        @Override
        public GraphDefinitionBuilder<S> defaultTo(Node<? super S> node) {
            graphDefinitionBuilder.addNode(node);
            Route<S> route = builder.target(node)
                    .type(Route.Type.DEFAULT)
                    .build();
            graphDefinitionBuilder.addRoute(route);
            return graphDefinitionBuilder;
        }
    }


    public static class ConditionalBuilderDefault<S extends GraphState> implements ConditionalBuilder<S> {

        private final GraphRouteBuilderDefault<S> graphRouteBuilder;
        private final Route.Builder<S> builder;

        public ConditionalBuilderDefault(GraphRouteBuilderDefault<S> graphRouteBuilder, Route.Builder<S> builder) {
            this.graphRouteBuilder = graphRouteBuilder;
            this.builder = builder;
        }

        @Override
        public GraphRouteBuilder<S> when(RouteConditional<? super S> conditional) {
            Route<S> route = builder.conditional(conditional)
                    .type(Route.Type.CONDITIONAL)
                    .build();
            graphRouteBuilder.graphDefinitionBuilder.addRoute(route);
            return graphRouteBuilder;
        }
    }
}
