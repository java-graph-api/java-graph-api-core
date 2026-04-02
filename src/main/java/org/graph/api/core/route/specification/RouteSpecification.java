package org.graph.api.core.route.specification;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.node.ConsumerNode;
import org.graph.api.core.node.Node;
import org.graph.api.core.route.conditional.RouteConditional;
import org.graph.api.core.route.conditional.RouteStateConditional;

import java.util.Collection;

public interface RouteSpecification<S extends GraphState> {

    <I, R, RR> RouteSpecification<S> route(Node<I, R, S> source, Node<R, RR, S> target);

    <I, R, RR> RouteSpecification<S> route(Node<I, R, S> source, Node<R, RR, S> target, RouteConditional<R, S> conditional);

    <I, R, RR> RouteSpecification<S> route(Node<I, R, S> source, Node<R, RR, S> target, RouteStateConditional<S> stateConditional);

    GraphExecutor<S> end(Node<?, Void, S> target);

    GraphExecutor<S> end(Collection<ConsumerNode<?, S>> targets);

}
