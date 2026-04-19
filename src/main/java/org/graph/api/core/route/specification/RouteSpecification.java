package org.graph.api.core.route.specification;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.node.Node;
import org.graph.api.core.route.conditional.RouteStateConditional;

import java.util.Collection;

public interface RouteSpecification<S extends GraphState> {

    RouteSpecification<S> route(Node<? super S> source, Node<? super S> target);

    RouteSpecification<S> route(Node<? super S> source, Node<? super S> target, RouteStateConditional<? super S> stateConditional);

    GraphExecutor<S> end(Node<? super S> target);

    GraphExecutor<S> end(Collection<Node<? super S>> targets);

}
