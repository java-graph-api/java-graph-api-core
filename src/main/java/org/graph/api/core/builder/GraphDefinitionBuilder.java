package org.graph.api.core.builder;

import org.graph.api.core.GraphExecutor;
import org.graph.api.core.GraphState;
import org.graph.api.core.node.Node;
import org.graph.api.core.route.RouteConditional;

import java.util.List;

public interface GraphDefinitionBuilder<S extends GraphState> {

    GraphRouteBuilder<S> from(Node<? super S> node);

    GraphDefinitionBuilder<S> end(Node<? super S> node);

    GraphDefinitionBuilder<S> end(List<Node<? super S>> nodes);

    GraphExecutor<S> done(); // todo возможно другие имя для метода?

    interface GraphRouteBuilder<S extends GraphState> {

        ConditionalBuilder<S> to(Node<? super S> node);

        GraphDefinitionBuilder<S> defaultTo(Node<? super S> node);
    }


    interface ConditionalBuilder<S extends GraphState> {

        GraphRouteBuilder<S> when(RouteConditional<? super S> conditional);
    }
}
