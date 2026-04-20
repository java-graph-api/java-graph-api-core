package org.graph.api.core.route;

import lombok.Getter;
import org.graph.api.core.GraphState;
import org.graph.api.core.exception.GraphRoutingException;
import org.graph.api.core.node.Node;

import java.util.Objects;

public final class Route<S extends GraphState> {

//    @Getter
    private final String source;
    @Getter
    private final String target;
    private final RouteConditional<? super S> conditional;
    private final Type type;

    Route(String source, String target, RouteConditional<? super S> conditional, Type type) {
        this.source = source;
        this.target = target;
        this.conditional = conditional;
        this.type = type;
    }

    public String getSourceNodeName() {
        return source;
    }

    public String getTargetNodeName() {
        return target;
    }

    String getSource() {
        if (source == null) {
            throw GraphRoutingException.routeNotFound(target); // todo убрать проверку отсюда туда где она используется
        }
        return source;
    }

    public boolean test(S state) {
        if (isDefault()) {
            return true;
        }

        if (isEnd()) {
            return true; // todo вообще наверное end роуты не должны проверяться на условие
        }

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

    public static <S extends GraphState> Builder<S > builder() {
        return new Builder<>();
    }

    public enum Type {
        BEGIN,
        DEFAULT,
        CONDITIONAL,
        END
    }

    public static class Builder<S extends GraphState> {

        private String source;
        private String target;
        private RouteConditional<? super S> conditional;
        private Type type;

        public Builder<S> source(Node<? super S> source) {
            this.source = source.getName();
            return this;
        }

        public Builder<S> target(Node<? super S> target) {
            this.target = target.getName();
            return this;
        }

        public Builder<S> conditional(RouteConditional<? super S> conditional) {
            this.conditional = conditional;
            return this;
        }

        public Builder<S> type(Type type) {
            this.type = type;
            return this;
        }

        public Route<S> build() {
            Objects.requireNonNull(type, "Route type must not be null");

            if (type == Type.CONDITIONAL) {
                Objects.requireNonNull(source, "Source node must not be null for a CONDITIONAL route");
                Objects.requireNonNull(target, "Target node must not be null for a CONDITIONAL route");
                Objects.requireNonNull(conditional, "Conditional predicate must not be null for a CONDITIONAL route");

            } else if (type == Type.BEGIN) {
                Objects.requireNonNull(target, "Target node must not be null for a BEGIN route");

            } else if (type == Type.END) {
                Objects.requireNonNull(source, "Source node must not be null for an END route");

            } else {
                Objects.requireNonNull(source, "Source node must not be null for a DEFAULT route");
                Objects.requireNonNull(target, "Target node must not be null for a DEFAULT route");
            }

            if (type != Type.BEGIN) {
                validate(source, target, type);
            }

            return new Route<>(source, target, conditional, type);
        }

        private void validate(String source, String target, Route.Type type) {
            if (type == Route.Type.DEFAULT && source.equals(target)) {
                throw new IllegalArgumentException(String.format(
                        "Both nodes within the same default route have identical names. ['%s']", source)
                );
            }
        }
    }
}
