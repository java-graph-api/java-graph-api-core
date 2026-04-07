package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.NodeAction;

import java.util.Objects;
import java.util.UUID;

/**
 * Базовая реализация {@link Node}, инкапсулирующая функциональный обработчик узла.
 * <p>
 * Использует переданное действие {@link NodeAction} для выполнения бизнес-логики, хранит метаданные,
 * ограничение количества вызовов и автоматически генерирует уникальный идентификатор узла.
 *
 * @param <I> тип входных данных, обрабатываемых узлом
 * @param <O> тип выходных данных, производимых узлом
 * @param <S> тип состояния графа, доступного узлу во время выполнения
 */
public class FunctionalNode<I, O, S extends GraphState> implements Node<I, O, S> {

    private final String name;
    private final NodeAction<I, O, S> nodeAction;
    private final UUID id = UUID.randomUUID();
    private final int callLimit;

    /**
     * Создаёт узел с указанными именем и действием без ограничения числа вызовов и без метаданных.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     */
    public FunctionalNode(String name, NodeAction<I, O, S> nodeAction) {
        this(name, nodeAction, 0);
    }

    /**
     * Создаёт узел с именем, действием, ограничением числа вызовов и метаданными.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие, выполняемое узлом
     * @param callLimit  максимальное число вызовов узла; {@code 0} означает отсутствие ограничения
     */
    public FunctionalNode(String name, NodeAction<I, O, S> nodeAction, int callLimit) {
        validate(name, nodeAction);
        this.name = name;
        this.nodeAction = nodeAction;
        this.callLimit = callLimit;
    }

    /**
     * Возвращает заданное при создании имя узла.
     *
     * @return человекочитаемое имя
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Делегирует выполнение действия узла предоставленному {@link NodeAction}.
     *
     * @param input входное значение, передаваемое узлу
     * @param state состояние графа, доступное для чтения и изменения
     * @return результат выполнения действия узла
     */
    @Override
    public O call(I input, S state) {
        return nodeAction.action(input, state);
    }

    /**
     * Возвращает ограничение на количество вызовов узла.
     *
     * @return максимальное число вызовов или {@code 0}, если ограничение не задано
     */
    @Override
    public int callLimit() {
        return callLimit;
    }

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Проверяет корректность обязательных параметров узла.
     *
     * @param name       человекочитаемое имя узла
     * @param nodeAction действие узла
     */
    private void validate(String name, NodeAction<I, O, S> nodeAction) {
        Objects.requireNonNull(name, "Node name cannot be null");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Node name cannot be empty");
        }
        Objects.requireNonNull(nodeAction, "Node action cannot be null");
    }
}
