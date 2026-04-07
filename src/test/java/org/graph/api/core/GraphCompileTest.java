package org.graph.api.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.graph.api.core.aspect.JoinPoint;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.GraphMemoryDefault;
import org.graph.api.core.node.*;
import org.graph.api.core.node.action.RunnableNodeAction;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GraphCompileTest {

    private GraphMemory memory;

    @BeforeEach
    public void setup() {
        memory = new GraphMemoryDefault();
    }

    @Test
    void compileTest() {

        RunnableNode<SimpleState> node1 = new RunnableNode<>(
                "node1",
                state -> state.setResult(4)
        );

        RunnableNode<SimpleState> node2 = new RunnableNode<>(
                "node2",
                state -> state.setResult(2)
        );

        RunnableNode<SimpleState> node3 = new RunnableNode<>(
                "node3",
                state -> state.setResult(3)
        );

        GraphOptions options = GraphOptions.builder()
                .graphName("GraphCompileTest")
                .build();

        var graph = new GraphBuilder<SimpleState>()
                .options(options)
                .memory(memory)
                .begin(node1)
                .route(node1, node2)
                .route(node2, node3)
                .end(node3);

        SimpleState state = new SimpleState();
        graph.execute(state, "id");
    }

    @ParameterizedTest
    @MethodSource("numGen")
    void manyEndNode(int number) {
        GraphOptions options = GraphOptions.builder()
                .graphName("GraphCompileTest")
                .build();

        RunnableNode<SimpleState> node1 = new RunnableNode<>(
                "node1",
                (state) -> state.setNumber(state.getInput())
        );

        RunnableNode<SimpleState> node2 = new RunnableNode<>(
                "node2",
                state -> state.setResult(2)
        );

        RunnableNode<SimpleState> node3 = new RunnableNode<>(
                "node3",
                state -> state.setResult(3)
        );

        RunnableNode<SimpleState> node4 = new RunnableNode<>(
                "node4",
                state -> state.setResult(4)
        );

        RunnableNode<SimpleState> node5 = new RunnableNode<>(
                "node5",
                RunnableNodeAction.noop()
        );

        var graph = new GraphBuilder<SimpleState>()
                .options(options)
                .memory(memory)
                .begin(node1)
                .route(node1, node2, state -> state.getNumber() == 2)
                .route(node1, node3, state -> state.getNumber() == 3)
                .route(node1, node4, state -> state.getNumber() == 4)
                .route(node1, node5)
                .end(List.of(node2, node3, node4, node5));

        SimpleState state = new SimpleState();
        state.setInput(number);
        graph.execute(state, "id");

        assertEquals(ExecutorStatus.COMPLETED, state.getExecutorStatus());
        assertEquals(number, state.getResult());
    }

    static IntStream numGen() {
        return IntStream.rangeClosed(2, 4);
    }


    @Test
    void simple2() {
        SupplierNode<String, SimpleState> node1 = new SupplierNode<>(
                "node1",
                (state) -> "node1"
        );

        FunctionalNode<String, String, SimpleState> node2 = new FunctionalNode<>(
                "node2",
                (input, state) -> input + "node2"
        );

        ConsumerNode<String, SimpleState> node3 = new ConsumerNode<>(
                "node3",
                (input, state) -> {
                    if (input.equals("node1node2")) {
                        state.setConditional(true);
                    }
                }
        );

        var options = GraphOptions.builder()
                .graphName("GraphCompileTest")
                .build();

        var graph = new GraphBuilder<SimpleState>()
                .options(options)
                .memory(memory)
                .begin(node1)
                .route(node1, node2)
                .route(node2, node3)
                .end(node3);

        SimpleState state = new SimpleState();

        graph.execute(state, "id");

        assertTrue(state.isConditional());
        assertEquals(ExecutorStatus.COMPLETED, state.getExecutorStatus());
    }

    private static class StepCounter implements NodeAspect<UserState> {
        @Override
        public void after(JoinPoint<UserState> joinPoint, Object input) {
            joinPoint.getState().setAmountSteps(joinPoint.getState().getAmountSteps() + 1);
        }
    }

    @Data
    @AllArgsConstructor
    private static class User {
        private String name;
        private Integer age;
    }

    @Getter
    @Setter
    private static class UserState extends GraphState {
        private User input;
        private boolean notNull = true;
        private boolean student;
        private final Map<String, List<User>> userByClass = new HashMap<>();
        private int amountStudent;
        private int amountNotStudent;
        private boolean correct;
        private int size;
        private int targetSize;
        private Map<String, Integer> amountCallNode = new HashMap<>();
        private boolean spy;
        private int amountSteps;
        private boolean interrupt;

        public void toInterruptGraphCustom() {
            interrupt = true;
        }
    }

    static SupplierNode<User, UserState> validation = new SupplierNode<>(
            "validationNotNullNode",
            (state) -> {
                var input = state.getInput();
                state.setSize(state.getSize() + 1);
                state.setNotNull(input.getName() != null && input.getAge() != null);
                return input;
            }
    );

    static ConsumerNode<User, UserState> notValidHandler = new ConsumerNode<>(
            "notValidHandler",
            (input, state) -> {
                throw new RuntimeException("input data is not valid");
            }
    );

    static UnaryNode<User, UserState> classificator = new UnaryNode<>(
            "classificator",
            (input, state) -> {
                state.setStudent(input.getAge() < 19);
                return input;
            }
    );

    static ConsumerNode<User, UserState> setStudent = new ConsumerNode<>(
            "setUsers",
            (input, state) -> state.getUserByClass().computeIfAbsent("student", v -> new ArrayList<>()).add(input)
    );

    static ConsumerNode<User, UserState> setNotStudent = new ConsumerNode<>(
            "setNotStudent",
            (input, state) -> state.getUserByClass().computeIfAbsent("notStudent", v -> new ArrayList<>()).add(input)
    );

    static RunnableNode<UserState> checkForInterrupt = new RunnableNode<>(
            "checkForInterrupt",
            state -> {
                if (state.size < state.getTargetSize()) {
                    state.toInterruptGraphCustom();
                } else {
                    state.setInterrupt(false);
                }
            }
    );

    static RunnableNode<UserState> spyNode = new RunnableNode<>(
            "spyNode",
            state -> state.getUserByClass()
                    .computeIfAbsent("student", v -> new ArrayList<>())
                    .add(new User("spy", 100))
    );

    static RunnableNode<UserState> amountCalc = new RunnableNode<>(
            "amountCalc",
            state -> {
                state.setAmountStudent(state.getUserByClass().get("student").size());
                state.setAmountNotStudent(state.getUserByClass().get("notStudent").size());
            }
    );

    static SupplierNode<Integer, UserState> amountAllCalc = new SupplierNode<>(
            "amountAllCalc",
            state -> state.getAmountStudent() + state.getAmountNotStudent()
    );

    static FunctionalNode<Integer, Boolean, UserState> checkCorrectCalc = new FunctionalNode<>(
            "checkCorrectCalc",
            (input, state) -> state.getTargetSize() == state.getUserByClass().values().stream()
                    .mapToInt(List::size)
                    .sum()
    );

    static ConsumerNode<Boolean, UserState> correctHandler = new ConsumerNode<>(
            "correctHandler",
            (input, state) -> state.setCorrect(input)
    );

    static RunnableNode<UserState> errorCorrectHandler = new RunnableNode<>(
            "errorCorrectHandler",
            state -> {
                throw new RuntimeException("result is not correct");
            }
    );

    static RunnableNode<UserState> finish = new RunnableNode<>(
            "finish",
            RunnableNodeAction.noop()
    );

    @ParameterizedTest
    @MethodSource("provideMixedArguments")
    void simple2(List<User> userList, BiConsumer<Boolean, List<User>> runner, boolean spy) {
        runner.accept(spy, userList);
    }

    static Stream<Arguments> provideMixedArguments() {
        return Stream.of(
                Arguments.of(
                        List.of(new User("user1", 20), new User("user2", 15)),
                        (BiConsumer<Boolean, List<User>>) (spy, users) -> {
                            var state = graphCall.apply(spy, users);
                            assertTrue(state.isCorrect());
                            assertEquals(14, state.getAmountSteps());
                            assertEquals(ExecutorStatus.COMPLETED, state.getExecutorStatus());
                        },
                        false
                ),
                Arguments.of(
                        List.of(new User(null, 20), new User("user2", 15)),
                        (BiConsumer<Boolean, List<User>>) (spy, users) -> {
                            var ex = Assertions.assertThrows(
                                    RuntimeException.class,
                                    () -> graphCall.apply(spy, users)
                            );
                            assertEquals("input data is not valid", ex.getMessage());
                        },
                        false
                ),
                Arguments.of(
                        List.of(new User("user1", 20), new User("user2", 15)),
                        (BiConsumer<Boolean, List<User>>) (spy, users) -> {
                            var ex = Assertions.assertThrows(
                                    RuntimeException.class,
                                    () -> graphCall.apply(spy, users)
                            );
                            assertEquals("result is not correct", ex.getMessage());
                        },
                        true
                )
        );
    }

    static BiFunction<Boolean, List<User>, UserState> graphCall = (spy, users) -> {
        var options = GraphOptions.builder()
                .graphName("GraphCompileTest")
                .build();

        var graph = new GraphBuilder<UserState>()
                .options(options)
                .aspect(new StepCounter())
                .memory(new GraphMemoryDefault())
                .begin(validation)
                .route(validation, notValidHandler, state -> !state.isNotNull())
                .route(validation, classificator)
                .route(classificator, setStudent, UserState::isStudent)
                .route(classificator, setNotStudent)
                .route(setStudent, checkForInterrupt)
                .route(setNotStudent, checkForInterrupt)
                .route(checkForInterrupt, finish, state -> state.isInterrupt() && !state.isSpy())
                .route(checkForInterrupt, spyNode, UserState::isSpy)
                .route(checkForInterrupt, amountCalc)
                .route(spyNode, amountCalc)
                .route(amountCalc, amountAllCalc)
                .route(amountAllCalc, checkCorrectCalc)
                .route(checkCorrectCalc, correctHandler)
                .route(correctHandler, errorCorrectHandler, state -> !state.isCorrect())
                .route(correctHandler, finish)
                .end(finish);

        UserState state = new UserState();
        state.setTargetSize(users.size());
        state.setSpy(spy);

        for (User user : users) {
            state.setInput(user);
            graph.execute(state, "id");
        }

        return state;
    };

    @Test
    void largeGraphBuilderTest() {
        var options = GraphOptions.builder()
                .graphName("LargeGraphBuilderTest")
                .build();

        var beginNode = new IncrementNodeBegin();

        var routeSpec = new GraphBuilder<SimpleState>()
                .options(options)
                .begin(beginNode);

        var node = new IncrementNode(1);
        routeSpec.route(beginNode, node);
        IncrementNode previousNode = node;

        for (int i = 2; i < 100_000; i++) {
            node = new IncrementNode(i);
            routeSpec.route(previousNode, node);
            previousNode = node;
        }

        var endNode = new ConsumerNode<Integer, SimpleState>(
                "end",
                (input, state) -> state.setNumber(input)
        );

        routeSpec.route(previousNode, endNode);

        var graphExecutor = routeSpec.end(endNode);

        SimpleState state = new SimpleState();
        state.setInput(0);

        graphExecutor.execute(state, "id");

        assertEquals(100_000, state.getNumber());
        assertNotEquals(null, state.getExecutionId());
    }

    private static class IncrementNode extends UnaryNode<Integer, SimpleState> {

        public IncrementNode(Integer number) {
            super("IncrementNode" + number, (input, state) -> input + 1);
        }
    }

    private static class IncrementNodeBegin extends SupplierNode<Integer, SimpleState> {

        public IncrementNodeBegin() {
            super("IncrementNodeBegin", (state) -> state.getInput() + 1);
        }
    }
}
