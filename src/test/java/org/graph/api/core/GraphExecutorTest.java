package org.graph.api.core;

import org.graph.api.core.exception.GraphNodeNotFoundException;
import org.graph.api.core.exception.GraphRoutingException;
import org.graph.api.core.exception.TooManyNodeCallException;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.SavePoint;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphExecutorTest {

    @Test
    void shouldExecuteComplexRoutingScenarioWithLoopsAndBranches() {
        WorkflowState state = new WorkflowState();

        Node<WorkflowState> start = node("start", s -> {
            s.value = 0;
            s.visits.add("start");
        });
        Node<WorkflowState> decide = node("decide", s -> s.visits.add("decide"));
        Node<WorkflowState> addTwo = node("addTwo", s -> {
            s.value += 2;
            s.visits.add("addTwo");
        });
        Node<WorkflowState> addOne = node("addOne", s -> {
            s.value += 1;
            s.visits.add("addOne");
        });
        Node<WorkflowState> checkpoint = node("checkpoint", s -> {
            s.pass += 1;
            s.visits.add("checkpoint" + s.pass);
        });
        Node<WorkflowState> subtractOne = node("subtractOne", s -> {
            s.value -= 1;
            s.visits.add("subtractOne");
        });

        GraphExecutor<WorkflowState> executor = new GraphSpecification<WorkflowState>()
                .options(options("complex-routing"))
                .begin(start)
                .route(start, decide)
                .route(decide, addTwo, s -> s.pass < 2)
                .route(decide, addOne, s -> s.pass >= 2 && s.value < 5)
                .route(decide, subtractOne)
                .route(addTwo, checkpoint)
                .route(addOne, checkpoint)
                .route(checkpoint, decide, s -> s.value % 2 == 0 && s.pass < 4)
                .route(checkpoint, subtractOne)
                .end(subtractOne);

        WorkflowState result = executor.execute(state, "complex-session");

        assertEquals(ExecutorStatus.COMPLETED, result.getExecutorStatus());
        assertEquals(4, result.value);
        assertEquals(3, result.pass);
        assertEquals(
                List.of("start", "decide", "addTwo", "checkpoint1", "decide", "addTwo", "checkpoint2", "decide", "addOne", "checkpoint3", "subtractOne"),
                result.visits
        );
    }

    @Test
    void shouldThrowTooManyNodeCallExceptionForInfiniteLoop() {
        Node<LoopState> loop = node("loop", s -> s.hits += 1, 3);
        Node<LoopState> finish = node("finish", s -> s.hits += 1000);

        GraphExecutor<LoopState> executor = new GraphSpecification<LoopState>()
                .options(options("loop-guard"))
                .begin(loop)
                .route(loop, loop, s -> true)
                .end(finish);

        TooManyNodeCallException exception = assertThrows(
                TooManyNodeCallException.class,
                () -> executor.execute(new LoopState(), "loop-session")
        );

        assertTrue(exception.getMessage().contains("Too many node call"));
        assertTrue(exception.getMessage().contains("loop"));
    }

    @Test
    void shouldThrowGraphNodeNotFoundExceptionWhenSavePointReferencesUnknownNode() {
        Node<ResumeState> known = node("known", s -> s.value += 1);

        GraphMemory memory = new GraphMemory() {
            @Override
            public void put(SavePoint savePoint) {
            }

            @Override
            public Optional<SavePoint> get(String graphName, String sessionId) {
                return Optional.of(
                        SavePoint.builder()
                                .graphName(graphName)
                                .sessionId(sessionId)
                                .nodeName("missing-node")
                                .state(new ResumeState())
                                .build()
                );
            }
        };

        GraphExecutor<ResumeState> executor = new GraphSpecification<ResumeState>()
                .options(options("not-found"))
                .memory(memory)
                .begin(known)
                .end(known);

        GraphNodeNotFoundException exception = assertThrows(
                GraphNodeNotFoundException.class,
                () -> executor.execute(new ResumeState(), "missing-session")
        );

        assertTrue(exception.getMessage().contains("missing-node"));
    }

    @Test
    void shouldThrowGraphRoutingExceptionWhenRouteIsMissing() {
        Node<WorkflowState> start = node("start", s -> s.value += 1);
        Node<WorkflowState> detachedEnd = node("detachedEnd", s -> s.value += 100);

        GraphExecutor<WorkflowState> executor = new GraphSpecification<WorkflowState>()
                .options(options("missing-route"))
                .begin(start)
                .end(detachedEnd);

        GraphRoutingException exception = assertThrows(
                GraphRoutingException.class,
                () -> executor.execute(new WorkflowState(), "missing-route-session")
        );

        assertTrue(exception.getMessage().contains("Route for node 'start' not found"));
    }

    @Test
    void shouldThrowGraphRoutingExceptionWhenMultipleConditionalRoutesMatch() {
        Node<WorkflowState> start = node("start", s -> s.value = 10);
        Node<WorkflowState> left = node("left", s -> s.visits.add("left"));
        Node<WorkflowState> right = node("right", s -> s.visits.add("right"));

        GraphExecutor<WorkflowState> executor = new GraphSpecification<WorkflowState>()
                .options(options("multiple-routes"))
                .begin(start)
                .route(start, left, s -> s.value > 0)
                .route(start, right, s -> s.value >= 10)
                .end(List.of(left, right));

        GraphRoutingException exception = assertThrows(
                GraphRoutingException.class,
                () -> executor.execute(new WorkflowState(), "multiple-route-session")
        );

        assertTrue(exception.getMessage().contains("Multiple routes found for node 'start'"));
        assertTrue(exception.getMessage().contains("start -> left"));
        assertTrue(exception.getMessage().contains("start -> right"));
    }

    private static GraphOptions options(String name) {
        return GraphOptions.builder()
                .graphName(name)
                .nodeCallLimit(100)
                .build();
    }

    private static <S extends GraphState> Node<S> node(String name, Consumer<S> action) {
        return new TestNode<>(name, action, 0);
    }

    @SuppressWarnings("SameParameterValue")
    private static <S extends GraphState> Node<S> node(String name, Consumer<S> action, int callLimit) {
        return new TestNode<>(name, action, callLimit);
    }

    private static final class TestNode<S extends GraphState> implements Node<S> {

        private final String name;
        private final Consumer<S> action;
        private final int callLimit;
        private final UUID id = UUID.randomUUID();

        private TestNode(String name, Consumer<S> action, int callLimit) {
            this.name = name;
            this.action = action;
            this.callLimit = callLimit;
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

        @Override
        public int callLimit() {
            return callLimit;
        }
    }

    private static final class WorkflowState extends GraphState implements Serializable {
        private int value;
        private int pass;
        private final List<String> visits = new ArrayList<>();
    }

    private static final class ResumeState extends GraphState implements Serializable {
        private int value;
    }

    private static final class LoopState extends GraphState implements Serializable {
        private int hits;
    }
}
