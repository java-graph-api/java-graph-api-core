package org.graph.api.core;

import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.GraphMemoryDefault;
import org.graph.api.core.model.AgentState;
import org.graph.api.core.node.ConsumerNode;
import org.graph.api.core.node.SupplierNode;
import org.graph.api.core.node.RunnableNode;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LlmAnswersTest {

    private final GraphOptions graphOptions = GraphOptions.builder().graphName("LlmAnswersTest").build();
    private GraphBuilder<AgentState> graphBuilder;

    @BeforeEach
    public void beforeEach() {
        GraphMemory graphMemory = new GraphMemoryDefault();
        graphBuilder = new GraphBuilder<AgentState>()
                .memory(graphMemory)
                .options(graphOptions);
    }


    RunnableNode<AgentState> intentParser = new RunnableNode<>(
            "intentParser",
            state -> {
                List<String> questions = Stream.of("Question1", "Question2").collect(Collectors.toList());
                state.setQuestions(questions);
            }
    );

    SupplierNode<Boolean, AgentState> intentParser2 = new SupplierNode<>(
            "intentParser2",
            state -> {
                state.setIntentCompleted(true);
                return true;
            }
    );

    SupplierNode<Boolean, AgentState> intentQuestion = new SupplierNode<>(
            "intentQuestion",
            state -> {
                var question = state.getQuestions().get(0);
                state.setResponse(question);
                state.redirect("intentAnswer");
                return true;
            }
    );

    ConsumerNode<Boolean, AgentState> intentAnswer = new ConsumerNode<>(
            "intentAnswer",
            (input, state) -> {
                state.getIntentAnswers().put(state.getQuestions().get(0), state.getRequest());
                state.getQuestions().remove(0);
            }
    );

    RunnableNode<AgentState> finalNode = new RunnableNode<>(
            "finalNode",
            state -> System.out.println()
    );

    ConsumerNode<Boolean, AgentState> finalNode2 = new ConsumerNode<>(
            "finalNode2",
            (o, state) -> System.out.println()
    );

    ConsumerNode<Boolean, AgentState> finalNode3 = new ConsumerNode<>(
            "finalNode3",
            (o, state) -> state.setGraphCompletion(o)
    );


    @Test
    void test() {
        var graphId = "id";

        var graph = graphBuilder
                .begin(intentParser)
                .route(intentParser, intentQuestion, (output, state) -> !state.getQuestions().isEmpty())
                .route(intentAnswer, intentQuestion, (output, state) -> !state.getQuestions().isEmpty())
                .route(intentAnswer, intentParser2, (output, state) -> state.getQuestions().isEmpty())
                .route(intentAnswer, finalNode)
                .route(intentParser2, finalNode3)
                .route(intentQuestion, intentAnswer, (output, state) -> !output)
                .route(intentQuestion, finalNode2, (output, state) -> output)
                .end(List.of(finalNode, finalNode2, finalNode3));

        AgentState state = new AgentState();
        state = graph.execute(state, graphId);

        assertEquals("Question1", state.getResponse());

        state = new AgentState();
        state.setRequest("Answer1");
        state = graph.execute(state, graphId);

        assertEquals("Question2", state.getResponse());
        assertEquals("Answer1", state.getIntentAnswers().get("Question1"));

        state = new AgentState();
        state.setRequest("Answer2");
        state = graph.execute(state, graphId);

        assertEquals("Answer1", state.getIntentAnswers().get("Question1"));
        assertEquals("Answer2", state.getIntentAnswers().get("Question2"));
        assertEquals(2, state.getIntentAnswers().size());
        assertTrue(state.isGraphCompletion());
    }
}
