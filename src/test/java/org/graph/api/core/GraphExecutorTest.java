package org.graph.api.core;

import org.graph.api.core.exception.GraphNodeNotFoundException;
import org.graph.api.core.exception.GraphRoutingException;
import org.graph.api.core.exception.TooManyNodeCallException;
import org.graph.api.core.builder.GraphBuilderDefault;
import org.graph.api.core.builder.GraphDefinitionBuilder;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.InMemoryGraphMemory;
import org.graph.api.core.memory.SavePoint;
import org.graph.api.core.merge.StateMergeStrategy;
import org.graph.api.core.merge.UseIncomingStateStrategy;
import org.graph.api.core.merge.UseSavedStateStrategy;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

        GraphDefinitionBuilder<WorkflowState> graph = new GraphBuilderDefault<WorkflowState>()
                .options(options("complex-routing"))
                .begin(start)
                ;

        graph.from(start)
                .defaultTo(decide);

        graph.from(decide)
                .to(addTwo).when(s -> s.pass < 2)
                .to(addOne).when(s -> s.pass >= 2 && s.value < 5)
                .defaultTo(subtractOne);

        graph.from(addTwo)
                .defaultTo(checkpoint);

        graph.from(addOne)
                .defaultTo(checkpoint);

        graph.from(checkpoint)
                .to(decide).when(s -> s.value % 2 == 0 && s.pass < 4)
                .defaultTo(subtractOne);

        graph.end(subtractOne);

        GraphExecutor<WorkflowState> executor = graph.done();

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

        GraphDefinitionBuilder<LoopState> graph = new GraphBuilderDefault<LoopState>()
                .options(options("loop-guard"))
                .begin(loop)
                ;

        graph.from(loop)
                .to(loop).when(s -> true);

        graph.end(finish);

        GraphExecutor<LoopState> executor = graph.done();

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

        GraphDefinitionBuilder<ResumeState> graph = new GraphBuilderDefault<ResumeState>()
                .options(options("not-found"))
                .memory(memory)
                .begin(known)
                ;

        graph.end(known);

        GraphExecutor<ResumeState> executor = graph.done();

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

        GraphDefinitionBuilder<WorkflowState> graph = new GraphBuilderDefault<WorkflowState>()
                .options(options("missing-route"))
                .begin(start)
                ;

        graph.end(detachedEnd);

        GraphExecutor<WorkflowState> executor = graph.done();

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

        GraphDefinitionBuilder<WorkflowState> graph = new GraphBuilderDefault<WorkflowState>()
                .options(options("multiple-routes"))
                .begin(start)
                ;

        graph.from(start)
                .to(left).when(s -> s.value > 0)
                .to(right).when(s -> s.value >= 10);

        graph.end(List.of(left, right));

        GraphExecutor<WorkflowState> executor = graph.done();

        GraphRoutingException exception = assertThrows(
                GraphRoutingException.class,
                () -> executor.execute(new WorkflowState(), "multiple-route-session")
        );

        assertTrue(exception.getMessage().contains("Multiple routes found for node 'start'"));
        assertTrue(exception.getMessage().contains("start -> left"));
        assertTrue(exception.getMessage().contains("start -> right"));
    }

    @Test
    void shouldKeepSessionIdAndChangeExecutionIdAfterInterruptAndResume() {
        GraphMemory memory = new InMemoryGraphMemory();

        Node<ResumableState> start = node("start", s -> {
            if (s.getStep() == 0) {
                s.setStep(1);
                s.toInterruptGraph();
                return;
            }
            s.setStep(2);
        });
        Node<ResumableState> end = node("end", s -> s.setStep(3));

        GraphDefinitionBuilder<ResumableState> graph = new GraphBuilderDefault<ResumableState>()
                .options(GraphOptions.builder()
                        .graphName("resume-execution-id")
                        .saveAll(true)
                        .build())
                .memory(memory)
                .begin(start)
                ;

        graph.from(start)
                .defaultTo(end);

        graph.end(end);

        GraphExecutor<ResumableState> executor = graph.done();

        ResumableState firstRunState = executor.execute(new ResumableState(), "resume-session");
        var firstExecutionId = firstRunState.getExecutionId();

        assertEquals(ExecutorStatus.INTERRUPT, firstRunState.getExecutorStatus());
        assertEquals("resume-session", firstRunState.getSessionId());
        assertEquals(1, firstRunState.getStep());

        ResumableState resumedState = executor.execute(new ResumableState(), "resume-session");

        assertEquals(ExecutorStatus.COMPLETED, resumedState.getExecutorStatus());
        assertEquals("resume-session", resumedState.getSessionId());
        assertEquals(3, resumedState.getStep());
        assertNotEquals(firstExecutionId, resumedState.getExecutionId());
    }


    @Test
    void shouldUseSavedStateStrategyDuringResume() {
        GraphMemory memory = memoryWithSavepoint("merge-use-saved", "start", mergeState(10, 1, "saved"));

        Node<MergeState> start = node("start", s -> s.step += 1);
        Node<MergeState> end = node("end", s -> s.step += 1);

        GraphDefinitionBuilder<MergeState> graph = new GraphBuilderDefault<MergeState>()
                .options(options("merge-use-saved"))
                .memory(memory)
                .mergeStrategy(new UseSavedStateStrategy<>())
                .begin(start)
                ;

        graph.from(start)
                .defaultTo(end);

        graph.end(end);

        GraphExecutor<MergeState> executor = graph.done();

        MergeState result = executor.execute(mergeState(999, 5, "incoming"), "merge-use-saved-session");

        assertEquals(10, result.value);
        assertEquals("saved", result.comment);
    }

    @Test
    void shouldUseIncomingStateStrategyDuringResume() {
        GraphMemory memory = memoryWithSavepoint("merge-use-incoming", "start", mergeState(10, 1, "saved"));

        Node<MergeState> start = node("start", s -> s.step += 1);
        Node<MergeState> end = node("end", s -> s.step += 1);

        GraphDefinitionBuilder<MergeState> graph = new GraphBuilderDefault<MergeState>()
                .options(options("merge-use-incoming"))
                .memory(memory)
                .mergeStrategy(new UseIncomingStateStrategy<>())
                .begin(start)
                ;

        graph.from(start)
                .defaultTo(end);

        graph.end(end);

        GraphExecutor<MergeState> executor = graph.done();

        MergeState result = executor.execute(mergeState(999, 5, "incoming"), "merge-use-incoming-session");

        assertEquals(999, result.value);
        assertEquals("incoming", result.comment);
    }

    @Test
    void shouldUseCustomMergeStrategyDuringResume() {
        GraphMemory memory = memoryWithSavepoint("merge-custom", "start", mergeState(10, 1, "saved"));

        Node<MergeState> start = node("start", s -> s.step += 1);
        Node<MergeState> end = node("end", s -> s.step += 1);

        StateMergeStrategy<MergeState> custom = (saved, incoming) -> {
            MergeState merged = new MergeState();
            merged.step = saved.step;
            merged.value = saved.value;
            merged.comment = incoming.comment;
            return merged;
        };

        GraphDefinitionBuilder<MergeState> graph = new GraphBuilderDefault<MergeState>()
                .options(options("merge-custom"))
                .memory(memory)
                .mergeStrategy(custom)
                .begin(start)
                ;

        graph.from(start)
                .defaultTo(end);

        graph.end(end);

        GraphExecutor<MergeState> executor = graph.done();

        MergeState result = executor.execute(mergeState(999, 5, "incoming"), "merge-custom-session");

        assertEquals(10, result.value);
        assertEquals("incoming", result.comment);
    }

    @Test
    void shouldWireMergeStrategyFromGraphSpecificationToExecutor() {
        GraphMemory memory = memoryWithSavepoint("merge-wiring", "start", mergeState(1, 2, "saved"));
        AtomicInteger mergeCalls = new AtomicInteger(0);

        StateMergeStrategy<MergeState> strategy = (saved, incoming) -> {
            mergeCalls.incrementAndGet();
            return incoming;
        };

        Node<MergeState> start = node("start", s -> s.step += 1);
        Node<MergeState> end = node("end", s -> s.step += 1);

        GraphDefinitionBuilder<MergeState> graph = new GraphBuilderDefault<MergeState>()
                .options(options("merge-wiring"))
                .memory(memory)
                .mergeStrategy(strategy)
                .begin(start)
                ;

        graph.from(start)
                .defaultTo(end);

        graph.end(end);

        GraphExecutor<MergeState> executor = graph.done();

        executor.execute(mergeState(2, 4, "incoming"), "merge-wiring-session");

        assertEquals(1, mergeCalls.get());
    }


    private static GraphMemory memoryWithSavepoint(String graphName, String nodeName, GraphState state) {
        return new GraphMemory() {
            @Override
            public void put(SavePoint savePoint) {
            }

            @Override
            public Optional<SavePoint> get(String requestedGraphName, String sessionId) {
                return Optional.of(
                        SavePoint.builder()
                                .graphName(graphName)
                                .sessionId(sessionId)
                                .nodeName(nodeName)
                                .state(state)
                                .build()
                );
            }
        };
    }

    private static MergeState mergeState(int value, int step, String comment) {
        MergeState state = new MergeState();
        state.value = value;
        state.step = step;
        state.comment = comment;
        return state;
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

    private static final class MergeState extends GraphState implements Serializable {
        private int value;
        private int step;
        private String comment;
    }

}
