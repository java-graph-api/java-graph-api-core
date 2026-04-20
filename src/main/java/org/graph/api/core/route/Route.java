package org.graph.api.core.route;

import lombok.Getter;
import org.graph.api.core.GraphState;
import org.graph.api.core.node.Node;

public final class Route<S extends GraphState> {

    @Getter
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

    public boolean test(S state) {
        if (isDefault() || isEnd()) {
            return true;
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

    public static <S extends GraphState> Builder<S> builder() {
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
            RouteValidator.validate(source, target, conditional, type);
            return new Route<>(source, target, conditional, type);
        }
    }
}
