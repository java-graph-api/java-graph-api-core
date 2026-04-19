package org.graph.api.core;

import org.graph.api.core.exception.GraphRoutingException;
import org.graph.api.core.memory.GraphMemoryDefault;
import org.graph.api.core.node.ConsumerNode;
import org.graph.api.core.node.SupplierNode;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GraphRoutingTest {

    private GraphSpecification<SimpleState> graphSpecification;

    @BeforeEach
    public void setup() {
        var options = GraphOptions.builder()
                .graphName("GraphRoutingTest")
                .build();

        graphSpecification = new GraphSpecification<SimpleState>()
                .memory(new GraphMemoryDefault())
                .options(options);
    }

    SupplierNode<Integer, SimpleState> node1 = new SupplierNode<>(
            "node1",
            (state) -> {
                var data = state.getInput();
                state.setEvenNumber(data % 2 == 0);
                return data;
            }
    );

    ConsumerNode<Integer, SimpleState> node2 = new ConsumerNode<>(
            "node2",
            (data, state) -> state.setResult(data + 1)
    );

    ConsumerNode<Integer, SimpleState> node3 = new ConsumerNode<>(
            "node3",
            (data, state) -> state.setResult(0)
    );

    SupplierNode<Integer, SimpleState> node4 = new SupplierNode<>(
            "node4",
            state -> {
                state.setEvenNumber(state.getResult() % 2 == 0);
                return 0;
            }
    );

    @ParameterizedTest
    @MethodSource("getNumbers")
    void routingTest(int number) {
        var executor = graphSpecification
                .begin(node1)
                .route(node1, node2, state -> !state.isEvenNumber())
                .route(node2, node4)
                .route(node4, node3, (output, state) -> output % 2 == 0)
                .route(node1, node3)
                .end(node3);

        SimpleState state = new SimpleState();
        state.setInput(number);
        var result = executor.execute(state, "id");

        assertEquals(0, result.getResult());
        assertEquals(ExecutorStatus.COMPLETED, state.getExecutorStatus());
    }

    private static IntStream getNumbers() {
        return IntStream.rangeClosed(0, 10);
    }

    @Test
    void routeNotFoundTest() {
        var exception = assertThrows(
                GraphRoutingException.class,
                () -> graphSpecification
                        .begin(node1)
                        .route(node2, node4)
                        .route(node4, node3, (output, state) -> output % 2 == 0)
                        .route(node1, node3, SimpleState::isEvenNumber)
                        .end(node3)
        );

        assertEquals("Route for node 'node4' not found", exception.getMessage());
    }

    @Test
    void multipleRoutesFoundTest() {
        var exception = assertThrows(
                GraphRoutingException.class,
                () -> {
                    var executor = graphSpecification
                            .begin(node1)
                            .route(node1, node3, state -> !state.isEvenNumber())
                            .route(node1, node2, state -> !state.isEvenNumber())
                            .route(node2, node4)
                            .route(node4, node3, (output, state) -> output % 2 == 0)
                            .end(node3);
                    SimpleState state = new SimpleState();
                    state.setInput(1);
                    executor.execute(state, "id");
                }
        );

        assertEquals("Multiple routes found for node 'node1': [node1 -> node3, node1 -> node2]", exception.getMessage());
    }
}
