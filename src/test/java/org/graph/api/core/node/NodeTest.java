package org.graph.api.core.node;

import org.graph.api.core.SimpleState;
import org.graph.api.core.GraphState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeTest {

    @Test
    void completableNode() {
        ConsumerNode<Integer, SimpleState> consumerNode = new ConsumerNode<>(
                "completableNode",
                (i, state) -> state.setNumber(i * 2)

        );

        SimpleState state = new SimpleState();
        consumerNode.call(5, state);
        assertEquals(10, state.getNumber());
    }

    @Test
    void producerNode() {
        SupplierNode<Integer, GraphState> supplierNode = new SupplierNode<>(
                "producerNode",
                state -> 123

        );

        assertEquals(123, supplierNode.call(null, null));
    }

    @Test
    void unaryNode() {
        UnaryNode<Integer, GraphState> unaryNode = new UnaryNode<>(
                "unaryNode",
                (input, state) -> input * 2
        );

        assertEquals(4, unaryNode.call(2, null));
    }

    @Test
    void typeNode() {
        FunctionalNode<Integer, String, GraphState> functionalNode = new FunctionalNode<>(
                "TypeNode",
                (input, state) -> String.valueOf(input)
        );

        assertEquals("20", functionalNode.call(20, null));
    }

    @Test
    void statelessNode() {
        RunnableNodeImpl<SimpleState> runnableNode = new RunnableNodeImpl<>(
                "statelessNode",
                state -> state.setConditional(true)
        );

        SimpleState state = new SimpleState();
        runnableNode.call(null, state);
        assertTrue(state.isConditional());
    }
}
