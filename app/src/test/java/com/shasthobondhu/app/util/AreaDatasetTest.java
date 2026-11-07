package com.shasthobondhu.app.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class AreaDatasetTest {

    @Test
    public void containsAllEightDivisions() {
        List<AreaDataset.Area> divisions = AreaDataset.getDivisions();

        assertEquals(8, divisions.size());
        assertEquals("Dhaka", divisions.get(0).name);
        assertEquals("Mymensingh", divisions.get(7).name);
    }
}
