package org.graph.api.core.memory;

import org.graph.api.core.*;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphMemoryTest {

    @Test
    void shouldResumeFromSavePointUsingSameSessionAndMergeState() {
        InMemoryGraphMemory memory = new InMemoryGraphMemory();

        Node<TestMemoryState> start = node("start", s -> {
            s.setValue(s.getValue() + 1);
            s.getTrace().add("start");
        });
        Node<TestMemoryState> checkpoint = node("checkpoint", s -> {
            s.setValue(s.getValue() + 10);
            s.getTrace().add("checkpoint");
            if (!s.isPauseDone()) {
                s.setPauseDone(true);
                s.toSave();
                s.toInterruptGraph();
            }
        });
        Node<TestMemoryState> finish = node("finish", s -> {
            s.setValue(s.getValue() + 5);
            s.getTrace().add("finish");
        });

        GraphExecutor<TestMemoryState> executor = new GraphSpecification<TestMemoryState>()
                .options(options("memory-resume"))
                .memory(memory)
                .begin(start)
                .route(start, checkpoint)
                .route(checkpoint, finish)
                .end(finish);

        TestMemoryState first = new TestMemoryState();
        TestMemoryState interrupted = executor.execute(first, "session-a");

        Assertions.assertEquals(ExecutorStatus.INTERRUPT, interrupted.getExecutorStatus());
        assertEquals(11, interrupted.getValue());
        assertEquals(List.of("start", "checkpoint"), interrupted.getTrace());

        TestMemoryState second = new TestMemoryState();
        second.setValue(999);
        second.getTrace().add("external-payload");

        TestMemoryState resumed = executor.execute(second, "session-a");

        assertEquals(ExecutorStatus.COMPLETED, resumed.getExecutorStatus());
        assertEquals(26, resumed.getValue());
        assertEquals(List.of("start", "checkpoint", "checkpoint", "finish"), resumed.getTrace());
        assertTrue(resumed.isPauseDone());
    }


    @Test
    void shouldStoreSavePointInGraphMemoryDefaultWhenNodeCallsToSave() {
        InMemoryGraphMemory memory = new InMemoryGraphMemory();

        Node<TestMemoryState> start = node("start", s -> s.setValue(1));
        Node<TestMemoryState> saveHere = node("save-here", s -> {
            s.setValue(s.getValue() + 41);
            s.toSave();
            s.toInterruptGraph();
        });
        Node<TestMemoryState> finish = node("finish", s -> s.setValue(999));

        GraphExecutor<TestMemoryState> executor = new GraphSpecification<TestMemoryState>()
                .options(options("memory-save-check"))
                .memory(memory)
                .begin(start)
                .route(start, saveHere)
                .route(saveHere, finish)
                .end(finish);

        TestMemoryState result = executor.execute(new TestMemoryState(), "session-save");

        assertEquals(ExecutorStatus.INTERRUPT, result.getExecutorStatus());
        var savePoint = memory.get("memory-save-check", "session-save").orElseThrow();

        assertEquals("memory-save-check", savePoint.graphName());
        assertEquals("save-here", savePoint.nodeName());
        assertEquals("session-save", savePoint.sessionId());
        TestMemoryState savedState = (TestMemoryState) savePoint.state();
        assertEquals(TestMemoryState.class, savedState.getClass());
        assertEquals(42, savedState.getValue());
    }

    @Test
    void shouldKeepSessionsIsolatedInMemory() {
        InMemoryGraphMemory memory = new InMemoryGraphMemory();

        Node<TestMemoryState> start = node("start", s -> s.setValue(s.getValue() + 1));
        Node<TestMemoryState> checkpoint = node("checkpoint", s -> {
            s.setValue(s.getValue() + 2);
            if (!s.isPauseDone()) {
                s.setPauseDone(true);
                s.toSave();
                s.toInterruptGraph();
            }
        });
        Node<TestMemoryState> finish = node("finish", s -> s.setValue(s.getValue() + 100));

        GraphExecutor<TestMemoryState> executor = new GraphSpecification<TestMemoryState>()
                .options(options("memory-sessions"))
                .memory(memory)
                .begin(start)
                .route(start, checkpoint)
                .route(checkpoint, finish)
                .end(finish);

        TestMemoryState interruptedA = executor.execute(new TestMemoryState(), "A");
        TestMemoryState interruptedB = executor.execute(new TestMemoryState(), "B");

        assertEquals(3, interruptedA.getValue());
        assertEquals(3, interruptedB.getValue());
        assertEquals(ExecutorStatus.INTERRUPT, interruptedA.getExecutorStatus());
        assertEquals(ExecutorStatus.INTERRUPT, interruptedB.getExecutorStatus());

        TestMemoryState resumedA = executor.execute(new TestMemoryState(), "A");
        TestMemoryState resumedB = executor.execute(new TestMemoryState(), "B");

        assertEquals(105, resumedA.getValue());
        assertEquals(105, resumedB.getValue());
        assertEquals(ExecutorStatus.COMPLETED, resumedA.getExecutorStatus());
        assertEquals(ExecutorStatus.COMPLETED, resumedB.getExecutorStatus());
    }

    private static GraphOptions options(String name) {
        return GraphOptions.builder().graphName(name).nodeInvocationLimit(100).build();
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

}
