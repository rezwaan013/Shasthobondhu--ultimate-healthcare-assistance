package com.shasthobondhu.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.shasthobondhu.app.R;

/** Opens turn-by-turn Google Maps navigation with safe fallbacks. */
public final class MapNavigationUtils {

    private MapNavigationUtils() {
    }

    public static void navigate(Context context, String destination) {
        if (context == null || destination == null || destination.trim().isEmpty()) {
            return;
        }

        String encoded = Uri.encode(destination.trim());

        Intent googleNavigation = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + encoded + "&mode=d"));
        googleNavigation.setPackage("com.google.android.apps.maps");
        if (googleNavigation.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(googleNavigation);
            return;
        }

        Intent geoIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=" + encoded));
        if (geoIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(geoIntent);
            return;
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + encoded));
        if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(browserIntent);
        } else {
            Toast.makeText(context, R.string.no_maps_app, Toast.LENGTH_LONG).show();
        }
    }
}
