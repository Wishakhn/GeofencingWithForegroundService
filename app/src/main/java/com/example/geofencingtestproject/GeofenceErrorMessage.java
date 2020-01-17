package com.example.geofencingtestproject;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.GeofenceStatusCodes;

public class GeofenceErrorMessage {
    public GeofenceErrorMessage() {
    }

    /**
     * Returns the error string for a geofencing exception.
     */
    public static String getErrorString(Context context, Exception e) {
        String msg="";
        if (e instanceof ApiException) {
            return getErrorString(context, ((ApiException) e).getStatusCode());
        } else {
            msg = "Unknow error occured";

            return msg;
        }
    }

    /**
     * Returns the error string for a geofencing error code.
     */
    public static String getErrorString(Context context, int errorCode) {
        String msg="";
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                msg = "Geofence is not available";
                return msg;
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                msg = "Geofence have too many geofences";
                return msg;
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                msg = "Geofence have too many pending intents";
                return msg;
            default:
                msg = "unknow error occured";
                return msg;
        }
    }
}
