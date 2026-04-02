package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.ConsumerNodeAction;

/**
 * Узел, выполняющий действие без возвращаемого значения.
 * <p>
 * Делегирует работу предоставленному {@link ConsumerNodeAction} и наследует
 * обработку метаданных, идентификатора и ограничения числа вызовов от {@link FunctionalNode}.
 *
 * @param <I> тип входных данных
 * @param <S> тип состояния графа
 */
public class ConsumerNode<I, S extends GraphState> extends FunctionalNode<I, Void, S> {

    /**
     * Создаёт узел, выполняющий действие без результата, без ограничения числа вызовов и метаданных.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     */
    public ConsumerNode(String name, ConsumerNodeAction<I, S> nodeAction) {
        super(name, nodeAction);
    }

    /**
     * Создаёт узел с ограничением числа вызовов и без метаданных.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param callLimit  максимальное число вызовов узла; {@code 0} означает отсутствие ограничения
     */
    public ConsumerNode(String name, ConsumerNodeAction<I, S> nodeAction, int callLimit) {
        super(name, nodeAction, callLimit);
    }

    /**
     * Создаёт узел без ограничения числа вызовов, но с метаданными.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param metadata   метаданные, описывающие узел
     */
    public ConsumerNode(String name, ConsumerNodeAction<I, S> nodeAction, Metadata metadata) {
        super(name, nodeAction, metadata);
    }

    /**
     * Создаёт узел с ограничением числа вызовов и метаданными.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param callLimit  максимальное число вызовов узла; {@code 0} означает отсутствие ограничения
     * @param metadata   метаданные, описывающие узел
     */
    public ConsumerNode(String name, ConsumerNodeAction<I, S> nodeAction, int callLimit, Metadata metadata) {
        super(name, nodeAction, callLimit, metadata);
    }

}
