package org.graph.api.core.route;

import lombok.Getter;
import org.graph.api.core.GraphState;
import org.graph.api.core.exception.GraphRoutingException;
import org.graph.api.core.node.Node;
import org.graph.api.core.route.conditional.RouteConditional;

public class Route<S extends GraphState> {

    private final Node<?, ?, S> source;
    @Getter
    private final Node<?, ?, S> target;
    private final RouteConditional<?, S> conditional;
    private final Type type;

    Route(Node<?, ?, S> source, Node<?, ?, S> target, RouteConditional<?, S> conditional, Type type) {
        this.source = source;
        this.target = target;
        this.conditional = conditional;
        this.type = type;
    }

    public Node<?, ?, S> getSource() {
        if (source == null) {
            throw GraphRoutingException.routeNotFound(target.getName());
        }
        return source;
    }

    public <T> boolean test(T output, S state) {
        //noinspection unchecked
        return ((RouteConditional<T, S>) conditional).test(output, state);
    }

    public boolean isBegin() {
        return type == Type.BEGIN;
    }

    public boolean isEnd() {
        return type == Type.END;
    }

    public boolean isConditional() {
        return type == Type.CONDITIONAL;
    }

    public boolean isDefault() {
        return type == Type.DEFAULT;
    }

    public enum Type {
        BEGIN,
        DEFAULT,
        CONDITIONAL,
        END
    }
}
