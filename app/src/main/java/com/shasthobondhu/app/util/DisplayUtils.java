package com.shasthobondhu.app.util;

import android.content.Context;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.util.constants.AppConstants;

/** Maps stored English values to the visible, localised labels. */
public final class DisplayUtils {

    private DisplayUtils() {
    }

    public static String stockLabel(Context context, String status) {
        if (AppConstants.STOCK_LOW.equals(status)) {
            return context.getString(R.string.stock_low);
        }
        if (AppConstants.STOCK_OUT.equals(status)) {
            return context.getString(R.string.stock_out);
        }
        return context.getString(R.string.stock_in);
    }

    public static String urgencyLabel(Context context, String urgency) {
        if (AppConstants.URGENCY_CRITICAL.equals(urgency)) {
            return context.getString(R.string.urgency_critical);
        }
        if (AppConstants.URGENCY_URGENT.equals(urgency)) {
            return context.getString(R.string.urgency_urgent);
        }
        return context.getString(R.string.urgency_normal);
    }

    /** In Stock -> Low -> Out of Stock -> In Stock. */
    public static String nextStock(String status) {
        if (AppConstants.STOCK_IN.equals(status)) {
            return AppConstants.STOCK_LOW;
        }
        if (AppConstants.STOCK_LOW.equals(status)) {
            return AppConstants.STOCK_OUT;
        }
        return AppConstants.STOCK_IN;
    }
}
