package org.graph.api.core.route;

import org.graph.api.core.GraphState;

import java.util.Objects;

public final class RouteValidator {

    private RouteValidator() {}

    public static <S extends GraphState> void validate(
            String source,
            String target,
            RouteConditional<? super S> conditional,
            Route.Type type
    ) {
        validateTypePresent(type);

        switch (type) {
            case CONDITIONAL -> validateConditional(source, target, conditional);
            case BEGIN -> validateBegin(target);
            case END -> validateEnd(source);
            case DEFAULT -> validateDefault(source, target);
        }

        validateNoSelfLoopForDefault(source, target, type);
    }

    private static void validateTypePresent(Route.Type type) {
        Objects.requireNonNull(type, "Route type must not be null");
    }

    private static void validateConditional(
            String source,
            String target,
            RouteConditional<?> conditional
    ) {
        Objects.requireNonNull(source, "Source node must not be null for a CONDITIONAL route");
        Objects.requireNonNull(target, "Target node must not be null for a CONDITIONAL route");
        Objects.requireNonNull(conditional, "Conditional predicate must not be null for a CONDITIONAL route");
    }

    private static void validateBegin(String target) {
        Objects.requireNonNull(target, "Target node must not be null for a BEGIN route");
    }

    private static void validateEnd(String source) {
        Objects.requireNonNull(source, "Source node must not be null for an END route");
    }

    private static void validateDefault(String source, String target) {
        Objects.requireNonNull(source, "Source node must not be null for a DEFAULT route");
        Objects.requireNonNull(target, "Target node must not be null for a DEFAULT route");
    }

    private static void validateNoSelfLoopForDefault(String source, String target, Route.Type type) {
        if (type == Route.Type.DEFAULT && source.equals(target)) {
            throw new IllegalArgumentException(
                    "Default route cannot have the same source and target node. Node: '" + source + "'"
            );
        }
    }
}