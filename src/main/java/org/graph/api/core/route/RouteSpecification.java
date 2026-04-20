package org.graph.api.core.route;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.node.Node;

import java.util.Collection;

public interface RouteSpecification<S extends GraphState> {

    RouteSpecification<S> route(Node<? super S> source, Node<? super S> target);

    RouteSpecification<S> route(Node<? super S> source, Node<? super S> target, RouteConditional<? super S> conditional);

    GraphExecutor<S> end(Node<? super S> target);

    GraphExecutor<S> end(Collection<Node<? super S>> targets);

}
