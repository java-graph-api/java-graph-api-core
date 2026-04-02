package org.graph.api.core.node.guard;

import org.graph.api.core.GraphBuilder;
import org.graph.api.core.options.GraphOptions;
import org.graph.api.core.SimpleState;
import org.graph.api.core.exception.TooManyNodeCallException;
import org.graph.api.core.node.RunnableNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeCallGuardTest {

    private static final int CALL_LIMIT = 50;

    RunnableNode<SimpleState> node1 = new RunnableNode<>(
            "1",
            SimpleState::countIncrement
    );

    RunnableNode<SimpleState> node2 = new RunnableNode<>(
            "2",
            SimpleState::countIncrement,
            CALL_LIMIT
    );

    @Test
    void nodeCallCounterTest() {
        var options = GraphOptions.builder()
                .graphName("NodeCallCounterTest")
                .build();

        var graph = new GraphBuilder<SimpleState>()
                .options(options)
                .begin(node1)
                .route(node1, node1, state -> !state.isEvenNumber())
                .route(node1, node2)
                .end(node2);

        SimpleState state = new SimpleState();
        var ex = Assertions.assertThrows(TooManyNodeCallException.class, () -> graph.execute(state, "id"));

        assertEquals(String.format("Too many node call. Node '%s' calls: %s", node1.getName(), options.getNodeCallLimit() + 1), ex.getMessage());
        assertEquals(state.getCounter(), options.getNodeCallLimit());

        var graph2 = new GraphBuilder<SimpleState>()
                .options(options)
                .begin(node2)
                .route(node2, node2, s -> !s.isEvenNumber())
                .route(node2, node1)
                .end(node1);

        SimpleState state2 = new SimpleState();
        ex = Assertions.assertThrows(TooManyNodeCallException.class, () -> graph2.execute(state2, "id"));

        assertEquals(String.format("Too many node call. Node '%s' calls: %s", node2.getName(), state2.getCounter() + 1), ex.getMessage());
        assertEquals(CALL_LIMIT, state2.getCounter());
    }
}
