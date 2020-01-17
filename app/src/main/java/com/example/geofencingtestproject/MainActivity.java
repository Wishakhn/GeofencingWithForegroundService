package com.example.geofencingtestproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private static final int REQUEST_PERMISSION_CODE = 1002;
    Button startGeofencing;
    Button stopGeofencing;
    protected GoogleApiClient mGoogleApiClient;
    GeofencingRequest request;
    private PendingIntent mGeofencePendingIntent;
    FusedLocationProviderClient fusedClient;
    LatLng latLng1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startGeofencing = findViewById(R.id.startGeofencing);
        stopGeofencing = findViewById(R.id.stopGeofencing);
        mGeofencePendingIntent = null;
        buildGoogleApiClient();
        getmyDevice_Location();
        setListner();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    void getmyDevice_Location() {
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        fusedClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onComplete(@NonNull final Task<Location> task) {
                if (task.isSuccessful()) {
                    Location current_location = task.getResult();
                    latLng1 = new LatLng(current_location.getLatitude(), current_location.getLongitude());
                    System.err.println("Current Latlongs are Lat: "+current_location.getLatitude()+" long: "+current_location.getLongitude());
                    startGeofencing(latLng1);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to detect your location", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setListner() {
        startGeofencing.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (checkCameraPermissions()) {
                    if (mGoogleApiClient.isConnected()) {
                        getmyDevice_Location();
                    }
                } else {
                    requestPermissions();
                }
            }
        });
        stopGeofencing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCameraPermissions()) {
                    removeGeofence();
                } else {
                    requestPermissions();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    private void logSecurityException(Exception securityException) {
        System.err.println("Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences" + securityException);
    }


    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, geofenceIntentservice.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    private void removeGeofence() {
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent()).setResultCallback(this);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startGeofencing(LatLng latlng) {
        if (latlng != null) {
            Geofence geofence = createGeofence(latlng, 50f);
            request = creatGeoRequest(geofence);
            addGeofence();

        }
    }

    private GeofencingRequest creatGeoRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private Geofence createGeofence(LatLng position, float v) {
        return new Geofence.Builder()
                .setRequestId("Geofence_target")//requestId of user to track
                .setCircularRegion(position.latitude, position.longitude, v)
                .setExpirationDuration(60 * 60 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private void addGeofence() {
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, request, getGeofencePendingIntent()).setResultCallback(this);
        } catch (Exception e) {
            logSecurityException(e);
        }
    }


    boolean checkCameraPermissions() {
        int locationPermission = ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION");
        int fineLocationPermission = ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION");
        return fineLocationPermission == 0 && locationPermission == 0;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_BACKGROUND_LOCATION",
                "android.permission.ACCESS_FINE_LOCATION"}, REQUEST_PERMISSION_CODE);

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length <= 0 || grantResults[0] != 0) {
                Toast.makeText(this, "You havee to gran all permissions", Toast.LENGTH_SHORT).show();
            } else {

            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.err.println("Google client is connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.err.println("Google client is susspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.err.println("Google client connection failed");

    }

    @Override
    public void onResult(@NonNull Status status) {
        System.err.println("Google client status is " + status);
    }
}
