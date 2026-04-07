package org.graph.api.core.memory;

import org.graph.api.core.ExecutorStatus;
import org.graph.api.core.GraphBuilder;
import org.graph.api.core.SimpleState;
import org.graph.api.core.node.ConsumerNode;
import org.graph.api.core.node.RunnableNode;
import org.graph.api.core.node.SupplierNode;
import org.graph.api.core.node.action.RunnableNodeAction;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphMemoryTest {

    private final GraphOptions options = GraphOptions.builder()
            .graphName("GraphMemoryTest")
            .build();

    private GraphBuilder<SimpleState> graphBuilder;

    private GraphMemory graphMemory;

    SupplierNode<Integer, SimpleState> node1 = new SupplierNode<>(
            "node1",
            (state) -> {
                var i = state.getInput();
                state.setResult(i);
                return i;
            }
    );

    ConsumerNode<Integer, SimpleState> node2 = new ConsumerNode<>(
            "node2",
            (data, state) -> {
                state.setResult(state.getResult() + 1);
                if (state.getResult() == 1) {
                    state.toSave();
                    state.toInterruptGraphCustom();
                }
            }
    );

    RunnableNode<SimpleState> node3 = new RunnableNode<>(
            "node3",
            state -> state.setResult(state.getResult() + 2)
    );

    RunnableNode<SimpleState> node6 = new RunnableNode<>(
            "node6",
            RunnableNodeAction.noop()
    );

    ConsumerNode<Integer, SimpleState> node4 = new ConsumerNode<>(
            "node4",
            (data, state) -> {
                state.setEvenNumber(data % 2 == 0);
                if (!state.isEvenNumber()) {
                    state.toInterruptGraph();
                }
            }
    );

    @BeforeEach
    public void beforeEach() {
        graphMemory = new GraphMemoryDefault();
        graphBuilder = new GraphBuilder<SimpleState>()
                .memory(graphMemory)
                .options(options);
    }

    @Test
    void savePointTest() {
        var executor = graphBuilder
                .begin(node1)
                .route(node1, node2)
                .route(node2, node3)
                .end(node3);

        SimpleState state = new SimpleState();
        state.setEvenNumber(true);
        state.setInput(0);
        state = executor.execute(state, "id");

        assertTrue(graphMemory.get("GraphMemoryTest", state.getSessionId()).isPresent());

        var dataFromSave = graphMemory.get("GraphMemoryTest", state.getSessionId()).get().state();
        GraphStatSaveMapper.merge(dataFromSave, state);

        assertEquals("id", state.getSessionId());

        state = new SimpleState();
        state.setInput(1);
        state = executor.execute(state, "id");

        assertEquals(ExecutorStatus.COMPLETED, state.getExecutorStatus());
        assertEquals(4, state.getResult());
        assertTrue(state.isEvenNumber());
    }

    @Test
    void interruptedTest() {
        var executor = graphBuilder
                .begin(node1)
                .route(node1, node2)
                .route(node2, node6, SimpleState::isInterrupt)
                .route(node2, node3)
                .end(List.of(node3, node6));

        SimpleState state = new SimpleState();
        state.setInput(0);
        executor.execute(state, "id");

        assertEquals(ExecutorStatus.COMPLETED, state.getExecutorStatus());
        assertEquals(1, state.getResult());
    }

    @Test
    void savePointTestWithRedirect() {
        var executor = graphBuilder
                .begin(node1)
                .route(node1, node4)
                .route(node2, node3)
                .route(node1, node2, state -> false)
                .route(node4, node3)
                .end(node3);

        SimpleState state = new SimpleState();
        state.setInput(0);
        state = executor.execute(state, "id");

        assertEquals(ExecutorStatus.COMPLETED, state.getExecutorStatus());
        assertEquals(2, state.getResult());

        state.setInput(1);
        state = executor.execute(state, "id");

        assertEquals(ExecutorStatus.INTERRUPT, state.getExecutorStatus());
        assertEquals(1, state.getResult());

        state.setInput(2);
        state = executor.execute(state, "id");
        assertEquals(ExecutorStatus.COMPLETED, state.getExecutorStatus());
        assertEquals(4, state.getResult());
    }
}
