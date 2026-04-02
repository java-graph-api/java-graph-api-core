package org.graph.api.core;

import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.GraphMemoryDefault;
import org.graph.api.core.node.ConsumerNode;
import org.graph.api.core.node.SupplierNode;
import org.graph.api.core.node.UnaryNode;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphInterruptedTest {

    private GraphMemory memory;

    @BeforeEach
    public void setup() {
        memory = new GraphMemoryDefault();
    }

    GraphOptions options = GraphOptions.builder()
            .graphName("GraphCompileTest")
            .build();

    SupplierNode<Integer, SimpleState> node1 = new SupplierNode<>(
            "node1",
            (state) -> {
                var data = state.getInput();
                state.setEvenNumber(data % 2 == 0);
                return data;
            }
    );

    UnaryNode<Integer, SimpleState> node11 = new UnaryNode<>(
            "node11",
            (data, state) -> {
                state.setEvenNumber(data % 2 == 0);
                return data;
            }
    );

    UnaryNode<Integer, SimpleState> node2 = new UnaryNode<>(
            "node2",
            (data, state) -> {
                state.setEvenNumber(data % 2 == 0);
                state.toInterruptGraph();
                return data;
            }
    );

    ConsumerNode<Integer, SimpleState> node3 = new ConsumerNode<>(
            "node3",
            (data, state) -> state.setResult(data + 1)
    );

    @Test
    void interruptedTest() {

        var graph = new GraphBuilder<SimpleState>()
                .options(options)
                .memory(memory)
                .begin(node1)
                .route(node11, node2)
                .route(node1, node2)
                .route(node2, node3, SimpleState::isInterrupt)
                .route(node2, node11)
                .end(node3);

        var state = new SimpleState();
        state.setInput(0);
        state = graph.execute(state, "id");

        assertEquals(ExecutorStatus.COMPLETED, state.getExecutorStatus());
    }
}
