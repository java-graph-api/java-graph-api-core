package org.graph.api.core;

import org.graph.api.core.memory.GraphMemoryDefault;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphMemoryTest {

    @Test
    void shouldResumeFromSavePointUsingSameSessionAndMergeState() {
        GraphMemoryDefault memory = new GraphMemoryDefault();

        Node<MemoryState> start = node("start", s -> {
            s.value += 1;
            s.trace.add("start");
        });
        Node<MemoryState> checkpoint = node("checkpoint", s -> {
            s.value += 10;
            s.trace.add("checkpoint");
            if (!s.pauseDone) {
                s.pauseDone = true;
                s.toSave();
                s.toInterruptGraph();
            }
        });
        Node<MemoryState> finish = node("finish", s -> {
            s.value += 5;
            s.trace.add("finish");
        });

        GraphExecutor<MemoryState> executor = new GraphSpecification<MemoryState>()
                .options(options("memory-resume"))
                .memory(memory)
                .begin(start)
                .route(start, checkpoint)
                .route(checkpoint, finish)
                .end(finish);

        MemoryState first = new MemoryState();
        MemoryState interrupted = executor.execute(first, "session-a");

        assertEquals(ExecutorStatus.INTERRUPT, interrupted.getExecutorStatus());
        assertEquals(11, interrupted.value);
        assertEquals(List.of("start", "checkpoint"), interrupted.trace);

        MemoryState second = new MemoryState();
        second.value = 999;
        second.trace.add("external-payload");

        MemoryState resumed = executor.execute(second, "session-a");

        assertEquals(ExecutorStatus.COMPLETED, resumed.getExecutorStatus());
        assertEquals(26, resumed.value);
        assertEquals(List.of("start", "checkpoint", "checkpoint", "finish"), resumed.trace);
        assertTrue(resumed.pauseDone);
    }

    @Test
    void shouldKeepSessionsIsolatedInMemory() {
        GraphMemoryDefault memory = new GraphMemoryDefault();

        Node<MemoryState> start = node("start", s -> s.value += 1);
        Node<MemoryState> checkpoint = node("checkpoint", s -> {
            s.value += 2;
            if (!s.pauseDone) {
                s.pauseDone = true;
                s.toSave();
                s.toInterruptGraph();
            }
        });
        Node<MemoryState> finish = node("finish", s -> s.value += 100);

        GraphExecutor<MemoryState> executor = new GraphSpecification<MemoryState>()
                .options(options("memory-sessions"))
                .memory(memory)
                .begin(start)
                .route(start, checkpoint)
                .route(checkpoint, finish)
                .end(finish);

        MemoryState interruptedA = executor.execute(new MemoryState(), "A");
        MemoryState interruptedB = executor.execute(new MemoryState(), "B");

        assertEquals(3, interruptedA.value);
        assertEquals(3, interruptedB.value);
        assertEquals(ExecutorStatus.INTERRUPT, interruptedA.getExecutorStatus());
        assertEquals(ExecutorStatus.INTERRUPT, interruptedB.getExecutorStatus());

        MemoryState resumedA = executor.execute(new MemoryState(), "A");
        MemoryState resumedB = executor.execute(new MemoryState(), "B");

        assertEquals(105, resumedA.value);
        assertEquals(105, resumedB.value);
        assertEquals(ExecutorStatus.COMPLETED, resumedA.getExecutorStatus());
        assertEquals(ExecutorStatus.COMPLETED, resumedB.getExecutorStatus());
    }

    private static GraphOptions options(String name) {
        return GraphOptions.builder().graphName(name).nodeCallLimit(100).build();
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

    private static final class MemoryState extends GraphState implements Serializable {
        private int value;
        private boolean pauseDone;
        private final List<String> trace = new ArrayList<>();
    }
}
