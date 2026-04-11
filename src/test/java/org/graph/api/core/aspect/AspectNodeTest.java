package org.graph.api.core.aspect;

import org.graph.api.core.ExecutorStatus;
import org.graph.api.core.GraphSpecification;
import org.graph.api.core.GraphState;
import org.graph.api.core.SimpleState;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.GraphMemoryDefault;
import org.graph.api.core.model.IntegerSimpleStateNode;
import org.graph.api.core.node.ConsumerNode;
import org.graph.api.core.node.SupplierNode;
import org.graph.api.core.node.RunnableNode;
import org.graph.api.core.node.factory.NodeProxyFactory;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AspectNodeTest {

    private GraphMemory memory;

    GraphOptions options = GraphOptions.builder()
            .graphName("GraphCompileTest")
            .build();

    @BeforeEach
    public void setup() {
        memory = new GraphMemoryDefault();
    }

    SupplierNode<Integer, SimpleState> node1 = new SupplierNode<>(
            "node1",
            (state) -> state.getInput() * 2
    );

    IntegerSimpleStateNode node2 = new IntegerSimpleStateNode(
            "node2",
            (input, state) -> input * 3
    );

    ConsumerNode<Integer, SimpleState> node3 = new ConsumerNode<>(
            "node3",
            (input, state) -> {
                var result = input * 4;
                state.setResult(state.getResult() + result);
            }
    );

    NodeAspect<SimpleState> aspect = new NodeAspect<>() {

        @Override
        public void before(JoinPoint<SimpleState> joinPoint, Object input) {
            if (joinPoint.getState().getResult() == null) {
                joinPoint.getState().setResult(1);
            }
            joinPoint.getState().setResult(joinPoint.getState().getResult() - 6);
        }

        @Override
        public void after(JoinPoint<SimpleState> joinPoint, Object result) {
            joinPoint.getState().setResult(joinPoint.getState().getResult() + 4);
        }

        @Override
        public Object around(ProcessingJoinPoint<SimpleState> processingJoinPoint, Object input) {
            processingJoinPoint.getState().setResult(processingJoinPoint.getState().getResult() + 10);
            var result = processingJoinPoint.action();
            result = result == null ? null : ((int) result) - 8;
            processingJoinPoint.getState().setResult(processingJoinPoint.getState().getResult() - 20);
            return result;
        }

        @Override
        public int getOrder() {
            return 1;
        }
    };

    final AtomicInteger counter = new AtomicInteger(0);

    NodeAspect<GraphState> aspectDefault = new NodeAspect<>() {

        @Override
        public void before(JoinPoint<GraphState> joinPoint, Object input) {
            counter.incrementAndGet();
        }

        @Override
        public void after(JoinPoint<GraphState> joinPoint, Object result) {
            counter.incrementAndGet();
        }

        @Override
        public Object around(ProcessingJoinPoint<GraphState> processingJoinPoint, Object input) {
            counter.incrementAndGet();
            var result = processingJoinPoint.action();
            counter.incrementAndGet();
            return result;
        }

        @Override
        public int getOrder() {
            return 2;
        }
    };

    @Test
    void aspectModifierTest() {
        GraphSpecification<SimpleState> graphSpecification = new GraphSpecification<SimpleState>()
                .memory(memory)
                .options(options)
                .aspects(List.of(aspect, aspectDefault, new LoggingAspect()));

        var graphExecutor = graphSpecification
                .begin(node1)
                .route(node1, node2)
                .route(node2, node3)
                .end(node3);

        SimpleState simpleState = new SimpleState();
        simpleState.setInput(1);
        var result = graphExecutor.execute(simpleState, "ID");

        int expectedValueOfState = 1;
        int expected = 1;
        for (int i = 2; i < 5; i++) {
            expected = expected * i - 8;
            expectedValueOfState = expectedValueOfState - 6 + 4 + 10 - 20;
        }
        expected = expected + expectedValueOfState + 8;

        assertEquals(expected, result.getResult());
        assertEquals(12, counter.get());
        assertEquals(ExecutorStatus.COMPLETED, result.getExecutorStatus());
    }

    @Test
    void aspectNodeTest() {
        List<Integer> result = new ArrayList<>();

        GraphOptions options = GraphOptions.builder()
                .graphName("GraphCompileTest")
                .build();

        RunnableNode<SimpleState> node1 = new RunnableNode<>(
                "node1",
                state -> result.add(2)
        );

        NodeAspect<SimpleState> aspect = new NodeAspect<>() {

            @Override
            public void before(JoinPoint<SimpleState> joinPoint, Object input) {
                result.add(0);
            }

            @Override
            public void after(JoinPoint<SimpleState> joinPoint, Object result1) {
                result.add(4);
            }

            @Override
            public Object around(ProcessingJoinPoint<SimpleState> processingJoinPoint, Object input) {
                result.add(1);
                var nodeResult = processingJoinPoint.action();
                result.add(3);
                return nodeResult;
            }
        };

        NodeProxyFactory<SimpleState> proxyFactory = new NodeProxyFactory<>(options);

        var nodeProxy = proxyFactory.createProxy(node1, List.of(aspect));
        nodeProxy.call(null, new SimpleState());

        for (int i = 0; i <= 4; i++) {
            assertEquals(result.get(i), i);
        }
    }
}
