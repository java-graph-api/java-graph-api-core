package org.graph.api.core.route;

import org.graph.api.core.GraphState;
import org.graph.api.core.exception.GraphRoutingException;
import org.graph.api.core.node.Node;
import org.graph.api.core.route.conditional.RouteConditional;

import java.util.UUID;

public class Route<S extends GraphState> {

    private final Node<?, ?, S> source;
    private final Node<?, ?, S> target;
    private final RouteConditional<?, S> conditional;
    private final Type type;
    private final UUID id = UUID.randomUUID();

    Route(Node<?, ?, S> source, Node<?, ?, S> target, RouteConditional<?, S> conditional, Type type) {
        this.source = source;
        this.target = target;
        this.conditional = conditional;
        this.type = type;
    }

    public Node<?, ?, S> getSource() {
        if (source == null) {
            throw new GraphRoutingException(target.getName());
        }
        return source;
    }

    public Node<?, ?, S> getTarget() {
        return target;
    }

    public <T> boolean test(T output, S state) {
        //noinspection unchecked
        return ((RouteConditional<T, S>) conditional).test(output, state);
    }

    public Type getType() {
        return type;
    }

    public boolean isEnd() {
        return type == Type.END;
    }

    public UUID getId() {
        return id;
    }

    public enum Type {
        BEGIN,
        DEFAULT,
        CONDITIONAL,
        END
    }
}
