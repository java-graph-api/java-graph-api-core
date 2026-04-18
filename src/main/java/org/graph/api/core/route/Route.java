package org.graph.api.core.route;

import lombok.Getter;
import org.graph.api.core.GraphState;
import org.graph.api.core.exception.GraphRoutingException;
import org.graph.api.core.node.TypedNode;
import org.graph.api.core.route.conditional.RouteConditional;

public class Route<S extends GraphState> {

    private final TypedNode<?, ?, ? super S> source;
    @Getter
    private final TypedNode<?, ?, ? super S> target;
    private final RouteConditional<?, ? super S> conditional;
    private final Type type;

    Route(TypedNode<?, ?, ? super S> source, TypedNode<?, ?, ? super S> target, RouteConditional<?, ? super S> conditional, Type type) {
        this.source = source;
        this.target = target;
        this.conditional = conditional;
        this.type = type;
    }

    public TypedNode<?, ?, ? super S> getSource() {
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
