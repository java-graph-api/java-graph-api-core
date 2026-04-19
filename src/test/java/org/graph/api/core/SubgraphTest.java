package org.graph.api.core;

import lombok.Getter;
import lombok.Setter;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.GraphMemoryDefault;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.graph.api.core.subgraph.InnerSubgraph;
import org.graph.api.core.subgraph.RunnableSubgraph;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SubgraphTest {

    @Test
    void shouldUseNestedGraphNameAsSubgraphNodeName() {
        GraphExecutor<MainState> innerExecutor = new GraphSpecification<MainState>()
                .options(options("inner-name"))
                .begin(node("innerStart", s -> s.trace.add("inner-start")))
                .end(node("innerEnd", s -> s.trace.add("inner-end")));

        GraphExecutor<NestedState> runnableExecutor = new GraphSpecification<NestedState>()
                .options(options("runnable-name"))
                .begin(node("runStart", s -> s.events.add("run-start")))
                .end(node("runEnd", s -> s.events.add("run-end")));

        InnerSubgraph<MainState> innerSubgraph = new InnerSubgraph<>(innerExecutor);
        RunnableSubgraph<MainState, NestedState> runnableSubgraph = new RunnableSubgraph<>(
                state -> new NestedState(state.counter),
                runnableExecutor,
                (state, nested) -> state.counter = nested.value
        );

        assertEquals("inner-name", innerSubgraph.getName());
        assertEquals("runnable-name", runnableSubgraph.getName());
    }

    @Test
    void shouldExecuteInnerSubgraphOnSameStateAndContinueMainGraph() {
        Node<MainState> subStart = node("subStart", s -> {
            s.counter += 2;
            s.trace.add("sub:start");
        });
        Node<MainState> subFinish = node("subFinish", s -> {
            s.counter += 3;
            s.trace.add("sub:finish");
        });

        GraphExecutor<MainState> subExecutor = new GraphSpecification<MainState>()
                .options(options("shared-state-subgraph"))
                .begin(subStart)
                .route(subStart, subFinish)
                .end(subFinish);

        InnerSubgraph<MainState> innerSubgraph = new InnerSubgraph<>(subExecutor);

        Node<MainState> start = node("start", s -> {
            s.counter += 1;
            s.trace.add("main:start");
        });
        Node<MainState> finish = node("finish", s -> {
            s.counter += 10;
            s.trace.add("main:finish");
        });

        GraphExecutor<MainState> mainExecutor = new GraphSpecification<MainState>()
                .options(options("main-with-inner-subgraph"))
                .begin(start)
                .route(start, innerSubgraph)
                .route(innerSubgraph, finish)
                .end(finish);

        MainState result = mainExecutor.execute(new MainState(), "inner-subgraph-session");

        assertEquals(ExecutorStatus.COMPLETED, result.getExecutorStatus());
        assertEquals(16, result.counter);
        assertEquals(
                List.of("main:start", "sub:start", "sub:finish", "main:finish"),
                result.trace
        );
    }

    @Test
    void shouldExecuteRunnableSubgraphWithDedicatedStateAndMergeResultBack() {
        Node<NestedState> nestedStart = node("nestedStart", s -> {
            s.value += 5;
            s.events.add("nested:start");
        });
        Node<NestedState> nestedFinish = node("nestedFinish", s -> {
            s.value *= 2;
            s.events.add("nested:finish");
        });

        GraphExecutor<NestedState> nestedExecutor = new GraphSpecification<NestedState>()
                .options(options("runnable-subgraph"))
                .begin(nestedStart)
                .route(nestedStart, nestedFinish)
                .end(nestedFinish);

        RunnableSubgraph<MainState, NestedState> runnableSubgraph = new RunnableSubgraph<>(
                state -> new NestedState(state.counter),
                nestedExecutor,
                (main, nested) -> {
                    main.counter = nested.value;
                    main.trace.addAll(nested.events);
                }
        );

        Node<MainState> start = node("start", s -> {
            s.counter = 3;
            s.trace.add("main:start");
        });
        Node<MainState> finish = node("finish", s -> s.trace.add("main:finish"));

        GraphExecutor<MainState> mainExecutor = new GraphSpecification<MainState>()
                .options(options("main-with-runnable-subgraph"))
                .begin(start)
                .route(start, runnableSubgraph)
                .route(runnableSubgraph, finish)
                .end(finish);

        MainState result = mainExecutor.execute(new MainState(), "runnable-subgraph-session");

        assertEquals(ExecutorStatus.COMPLETED, result.getExecutorStatus());
        assertEquals(16, result.counter);
        assertEquals(
                List.of("main:start", "nested:start", "nested:finish", "main:finish"),
                result.trace
        );
    }

    @Test
    void shouldResumeInterruptedSubgraphAndThenContinueMainGraphWithSavedState() {
        GraphMemory memory = new GraphMemoryDefault();

        Node<MainState> subStart = node("subStart", s -> {
            s.subStep += 1;
            s.trace.add("sub:start:" + s.subStep);
            s.toSave();
            if (s.subStep == 1) {
                s.toInterruptGraph();
            }
        });
        Node<MainState> subFinish = node("subFinish", s -> {
            s.subStep += 1;
            s.trace.add("sub:finish");
        });

        GraphExecutor<MainState> subExecutor = new GraphSpecification<MainState>()
                .memory(memory)
                .options(GraphOptions.builder().graphName("inner-resume").build())
                .begin(subStart)
                .route(subStart, subFinish)
                .end(subFinish);

        InnerSubgraph<MainState> innerSubgraph = new InnerSubgraph<>(subExecutor);

        Node<MainState> start = node("start", s -> {
            s.counter += 1;
            s.trace.add("main:start");
        });
        Node<MainState> finish = node("finish", s -> {
            s.counter += 100;
            s.trace.add("main:finish");
        });

        GraphExecutor<MainState> mainExecutor = new GraphSpecification<MainState>()
                .memory(memory)
                .options(GraphOptions.builder().graphName("main-resume").build())
                .begin(start)
                .route(start, innerSubgraph)
                .route(innerSubgraph, finish)
                .end(finish);

        MainState firstRun = mainExecutor.execute(new MainState(), "shared-session");

        assertEquals(ExecutorStatus.INTERRUPT, firstRun.getExecutorStatus());
        assertEquals(1, firstRun.subStep);
        assertEquals(1, firstRun.counter);
        assertEquals(List.of("main:start", "sub:start:1"), firstRun.trace);

        MainState secondRun = mainExecutor.execute(new MainState(), "shared-session");

        assertEquals(ExecutorStatus.COMPLETED, secondRun.getExecutorStatus());
        assertEquals(3, secondRun.subStep);
        assertEquals(101, secondRun.counter);
        assertEquals(
                List.of("main:start", "sub:start:1", "sub:start:2", "sub:finish", "main:finish"),
                secondRun.trace
        );
        assertNotEquals(firstRun.getExecutionId(), secondRun.getExecutionId());
    }

    private static GraphOptions options(String name) {
        return GraphOptions.builder()
                .graphName(name)
                .nodeCallLimit(100)
                .build();
    }

    private static <S extends GraphState> Node<S> node(String name, Consumer<S> action) {
        return new TestNode<>(name, action);
    }

    private static final class TestNode<S extends GraphState> implements Node<S> {
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

    @Setter
    @Getter
    public static final class MainState extends GraphState implements Serializable {
        private int counter;
        private int subStep;
        private List<String> trace = new ArrayList<>();

    }

    @Setter
    @Getter
    public static final class NestedState extends GraphState implements Serializable {
        private int value;
        private List<String> events = new ArrayList<>();

        private NestedState(int value) {
            this.value = value;
        }

    }
}
