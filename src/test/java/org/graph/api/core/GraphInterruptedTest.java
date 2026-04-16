package org.graph.api.core;

import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.GraphMemoryDefault;
import org.graph.api.core.node.RunnableNodeImpl;
import org.graph.api.core.node.action.RunnableNodeAction;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphInterruptedTest {

    private final GraphMemory memory = new GraphMemoryDefault();

    GraphOptions options = GraphOptions.builder()
            .graphName("GraphInterruptedTest")
            .build();

    RunnableNodeImpl<SimpleState> node1 = new RunnableNodeImpl<>(
            "node1",
            state -> {
                state.setResult(state.getInputWithoutSave() + 1);
                state.toSave();
            }
    );

    RunnableNodeImpl<SimpleState> node2 = new RunnableNodeImpl<>(
            "node2",
            GraphState::toInterruptGraph
    );

    RunnableNodeImpl<SimpleState> node3 = new RunnableNodeImpl<>(
            "node3",
            RunnableNodeAction.noop()
    );

    @Test
    void interruptedTest() {

        var graph = new GraphSpecification<SimpleState>()
                .options(options)
                .memory(memory)
                .begin(node1)
                .route(node1, node2, state -> state.getResult() == 1)
                .route(node1, node3, state -> state.getResult() == 2)
                .end(node3);

        var state = new SimpleState();

        state.setInputWithoutSave(0);
        state = graph.execute(state, "id");
        assertEquals(ExecutorStatus.INTERRUPT, state.getExecutorStatus());

        state.setInputWithoutSave(1);
        state = graph.execute(state, "id");
        assertEquals(ExecutorStatus.COMPLETED, state.getExecutorStatus());
    }
}
