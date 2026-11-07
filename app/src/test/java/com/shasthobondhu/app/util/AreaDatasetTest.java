package com.shasthobondhu.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.util.List;

public class AreaDatasetTest {

    @Test
    public void divisionsContainDistrictsAndAreas() {
        List<AreaDataset.Area> divisions = AreaDataset.getDivisions();

        assertFalse(divisions.isEmpty());
        assertEquals("Dhaka", divisions.get(0).name);
        assertFalse(divisions.get(0).children.isEmpty());
        assertFalse(divisions.get(0).children.get(0).children.isEmpty());
    }
}
