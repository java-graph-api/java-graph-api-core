package org.graph.api.core.memory;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.Node;

public class SavePointState {

    private transient boolean isSave;
    private transient String saveNodeName;

    public final <T extends GraphState> T toSave() {
        this.isSave = true;
        return (T) this;
    }

    public final <T extends GraphState> T redirect(String nodeName) {
        // todo убрать все редерикты
        saveNodeName = nodeName;
        toSave();
        return (T) this;
    }

    public final <T extends GraphState> T redirect(Node<?, ?, ?> node) {
        // todo убрать все редерикты
        // todo есть проблемы с методом
        redirect(node.getName());
        return (T) this;
    }

    final String getSaveNodeName() {
        return saveNodeName;
    }

    final boolean isSave() {
        return isSave;
    }

    final void saveClear() {
        this.isSave = false;
        saveNodeName = null;
    }
}
