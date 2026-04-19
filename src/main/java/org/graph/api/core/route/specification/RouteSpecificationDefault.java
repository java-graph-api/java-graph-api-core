package org.graph.api.core.route.specification;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.NodeRouting;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.node.TypedNode;
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

    public RouteSpecificationDefault(GraphOptions options, GraphMemory memory, TypedNode<Void, ?, ? super S> beginNode, List<NodeAspect<? extends GraphState>> aspects) {
        this.aspects = aspects;
        this.memory = memory;
        this.options = options;
        this.nodeFactory = new NodeFactory<>(options, memory);
        this.addBeginNode(beginNode);
    }

    @Override
    public <I, R, RR> RouteSpecification<S> route(TypedNode<I, R, ? super S> source, TypedNode<R, RR, ? super S> target) {
        return route(source, target, (output, state) -> true, Route.Type.DEFAULT);
    }

    @Override
    public <I, R, RR> RouteSpecification<S> route(TypedNode<I, R, ? super S> source, TypedNode<R, RR, ? super S> target, RouteConditional<R, ? super S> conditional) {
        return route(source, target, conditional, Route.Type.CONDITIONAL);
    }

    @Override
    public <I, R, RR> RouteSpecification<S> route(TypedNode<I, R, ? super S> source, TypedNode<R, RR, ? super S> target, RouteStateConditional<? super S> stateConditional) {
        return route(source, target, RouteConditional.ofState(stateConditional), Route.Type.CONDITIONAL);
    }

    @Override
    public GraphExecutor<S> end(TypedNode<?, Void, ? super S> target) {
        addEndNode(target);
        return new GraphExecutor<>(getRoutes(), memory, options);
    }

    @Override
    public GraphExecutor<S> end(Collection<TypedNode<?, Void, ? super S>> targets) {
        targets.forEach(this::addEndNode);
        return new GraphExecutor<>(getRoutes(), memory, options);
    }

    private <I, R, RR> RouteSpecification<S> route(TypedNode<I, R, ? super S> source, TypedNode<R, RR, ? super S> target, RouteConditional<R, ? super S> conditional, Route.Type type) {
        var schema = routeFactory.create(source.getName(), target.getName(), conditional, type);
        add(target, schema);
        return this;
    }

    private void addEndNode(TypedNode<?, Void, ? super S> endNode) {
        var schema = routeFactory.end(endNode.getName());
        add(endNode, schema);
    }

    private void addBeginNode(TypedNode<?, ?, ? super S> beginNode) {
        var schema = routeFactory.begin(beginNode.getName());
        add(beginNode, schema);
    }

    private void add(TypedNode<?, ?, ? super S> node, RouteSchema schema) {
        addSchema(schema);
        addNodeToMap(node);
    }

    private void addSchema(RouteSchema schema) {
        this.schemas.add(schema);
    }

    private void addNodeToMap(TypedNode<?, ?, ? super S> node) {
        this.nodeMap.put(node.getName(), createNodeProxy(node));
    }

    private TypedNode<?, ?, ? super S> createNodeProxy(TypedNode<?, ?, ? super S> target) {
        return nodeFactory.createProxy(target, aspects);
    }

    private NodeRouting<S> getRoutes() {
        Map<String, List<Route<S>>> routesMap = routeFactory.routesMap(schemas, nodeMap);
        return new NodeRouting<>(routesMap, nodeMap);
    }
}
