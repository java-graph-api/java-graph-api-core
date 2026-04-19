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

    private final GraphMemory memory;
    private final GraphOptions options;
    private final List<RouteSchema> schemas = new ArrayList<>();
    private final NodeMap<S> nodeMap = new NodeMap<>();
    private final RouteFactory<S> routeFactory = new RouteFactory<>();
    private final List<NodeAspect<? extends GraphState>> aspects;
    private final NodeFactory<S> nodeFactory;
    private final StateMergeStrategy<S> mergeStrategy;

    public RouteSpecificationDefault(GraphOptions options, GraphMemory memory, Node<? super S> beginNode, List<NodeAspect<? extends GraphState>> aspects) {
        this(options, memory, beginNode, aspects, new ReflectionStateMergeStrategy<>());
    }

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
        return route(source, target, state -> true, Route.Type.DEFAULT);
    }

    @Override
    public RouteSpecification<S> route(Node<? super S> source, Node<? super S> target, RouteConditional<? super S> stateConditional) {
        return route(source, target, stateConditional, Route.Type.CONDITIONAL);
    }

    @Override
    public GraphExecutor<S> end(Node<? super S> target) {
        addEndNode(target);
        return new GraphExecutor<>(getRoutes(), memory, options, mergeStrategy);
    }

    @Override
    public GraphExecutor<S> end(Collection<Node<? super S>> targets) {
        targets.forEach(this::addEndNode);
        return new GraphExecutor<>(getRoutes(), memory, options, mergeStrategy);
    }

    private RouteSpecification<S> route(Node<? super S> source, Node<? super S> target, RouteConditional<? super S> conditional, Route.Type type) {
        var schema = routeFactory.create(source.getName(), target.getName(), conditional, type);
        add(target, schema);
        return this;
    }

    private void addEndNode(Node<? super S> endNode) {
        var schema = routeFactory.end(endNode.getName());
        add(endNode, schema);
    }

    private void addBeginNode(Node<? super S> beginNode) {
        var schema = routeFactory.begin(beginNode.getName());
        add(beginNode, schema);
    }

    private void add(Node<? super S> node, RouteSchema schema) {
        addSchema(schema);
        addNodeToMap(node);
    }

    private void addSchema(RouteSchema schema) {
        this.schemas.add(schema);
    }

    private void addNodeToMap(Node<? super S> node) {
        this.nodeMap.put(node.getName(), createNodeProxy(node));
    }

    private Node<? super S> createNodeProxy(Node<? super S> target) {
        return nodeFactory.createProxy(target, aspects);
    }

    private NodeRouting<S> getRoutes() {
        Map<String, List<Route<S>>> routesMap = routeFactory.routesMap(schemas, nodeMap);
        return new NodeRouting<>(routesMap, nodeMap);
    }
}
