package me.sebastianrevel.picofinterest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

//@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    Fragment mapFragment = new MapFragment();
    FragmentTransaction fragmentTransaction;

//    private SupportMapFragment supportMapFragment;
//    private GoogleMap map;
//    private LocationRequest mLocationRequest;
//    Location mCurrentLocation;

//    private long UPDATE_INTERVAL = 60000; // 60 seconds
//    private long FASTEST_INTERVAL = 5000; // 5 seconds

    PlaceAutocompleteFragment placeAutoComplete;
    private Button cameraBtn;

    private final static String KEY_LOCATION = "location";
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private final static double CURRENT_LATITUDE = 47.629157;
    private final static double CURRENT_LONGITUDE = -122.341167;

    // Request code to send to Google Play services to be returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("You forgot to supply a Google Maps API key");
        }


        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        }

        cameraBtn = findViewById(R.id.camera_btn);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i,0);
            }
        });

//        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
//            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
//        }

        // initialize autocomplete search bar fragment and set a listener
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // add a marker at place selected (from search bar)
                MapFragment.addMarker(place);

                Log.d("Maps", "Place selected: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flContainer, mapFragment);
        fragmentTransaction.commit();

        //supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));

//        if (supportMapFragment != null) {
//            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
//                @Override
//                public void onMapReady(GoogleMap googleMap) {
//                    loadMap(googleMap);
//                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//
//                    //this part is harcoded for testing purposes
//                    ArrayList<LatLng> points = new ArrayList<>();
//                    LatLng p = new LatLng(47.62, -122.35); // space needle coordinate
//                    points.add(p);
//                    LatLng s = new LatLng(47.595, -122.3); // century link field coordinate
//                    points.add(s);
//                    LatLng t = new LatLng(46.85, -121.76); // mt. rainier coordinate
//                    points.add(t);
//                    LatLng u = new LatLng(47.611, -122.33); // washington state convention center coordinate
//                    points.add(u);
//                    LatLng v = new LatLng(67.8, -42.4); // washington state convention center coordinate
//                    points.add(v);
//                    addPins(points);
//                }
//            });
//        } else {
//            Toast.makeText(this,"Error - Map Fragment was null.", Toast.LENGTH_SHORT).show();
//        }
    }

//    private void addPins(ArrayList<LatLng> points) {
//        for(LatLng p: points)
//            addMarker(p);
//    }
//
//    protected void loadMap(GoogleMap googleMap) {
//        map = googleMap;
//
//        if (map != null) {
//            // Map is ready
//            Toast.makeText(this, "Map Fragment was loaded properly.", Toast.LENGTH_SHORT).show();
//
//            MainActivityPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
//            MainActivityPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
//            map.setOnMapClickListener(this);
//
//            if (mCurrentLocation != null) {
//                BitmapDescriptor defaultMarker =
//                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
//                LatLng currentCoordinates = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//                Marker marker = map.addMarker(new MarkerOptions()
//                        .position(currentCoordinates)
//                        .title("Current Location")
//                        .icon(defaultMarker));
//
//                dropPinEffect(marker);
//            }
//        } else {
//            Toast.makeText(this, "Error - Map was null!", Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    public void addMarker(Place p){
//
//        MarkerOptions markerOptions = new MarkerOptions();
//
//        markerOptions.position(p.getLatLng());
//        markerOptions.title(p.getName()+"");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//
//        map.addMarker(markerOptions);
//        map.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
//        map.animateCamera(CameraUpdateFactory.zoomTo(13));
//    }
//
//    public void addMarker(LatLng p){
//
//        MarkerOptions markerOptions = new MarkerOptions();
//
//        markerOptions.position(p);
//        markerOptions.title("Point of Interest");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//
//        map.addMarker(markerOptions);
//        map.moveCamera(CameraUpdateFactory.newLatLng(p));
//        map.animateCamera(CameraUpdateFactory.zoomTo(13));
//
//    }
//
//    private void dropPinEffect(final Marker marker) {
//        final android.os.Handler handler = new Handler();
//        final long start = SystemClock.uptimeMillis();
//        final long duration = 2000;
//
//        final Interpolator interpolator = new BounceInterpolator();
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                long elapsed = SystemClock.uptimeMillis() - start;
//                // Calculate t for bounce based on elapsed time
//                float t = Math.max(
//                        1 - interpolator.getInterpolation((float) elapsed
//                                / duration), 0);
//                // Set the anchor
//                marker.setAnchor(0.5f, 1.0f + 14 * t);
//
//                if (t > 0.0) {
//                    // Post this event again 15ms from now.
//                    handler.postDelayed(this, 15);
//                } else { // done elapsing, show window
//                    marker.showInfoWindow();
//                }
//            }
//        });
//    }

//    @SuppressLint("NeedOnRequestPermissionsResult")
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
//    }

//    @SuppressWarnings({"MissingPermission"})
//    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
//    void getMyLocation() {
//        map.setMyLocationEnabled(true);
//
//        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
//        locationClient.getLastLocation()
//                .addOnSuccessListener(new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        if (location != null) {
//                            onLocationChanged(location);
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d("MainActivity", "Error trying to get last GPS location");
//                        e.printStackTrace();
//                    }
//                });
//    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates", "Google Play services are available.");
            return true;
        } else {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            if (errorDialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    public static class ErrorDialogFragment extends android.support.v4.app.DialogFragment {

        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        LatLng latLng;
//
//        if (mCurrentLocation != null) {
//            //Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
//            latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
//            map.animateCamera(cameraUpdate);
//        } else {
//            Toast.makeText(this, "Current location was null, enable GPS on emulator!",
//                    Toast.LENGTH_SHORT).show();
//            //latLng = new LatLng(CURRENT_LATITUDE, CURRENT_LONGITUDE);
//        }
//    }

//    @SuppressLint("MissingPermission")
//    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
//    protected void startLocationUpdates() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(mLocationRequest);
//        LocationSettingsRequest locationSettingsRequest = builder.build();
//
//        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
//        settingsClient.checkLocationSettings(locationSettingsRequest);
//
//        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                onLocationChanged(locationResult.getLastLocation());
//            }
//        }, Looper.myLooper());
//    }
//
//    private void onLocationChanged(Location location) {
//        if (location == null) {
//            return;
//        }
//
//        // report to the UI that the location was updated
//        mCurrentLocation = location;
//        String msg = "Updated Location: "
//                + Double.toString(location.getLatitude()) + ","
//                + Double.toString(location.getLongitude());
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//    }
}
