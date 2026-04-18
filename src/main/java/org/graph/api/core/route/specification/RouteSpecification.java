package org.graph.api.core.route.specification;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.node.ConsumerNode;
import org.graph.api.core.node.TypedNode;
import org.graph.api.core.route.conditional.RouteConditional;
import org.graph.api.core.route.conditional.RouteStateConditional;

import java.util.Collection;

public interface RouteSpecification<S extends GraphState> {

    <I, R, RR> RouteSpecification<S> route(TypedNode<I, R, ? super S> source, TypedNode<R, RR, ? super S> target);

    <I, R, RR> RouteSpecification<S> route(TypedNode<I, R, ? super S> source, TypedNode<R, RR, ? super S> target, RouteConditional<R, ? super S> conditional);

    <I, R, RR> RouteSpecification<S> route(TypedNode<I, R, ? super S> source, TypedNode<R, RR, ? super S> target, RouteStateConditional<? super S> stateConditional);

    GraphExecutor<S> end(TypedNode<?, Void, ? super S> target);

    GraphExecutor<S> end(Collection<ConsumerNode<?, ? super S>> targets);

}
