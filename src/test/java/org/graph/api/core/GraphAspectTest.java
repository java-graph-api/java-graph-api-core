package org.graph.api.core;

import org.graph.api.core.aspect.LoggingAspect;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.aspect.ProcessingJoinPoint;
import org.graph.api.core.builder.GraphBuilderDefault;
import org.graph.api.core.builder.GraphDefinitionBuilder;
import org.graph.api.core.node.AbstractNode;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphAspectTest {

    @Test
    void shouldApplySingleAspectAroundEachNodeInExecutionOrder() {
        AspectState state = new AspectState();

        Node<AspectState> start = node("start", s -> {
            s.value += 1;
            s.events.add("node:start");
        });
        Node<AspectState> finish = node("finish", s -> {
            s.value += 10;
            s.events.add("node:finish");
        });

        NodeAspect<AspectState> auditAspect = new NodeAspect<>() {
            @Override
            public void around(ProcessingJoinPoint<AspectState> joinPoint) {
                joinPoint.getState().events.add("aspect:before:" + joinPoint.getCurrentNodeName());
                joinPoint.action();
                joinPoint.getState().events.add("aspect:after:" + joinPoint.getCurrentNodeName());
            }
        };

        GraphDefinitionBuilder<AspectState> graph = new GraphBuilderDefault<AspectState>()
                .options(options("aspect-single"))
                .aspect(auditAspect)
                .begin(start)
                ;

        graph.from(start)
                .defaultTo(finish);

        graph.end(finish);

        GraphExecutor<AspectState> executor = graph.done();

        AspectState result = executor.execute(state, "aspect-1");

        assertEquals(ExecutorStatus.COMPLETED, result.getExecutorStatus());
        assertEquals(11, result.value);
        assertEquals(
                List.of(
                        "aspect:before:start", "node:start", "aspect:after:start",
                        "aspect:before:finish", "node:finish", "aspect:after:finish"
                ),
                result.events
        );
    }

    @Test
    void shouldRespectAspectOrderForNestedAroundCalls() {
        AspectState state = new AspectState();

        Node<AspectState> start = node("start", s -> s.events.add("node:start"));
        Node<AspectState> middle = node("middle", s -> s.events.add("node:middle"));
        Node<AspectState> finish = node("finish", s -> s.events.add("node:finish"));

        NodeAspect<AspectState> outer = new OrderedAspect("outer", 1);
        NodeAspect<AspectState> inner = new OrderedAspect("inner", 10);

        GraphDefinitionBuilder<AspectState> graph = new GraphBuilderDefault<AspectState>()
                .options(options("aspect-order"))
                .aspect(inner)
                .aspect(outer)
                .begin(start)
                ;

        graph.from(start)
                .defaultTo(middle);

        graph.from(middle)
                .defaultTo(finish);

        graph.end(finish);

        GraphExecutor<AspectState> executor = graph.done();

        AspectState result = executor.execute(state, "aspect-2");

        assertEquals(
                List.of(
                        "outer:before:start", "inner:before:start", "node:start", "inner:after:start", "outer:after:start",
                        "outer:before:middle", "inner:before:middle", "node:middle", "inner:after:middle", "outer:after:middle",
                        "outer:before:finish", "inner:before:finish", "node:finish", "inner:after:finish", "outer:after:finish"
                ),
                result.events
        );
    }


    @Test
    void shouldExecuteWithLoggingAspectAndPreserveAspectOrder() {
        AspectState state = new AspectState();

        Node<AspectState> start = node("start", s -> s.events.add("node:start"));
        Node<AspectState> finish = node("finish", s -> s.events.add("node:finish"));

        SpyLoggingAspect loggingAspect = new SpyLoggingAspect();
        NodeAspect<AspectState> customAspect = new NodeAspect<>() {
            @Override
            public int order() {
                return 100;
            }

            @Override
            public void around(ProcessingJoinPoint<AspectState> joinPoint) {
                joinPoint.getState().events.add("custom:before:" + joinPoint.getCurrentNodeName());
                joinPoint.action();
                joinPoint.getState().events.add("custom:after:" + joinPoint.getCurrentNodeName());
            }
        };

        GraphDefinitionBuilder<AspectState> graph = new GraphBuilderDefault<AspectState>()
                .options(options("aspect-logging"))
                .aspect(customAspect)
                .aspect(loggingAspect)
                .begin(start)
                ;

        graph.from(start)
                .defaultTo(finish);

        graph.end(finish);

        GraphExecutor<AspectState> executor = graph.done();

        AspectState result = executor.execute(state, "aspect-logging-session");

        assertEquals(
                List.of(
                        "log:before:start", "custom:before:start", "node:start", "custom:after:start", "log:after:start",
                        "log:before:finish", "custom:before:finish", "node:finish", "custom:after:finish", "log:after:finish"
                ),
                result.events
        );
    }

    @Test
    void shouldApplyAspectsAddedViaAspectsMethod() {
        AspectState state = new AspectState();

        Node<AspectState> start = node("start", s -> s.events.add("node:start"));
        Node<AspectState> finish = node("finish", s -> s.events.add("node:finish"));

        NodeAspect<AspectState> outer = new OrderedAspect("outer", 1);
        NodeAspect<AspectState> inner = new OrderedAspect("inner", 10);

        GraphDefinitionBuilder<AspectState> graph = new GraphBuilderDefault<AspectState>()
                .options(options("aspect-list"))
                .aspects(List.of(inner, outer))
                .begin(start)
                ;

        graph.from(start)
                .defaultTo(finish);

        graph.end(finish);

        GraphExecutor<AspectState> executor = graph.done();

        AspectState result = executor.execute(state, "aspect-list-session");

        assertEquals(
                List.of(
                        "outer:before:start", "inner:before:start", "node:start", "inner:after:start", "outer:after:start",
                        "outer:before:finish", "inner:before:finish", "node:finish", "inner:after:finish", "outer:after:finish"
                ),
                result.events
        );
    }

    private static GraphOptions options(String name) {
        return GraphOptions.builder().graphName(name).nodeCallLimit(100).build();
    }

    private static <S extends GraphState> Node<S> node(String name, Consumer<S> action) {
        return new TestNode<>(name, action);
    }

    private record OrderedAspect(String name, int order) implements NodeAspect<AspectState> {

        @Override
            public void around(ProcessingJoinPoint<AspectState> joinPoint) {
                joinPoint.getState().events.add(name + ":before:" + joinPoint.getCurrentNodeName());
                joinPoint.action();
                joinPoint.getState().events.add(name + ":after:" + joinPoint.getCurrentNodeName());
            }
        }


    private static final class SpyLoggingAspect extends LoggingAspect {
        @Override
        public void around(ProcessingJoinPoint<GraphState> processingJoinPoint) {
            AspectState state = (AspectState) processingJoinPoint.getState();
            state.events.add("log:before:" + processingJoinPoint.getCurrentNodeName());
            super.around(processingJoinPoint);
            state.events.add("log:after:" + processingJoinPoint.getCurrentNodeName());
        }
    }

    private static final class TestNode<S extends GraphState> extends AbstractNode<S> {

        private final String name;
        private final Consumer<S> action;
        private final UUID id = UUID.randomUUID();

        private TestNode(String name, Consumer<S> action) {
            this.name = name;
            this.action = action;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void call(S state) {
            action.accept(state);
        }

        @Override
        public UUID getId() {
            return id;
        }
    }

    private static final class AspectState extends GraphState implements Serializable {
        private int value;
        private final List<String> events = new ArrayList<>();
    }
}
