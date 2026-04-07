package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.UnaryNodeAction;

/**
 * Узел-преобразователь, принимающий и возвращающий данные одного типа.
 * <p>
 * Делегирует преобразование предоставленному {@link UnaryNodeAction} и использует базовую
 * функциональность {@link FunctionalNode} для работы с идентификатором, метаданными и лимитом вызовов.
 *
 * @param <D> тип обрабатываемых и возвращаемых данных
 * @param <S> тип состояния графа
 */
public class UnaryNode<D, S extends GraphState> extends FunctionalNode<D, D, S> {

    /**
     * Создаёт узел-преобразователь без ограничения числа вызовов и метаданных.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     */
    public UnaryNode(String name, UnaryNodeAction<D, S> nodeAction) {
        this(name, nodeAction, 0);
    }

    /**
     * Создаёт узел-преобразователь с ограничением числа вызовов и без метаданных.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param callLimit  максимальное число вызовов узла; {@code 0} означает отсутствие ограничения
     */
    public UnaryNode(String name, UnaryNodeAction<D, S> nodeAction, int callLimit) {
        super(name, nodeAction, callLimit);
    }
}
