package org.graph.api.core.route;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.factory.NodeMap;
import org.graph.api.core.route.conditional.RouteConditional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RouteFactory<S extends GraphState> {

    public Map<String, List<Route<S>>> routesMap(List<RouteSchema> schemas, NodeMap<S> nodeMap) {
        //noinspection unchecked
        return schemas.stream()
                .map(schema -> new Route<>(
                                nodeMap.get(schema.source()),
                                nodeMap.get(schema.target()),
                                (RouteConditional<?, S>) schema.conditional(),
                                schema.type()
                        )
                ).collect(Collectors.groupingBy(route ->
                                route.isBegin()
                                        ? Route.Type.BEGIN.name()
                                        : route.getSource().getName()
                        )
                );
    }

    public RouteSchema begin(String nodeName) {
        return new RouteSchema(null, nodeName, (output, state) -> true, Route.Type.BEGIN);
    }

    public RouteSchema create(String source, String target, RouteConditional<?, ?> conditional, Route.Type type) {
        return new RouteSchema(source, target, conditional, type);
    }

    public RouteSchema end(String nodeName) {
        return new RouteSchema(nodeName, null, (output, state) -> true, Route.Type.END);
    }
}
