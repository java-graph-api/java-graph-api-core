package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.RunnableNodeAction;

/**
 * Узел, выполняющий побочные эффекты, не принимая и не возвращая данных.
 * <p>
 * Делегирует выполнение предоставленному {@link RunnableNodeAction} и наследует логику работы
 * с метаданными и ограничением числа вызовов от {@link ConsumerNode}.
 *
 * @param <S> тип состояния графа
 */
public class RunnableNode<S extends GraphState> extends ConsumerNode<Void, S> {

    /**
     * Создаёт узел без входных данных и результата без ограничения числа вызовов и метаданных.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     */
    public RunnableNode(String name, RunnableNodeAction<S> nodeAction) {
        super(name, nodeAction);
    }

    /**
     * Создаёт узел без входных данных и результата с ограничением числа вызовов и без метаданных.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param callLimit  максимальное число вызовов узла; {@code 0} означает отсутствие ограничения
     */
    public RunnableNode(String name, RunnableNodeAction<S> nodeAction, int callLimit) {
        super(name, nodeAction, callLimit);
    }

    /**
     * Создаёт узел без входных данных и результата без ограничения числа вызовов, но с метаданными.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param metadata   метаданные, описывающие узел
     */
    public RunnableNode(String name, RunnableNodeAction<S> nodeAction, Metadata metadata) {
        super(name, nodeAction, metadata);
    }

    /**
     * Создаёт узел без входных данных и результата с ограничением числа вызовов и метаданными.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param callLimit  максимальное число вызовов узла; {@code 0} означает отсутствие ограничения
     * @param metadata   метаданные, описывающие узел
     */
    public RunnableNode(String name, RunnableNodeAction<S> nodeAction, int callLimit, Metadata metadata) {
        super(name, nodeAction, callLimit, metadata);
    }

}
