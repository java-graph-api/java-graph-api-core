package org.graph.api.core.route.specification;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.NodeRouting;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.node.ConsumerNode;
import org.graph.api.core.node.Node;
import org.graph.api.core.node.factory.NodeFactory;
import org.graph.api.core.node.factory.NodeMap;
import org.graph.api.core.options.GraphOptions;
import org.graph.api.core.route.Route;
import org.graph.api.core.route.RouteFactory;
import org.graph.api.core.route.RouteSchema;
import org.graph.api.core.route.conditional.RouteConditional;
import org.graph.api.core.route.conditional.RouteStateConditional;

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

    public RouteSpecificationDefault(GraphOptions options, GraphMemory memory, Node<Void, ?, S> beginNode, List<NodeAspect<? extends GraphState>> aspects) {
        this.aspects = aspects;
        this.memory = memory;
        this.options = options;
        this.nodeFactory = new NodeFactory<>(options, memory);
        this.addBeginNode(beginNode);
    }

    @Override
    public <I, R, RR> RouteSpecification<S> route(Node<I, R, S> source, Node<R, RR, S> target) {
        return route(source, target, (output, state) -> true, Route.Type.DEFAULT);
    }

    @Override
    public <I, R, RR> RouteSpecification<S> route(Node<I, R, S> source, Node<R, RR, S> target, RouteConditional<R, S> conditional) {
        return route(source, target, conditional, Route.Type.CONDITIONAL);
    }

    @Override
    public <I, R, RR> RouteSpecification<S> route(Node<I, R, S> source, Node<R, RR, S> target, RouteStateConditional<S> stateConditional) {
        return route(source, target, RouteConditional.ofState(stateConditional), Route.Type.CONDITIONAL);
    }

    @Override
    public GraphExecutor<S> end(Node<?, Void, S> target) {
        addEndNode(target);
        return new GraphExecutor<>(getRoutes(), memory, options);
    }

    @Override
    public GraphExecutor<S> end(Collection<ConsumerNode<?, S>> targets) {
        targets.forEach(this::addEndNode);
        return new GraphExecutor<>(getRoutes(), memory, options);
    }

    private <I, R, RR> RouteSpecification<S> route(Node<I, R, S> source, Node<R, RR, S> target, RouteConditional<R, S> conditional, Route.Type type) {
        var schema = routeFactory.create(source.getName(), target.getName(), conditional, type);
        add(target, schema);
        return this;
    }

    private void addEndNode(Node<?, Void, S> endNode) {
        var schema = routeFactory.end(endNode.getName());
        add(endNode, schema);
    }

    private void addBeginNode(Node<?, ?, S> beginNode) {
        var schema = routeFactory.begin(beginNode.getName());
        add(beginNode, schema);
    }

    private void add(Node<?, ?, S> node, RouteSchema schema) {
        addSchema(schema);
        addNodeToMap(node);
    }

    private void addSchema(RouteSchema schema) {
        this.schemas.add(schema);
    }

    private void addNodeToMap(Node<?, ?, S> node) {
        this.nodeMap.put(node.getName(), createNodeProxy(node));
    }

    private Node<?, ?, S> createNodeProxy(Node<?, ?, S> target) {
        return nodeFactory.createProxy(target, aspects);
    }

    private NodeRouting<S> getRoutes() {
        Map<String, List<Route<S>>> routesMap = routeFactory.routesMap(schemas, nodeMap);
        return new NodeRouting<>(routesMap, nodeMap);
    }
}
