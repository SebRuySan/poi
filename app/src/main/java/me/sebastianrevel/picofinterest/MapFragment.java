package me.sebastianrevel.picofinterest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
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
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import me.sebastianrevel.picofinterest.Models.Pics;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.parse.Parse.getApplicationContext;

//import android.net.ParseException;

@RuntimePermissions
public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    MapView mMapView;
    private static GoogleMap map;
    private OnFragmentInteractionListener mListener;

    Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    private final static String KEY_LOCATION = "location";
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private long UPDATE_INTERVAL = 60000; // 60 seconds
    private long FASTEST_INTERVAL = 5000; // 5 seconds

    Button btnStyle; // this is the button to change the mapstyle
    boolean daymode; // this variable is true if current style is daymode and is false if current map style id night mode

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
                // Customise the styling of the base map using a JSON object defined
                // in a string resource file. First create a MapStyleOptions object
                // from the JSON styles string, then pass this to the setMapStyle
                // method of the GoogleMap object.
//                boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
//                        .getString(R.string.nightstyle_json)));
//
//                if (!success) {
//                    Log.e("Map Fragment", "Style parsing failed.");
//                }
                /*try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    getActivity(), R.raw.style_json));

                    if (!success) {
                        Log.e("MapsActivity", "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e("MapsActivity", "Can't find style.", e);
                }*/

                // initialize the map style as daymode

                loadMap(googleMap);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                daymode = false;
                changeStyle();

                // Customise the styling of the base map using a JSON object defined
                // in a string resource file. First create a MapStyleOptions object
                // from the JSON styles string, then pass this to the setMapStyle
                // method of the GoogleMap object.


                //this part is harcoded for testing purposes
                /*ArrayList<LatLng> points = new ArrayList<>();
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
                addPins(points);*/

                // Define the class we would like to Query
                ParseQuery<Pics> query =ParseQuery.getQuery(Pics.class);
                // get all posts
                final Set<String> locs = new HashSet<String>(); // this will contain a no-duplicate set of locations hwere pictures have been taken
                final ArrayList<Pics> pictures = new ArrayList<>(); // this will contain a list of Pics (filtered so that it only contains the most recent picture taken at each location)
                query.orderByDescending("createdAt"); // so query returns results in order of most recent pictures
                query.findInBackground(new FindCallback<Pics>(){
                    public void done(List<Pics> itemList, ParseException e){
                        Log.d("MapFragment", "Query done");
                        Log.d("MapFragment", "ItemList array size : " + itemList.size());
                        // if no errors
                        if(e == null){
                            Log.d("MapFragment", "No errors in querying");
                            for(Pics p: itemList){
                                boolean added = locs.add(p.getLocation());
                                Log.d("MapFragment", "Attempt to add to pictures");
                                if(added) {
                                    pictures.add(p);
                                    Log.d("MapFragment", "Item added to pictures");
                                }
                            }
                            Log.d("MapFragment", "Pictures array size1 : " + pictures.size());
                            Place place;
                            // after all pictures have been added, add markers there
                            for(final Pics p : pictures) {
                                //addMarker(p);
                                place = new Place() {
                                    @Override
                                    public String getId() {
                                        return "Picture Location";
                                    }

                                    @Override
                                    public List<Integer> getPlaceTypes() {
                                        return null;
                                    }

                                    @Nullable
                                    @Override
                                    public CharSequence getAddress() {
                                        return p.getLocation();
                                    }

                                    @Override
                                    public Locale getLocale() {
                                        return null;
                                    }

                                    @Override
                                    public CharSequence getName() {
                                        return p.getLocation();
                                    }

                                    @Override
                                    public LatLng getLatLng() {
                                        return new LatLng(p.getLat(), p.getLong());
                                    }

                                    @Nullable
                                    @Override
                                    public LatLngBounds getViewport() {
                                        return null;
                                    }

                                    @Nullable
                                    @Override
                                    public Uri getWebsiteUri() {
                                        return null;
                                    }

                                    @Nullable
                                    @Override
                                    public CharSequence getPhoneNumber() {
                                        return null;
                                    }

                                    @Override
                                    public float getRating() {
                                        return 0;
                                    }

                                    @Override
                                    public int getPriceLevel() {
                                        return 0;
                                    }

                                    @Nullable
                                    @Override
                                    public CharSequence getAttributions() {
                                        return null;
                                    }

                                    @Override
                                    public Place freeze() {
                                        return null;
                                    }

                                    @Override
                                    public boolean isDataValid() {
                                        return false;
                                    }
                                };
                                addMarker(place, p.getPic());
                            }
                        }
                        else {
                            Log.d("item", "Error: " + e.getMessage());
                        }
                    }

                });
            }
        });


        if (mCurrentLocation != null) {
            LatLng currentCoordinates = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLng(currentCoordinates));
            map.animateCamera(CameraUpdateFactory.zoomTo(13));

            //dropPinEffect(marker);

        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle SavedInstanceState) {
        btnStyle = (Button) view.findViewById(R.id.btnStyle);
        btnStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStyle();
            }
        });

    }

    private void addPins(ArrayList<LatLng> points) {
        for(LatLng p: points)
            addMarker(p);
    }

    private void changeStyle(){
       if(daymode){
           try {
               // Customise the styling of the base map using a JSON object defined
               // in a raw resource file.
               boolean success = map.setMapStyle(
                       MapStyleOptions.loadRawResourceStyle(
                               getActivity(), R.raw.style_json));

               if (!success) {
                   Log.e("MapsActivity", "Style parsing failed.");
               }
           } catch (Resources.NotFoundException e) {
               Log.e("MapsActivity", "Can't find style.", e);
           }
       }
       else{
           try {
               // Customise the styling of the base map using a JSON object defined
               // in a raw resource file.
               boolean success = map.setMapStyle(
                       MapStyleOptions.loadRawResourceStyle(
                               getActivity(), R.raw.retrostyle_json));

               if (!success) {
                   Log.e("MapsActivity", "Style parsing failed.");
               }
           } catch (Resources.NotFoundException e) {
               Log.e("MapsActivity", "Can't find style.", e);
           }
       }
       daymode = !daymode;
    }
    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;

        if (map != null) {
            // Map is ready
            Toast.makeText(getContext(), "Map Fragment was loaded properly.", Toast.LENGTH_SHORT).show();

            MapFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
            MapFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
            //map.setOnMapClickListener(this);

            if (mCurrentLocation != null) {
                BitmapDescriptor defaultMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
                LatLng currentCoordinates = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(currentCoordinates)
                        .title("Current Location")
                        .icon(defaultMarker));

                map.moveCamera(CameraUpdateFactory.newLatLng(currentCoordinates));
                map.animateCamera(CameraUpdateFactory.zoomTo(13));

                //dropPinEffect(marker);

            }
        } else {
            Toast.makeText(getContext(), "Error - Map was null!", Toast.LENGTH_SHORT).show();
        }

        map.setOnMarkerClickListener(this);

    }

    public static void addMarker(final Place p, ParseFile parseFile){
        Target mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d("MapFragment", "Marker is being created");
                Marker driver_marker = map.addMarker(new MarkerOptions()
                        .position(p.getLatLng())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title(p.getName() + "")
                        //.snippet("test address")
                );
                Log.d("MapFragment", "Marker created");
                map.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
                map.animateCamera(CameraUpdateFactory.zoomTo(13));
                Log.d("MapFragment", "Camera zoomed in hopefully");
            }

            @Override
            public void onBitmapFailed(Exception ex, Drawable errorDrawable) {
                ex.printStackTrace();
                Log.d("picasso", "onBitmapFailed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        String imagePath = null;
        File imageFile = null;
        try {
            imageFile = parseFile.getFile();
            imagePath = imageFile.getAbsolutePath();
            Log.d("MapFragment", "Imagepath is not null");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("MapFragment", "Picasso about to be called");
        Picasso.get()
                .load(imageFile)
                .resize(200,200)
                .centerCrop()
                .transform(new CircleBubbleTransformation())
                .into(mTarget);
        Log.d("MapFragment", "Picasso hopefully done");
        /*
        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(p.getLatLng());
        markerOptions.title(p.getName()+"");

        String imagePath = null;
        try {
            File imageFile = parseFile.getFile();
            imagePath = imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (imagePath != null) {
            //Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Bitmap bitmap = rotateBitmapOrientation(imagePath);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }


        map.addMarker(markerOptions).showInfoWindow();
        map.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
        map.animateCamera(CameraUpdateFactory.zoomTo(13));
        */
    }

    public static void addMarker(Place p){

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(p.getLatLng());
        markerOptions.title(p.getName()+"");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


        map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
        map.animateCamera(CameraUpdateFactory.zoomTo(13));
    }

    public static void addMarker(LatLng p){

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(p);
        markerOptions.title("Point of Interest");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLng(p));
        map.animateCamera(CameraUpdateFactory.zoomTo(13));

    }

    public static void addMarker(Pics p){
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng l = new LatLng(p.getLat(), p.getLong());
        markerOptions.position(l);
        markerOptions.title(p.getLocation());
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        String imagePath = null;
        try {
            File imageFile = p.getPic().getFile();
            imagePath = imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (imagePath != null) {
            //Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Bitmap bitmap = rotateBitmapOrientation(imagePath);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }

        map.addMarker(markerOptions).showInfoWindow();
        map.moveCamera(CameraUpdateFactory.newLatLng(l));
        map.animateCamera(CameraUpdateFactory.zoomTo(13));
    }
    public static Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
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
    public boolean onMarkerClick(final Marker marker) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        if (marker != null) {
            //Toast.makeText(getContext(), "Marker click registered", Toast.LENGTH_SHORT).show();
            try {
                MainActivity.drawerOpen(marker, geocoder);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
        }

        return false;
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
