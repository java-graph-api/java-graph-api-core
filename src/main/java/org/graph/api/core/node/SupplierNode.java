package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.SupplierNodeAction;

/**
 * Узел-источник, производящий данные без входных параметров.
 * <p>
 * Выполняет логику, описанную в {@link SupplierNodeAction}, и использует возможности базового
 * {@link FunctionalNode} для работы с метаданными, идентификатором и ограничением вызовов.
 *
 * @param <O> тип возвращаемых данных
 * @param <S> тип состояния графа
 */
public class SupplierNode<O, S extends GraphState> extends FunctionalNode<Void, O, S> {

    /**
     * Создаёт узел-источник без ограничения числа вызовов и без метаданных.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     */
    public SupplierNode(String name, SupplierNodeAction<O, S> nodeAction) {
        super(name, nodeAction);
    }

    /**
     * Создаёт узел-источник с ограничением числа вызовов и без метаданных.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param callLimit  максимальное число вызовов узла; {@code 0} означает отсутствие ограничения
     */
    public SupplierNode(String name, SupplierNodeAction<O, S> nodeAction, int callLimit) {
        super(name, nodeAction, callLimit);
    }

    /**
     * Создаёт узел-источник без ограничения числа вызовов, но с метаданными.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param metadata   метаданные, описывающие узел
     */
    public SupplierNode(String name, SupplierNodeAction<O, S> nodeAction, Metadata metadata) {
        super(name, nodeAction, metadata);
    }

    /**
     * Создаёт узел-источник с ограничением числа вызовов и метаданными.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param callLimit  максимальное число вызовов узла; {@code 0} означает отсутствие ограничения
     * @param metadata   метаданные, описывающие узел
     */
    public SupplierNode(String name, SupplierNodeAction<O, S> nodeAction, int callLimit, Metadata metadata) {
        super(name, nodeAction, callLimit, metadata);
    }

}
