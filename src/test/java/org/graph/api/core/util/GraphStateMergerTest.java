package org.graph.api.core.util;

import org.graph.api.core.SimpleState;
import org.graph.api.core.memory.GraphStateMerger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GraphStateMergerTest {

    @Test
    void serializeTest() {
        SimpleState stateSource = new SimpleState();
        stateSource.setNotSerialize(true);
        stateSource.setResult(0);

        SimpleState stateTarget = new SimpleState();

        GraphStateMerger.merge(stateSource, stateTarget);

        assertFalse(stateTarget.isSerialize());
        assertEquals(0, stateTarget.getResult());
    }
}
