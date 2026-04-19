package org.graph.api.core.route;

public record RouteSchema(String source, String target, RouteConditional<?> conditional, Route.Type type) {

    public RouteSchema {
        validate(source, target, type);
    }

    private void validate(String source, String target, Route.Type type) {
        if (type == Route.Type.DEFAULT && source.equals(target)) {
            throw new IllegalArgumentException(String.format("Both nodes within the same default route have identical names. ['%s']", source));
        }
    }
}
