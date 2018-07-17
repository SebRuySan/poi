package me.sebastianrevel.picofinterest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
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

@RuntimePermissions
public class MapFragment extends Fragment implements GoogleMap.OnMapClickListener {

    MapView mMapView;
    private static GoogleMap map;
    private OnFragmentInteractionListener mListener;

    Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    private final static String KEY_LOCATION = "location";
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private long UPDATE_INTERVAL = 60000; // 60 seconds
    private long FASTEST_INTERVAL = 5000; // 5 seconds

    public MapFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                loadMap(googleMap);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                //this part is harcoded for testing purposes
                ArrayList<LatLng> points = new ArrayList<>();
                LatLng p = new LatLng(47.62, -122.35); // space needle coordinate
                points.add(p);
                LatLng s = new LatLng(47.595, -122.3); // century link field coordinate
                points.add(s);
                LatLng t = new LatLng(46.85, -121.76); // mt. rainier coordinate
                points.add(t);
                LatLng u = new LatLng(47.611, -122.33); // washington state convention center coordinate
                points.add(u);
                LatLng v = new LatLng(67.8, -42.4); // washington state convention center coordinate
                points.add(v);
                addPins(points);
            }
        });

        return rootView;
    }

    private void addPins(ArrayList<LatLng> points) {
        for(LatLng p: points)
            addMarker(p);
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;

        if (map != null) {
            // Map is ready
            Toast.makeText(getContext(), "Map Fragment was loaded properly.", Toast.LENGTH_SHORT).show();

            MapFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
            MapFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
            map.setOnMapClickListener(this);

            if (mCurrentLocation != null) {
                BitmapDescriptor defaultMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
                LatLng currentCoordinates = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(currentCoordinates)
                        .title("Current Location")
                        .icon(defaultMarker));

                dropPinEffect(marker);
            }
        } else {
            Toast.makeText(getContext(), "Error - Map was null!", Toast.LENGTH_SHORT).show();
        }

    }

    public static void addMarker(Place p){

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(p.getLatLng());
        markerOptions.title(p.getName()+"");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
        map.animateCamera(CameraUpdateFactory.zoomTo(13));
    }

    public void addMarker(LatLng p){

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(p);
        markerOptions.title("Point of Interest");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLng(p));
        map.animateCamera(CameraUpdateFactory.zoomTo(13));

    }

    private void dropPinEffect(final Marker marker) {
        final android.os.Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 2000;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        map.setMyLocationEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MainActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);

        getFusedLocationProviderClient(getContext()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.myLooper());
    }

    private void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        // report to the UI that the location was updated
        mCurrentLocation = location;
        String msg = "Updated Location: "
                + Double.toString(location.getLatitude()) + ","
                + Double.toString(location.getLongitude());
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
