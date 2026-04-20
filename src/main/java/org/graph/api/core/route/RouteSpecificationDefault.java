package org.graph.api.core.route;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.NodeRouting;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.merge.ReflectionStateMergeStrategy;
import org.graph.api.core.merge.StateMergeStrategy;
import org.graph.api.core.node.Node;
import org.graph.api.core.node.factory.NodeFactory;
import org.graph.api.core.node.factory.NodeMap;
import org.graph.api.core.options.GraphOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class RouteSpecificationDefault<S extends GraphState> implements RouteSpecification<S> {

    private final NodeMap<S> nodeMap = new NodeMap<>();
    private final NodeFactory<S> nodeFactory;
    private final RouteMap<S> routeMap = new RouteMap<>();
    private final GraphMemory memory;
    private final GraphOptions options;
    private final List<NodeAspect<? extends GraphState>> aspects;
    private final StateMergeStrategy<S> mergeStrategy;

//    public RouteSpecificationDefault(GraphOptions options, GraphMemory memory, Node<? super S> beginNode, List<NodeAspect<? extends GraphState>> aspects) {
//        this(options, memory, beginNode, aspects, new ReflectionStateMergeStrategy<>());
//    }

    public RouteSpecificationDefault(GraphOptions options, GraphMemory memory, Node<? super S> beginNode, List<NodeAspect<? extends GraphState>> aspects, StateMergeStrategy<S> mergeStrategy) {
        this.aspects = aspects;
        this.memory = memory;
        this.options = options;
        this.nodeFactory = new NodeFactory<>(options, memory);
        this.mergeStrategy = mergeStrategy;
        this.addBeginNode(beginNode);
    }

    @Override
    public RouteSpecification<S> route(Node<? super S> source, Node<? super S> target) {
        addNodeToMap(target);
        Route<S> route = Route.<S>builder()
                .source(source)
                .target(target)
                .type(Route.Type.DEFAULT)
                .build();
        addRoute(route);
        return this;
    }

    @Override
    public RouteSpecification<S> route(Node<? super S> source, Node<? super S> target, RouteConditional<? super S> conditional) {
        addNodeToMap(target);
        Route<S> route = Route.<S>builder()
                .source(source)
                .target(target)
                .conditional(conditional)
                .type(Route.Type.CONDITIONAL)
                .build();
        addRoute(route);
        return this;
    }

    @Override
    public GraphExecutor<S> end(Node<? super S> target) {
        Route<S> route = endRoteBuild(target);
        addRoute(route);
        return graphExecutorBuild();
    }

    @Override
    public GraphExecutor<S> end(Collection<Node<? super S>> targets) {
        targets.stream()
                .map(this::endRoteBuild)
                .forEach(this::addRoute);
        return graphExecutorBuild();
    }

    private Route<S> endRoteBuild(Node<? super S> node) {
        return Route.<S>builder()
                .source(node)
                .type(Route.Type.END)
                .build();
    }

    private void addBeginNode(Node<? super S> beginNode) {
        addNodeToMap(beginNode);
        Route<S> route = Route.<S>builder()
                .target(beginNode)
                .type(Route.Type.BEGIN)
                .build();
        addRoute(route);
    }

    private void addRoute(Route<S> route) {
        routeMap.put(route);
    }

    private GraphExecutor<S> graphExecutorBuild() {
        NodeRouting<S> nodeRouting = new NodeRouting<>(routeMap, nodeMap);
        return new GraphExecutor<>(nodeRouting, memory, options, mergeStrategy);
    }

    private void addNodeToMap(Node<? super S> node) {
        if (!nodeMap.containsKey(node)) {
            Node<? super S> proxy = createNodeProxy(node);
            nodeMap.put(proxy);
        }
    }

    private Node<? super S> createNodeProxy(Node<? super S> target) {
        return nodeFactory.createProxy(target, aspects);
    }
}
