package com.shasthobondhu.app.util;

import static org.junit.Assert.assertEquals;

import com.shasthobondhu.app.util.constants.AppConstants;

import org.junit.Test;

public class DisplayUtilsTest {

    @Test
    public void nextStock_cyclesThroughAllStatuses() {
        assertEquals(AppConstants.STOCK_LOW,
                DisplayUtils.nextStock(AppConstants.STOCK_IN));
        assertEquals(AppConstants.STOCK_OUT,
                DisplayUtils.nextStock(AppConstants.STOCK_LOW));
        assertEquals(AppConstants.STOCK_IN,
                DisplayUtils.nextStock(AppConstants.STOCK_OUT));
    }

    @Test
    public void unknownStock_returnsInStock() {
        assertEquals(AppConstants.STOCK_IN, DisplayUtils.nextStock("unknown"));
    }
}
