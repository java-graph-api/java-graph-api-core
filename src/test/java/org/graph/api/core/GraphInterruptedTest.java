package org.graph.api.core;

import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.GraphMemoryDefault;
import org.graph.api.core.node.RunnableNode;
import org.graph.api.core.node.action.RunnableNodeAction;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphInterruptedTest {

    private final GraphMemory memory = new GraphMemoryDefault();

    GraphOptions options = GraphOptions.builder()
            .graphName("GraphInterruptedTest")
            .build();

    RunnableNode<SimpleState> node1 = new RunnableNode<>(
            "node1",
            state -> {
                state.setResult(state.getInputWithoutSave() + 1);
                state.toSave();
            }
    );

    RunnableNode<SimpleState> node2 = new RunnableNode<>(
            "node2",
            GraphState::toInterruptGraph
    );

    RunnableNode<SimpleState> node3 = new RunnableNode<>(
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
