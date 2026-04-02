package org.graph.api.core.util;

import org.graph.api.core.SimpleState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GraphStateMapperTest {

    @Test
    void serializeTest() {
        SimpleState stateSource = new SimpleState();
        stateSource.setNotSerialize(true);
        stateSource.setResult(0);

        SimpleState stateTarget = new SimpleState();

        GraphStateMapper.merge(stateSource, stateTarget);

        assertFalse(stateTarget.isSerialize());
        assertEquals(0, stateTarget.getResult());
    }
}
