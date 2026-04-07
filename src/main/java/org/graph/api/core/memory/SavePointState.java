package org.graph.api.core.memory;

public abstract class SavePointState {

    private transient boolean isSave;
    private transient String saveNodeName;

    public final void toSave() {
        this.isSave = true;
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
