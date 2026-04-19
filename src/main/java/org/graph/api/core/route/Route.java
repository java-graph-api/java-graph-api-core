package org.graph.api.core.route;

import lombok.Getter;
import org.graph.api.core.GraphState;
import org.graph.api.core.exception.GraphRoutingException;
import org.graph.api.core.node.Node;

public class Route<S extends GraphState> {

    private final Node<? super S> source;
    @Getter
    private final Node<? super S> target;
    private final RouteConditional<? super S> conditional;
    private final Type type;

    Route(Node<? super S> source, Node<? super S> target, RouteConditional<? super S> conditional, Type type) {
        this.source = source;
        this.target = target;
        this.conditional = conditional;
        this.type = type;
    }

    public Node<? super S> getSource() {
        if (source == null) {
            throw GraphRoutingException.routeNotFound(target.getName());
        }
        return source;
    }

    public boolean test(S state) {
        return conditional.test(state);
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
