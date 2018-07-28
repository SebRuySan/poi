package me.sebastianrevel.picofinterest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.graphics.Matrix;

import java.util.Calendar;
import com.google.maps.android.SphericalUtil;
import com.parse.ParseException;
import android.media.ExifInterface;
//import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.autofill.SaveCallback;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import android.text.format.DateUtils;

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
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
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
public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {

    static MapView mMapView;
    private static GoogleMap map;
    private OnFragmentInteractionListener mListener;
    private static int mRadius = 15;
    private int mTimeframe = 5;
    private static Circle radiusCircle;
    private static float zoomLevel;

    public static LatLng mCurrentLocation;
    public static LatLng mSearchLocation;
    private LocationRequest mLocationRequest;

    private static Context context;
    private static MapFragment thisMapFrag;

    private final static String KEY_LOCATION = "location";
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private long UPDATE_INTERVAL = 60000; // 60 seconds
    private long FASTEST_INTERVAL = 5000; // 5 seconds

    Switch swStyle; // this is the button to change the mapstyle
    Button btnLogout; // this is the button to log out
    // the following are for notifications/messages
    private CardView cvMess;
    private TextView tvmessage;
    static boolean daymode; // this variable is true if current style is daymode and is false if current map style id night mode

    public MapFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            Location currentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCurrentLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mSearchLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        }

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        context = getContext();
        thisMapFrag = MapFragment.this;
        showMap();

        return rootView;
    }

    public static void showMap() {
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                loadMap(googleMap);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                daymode = false;
                changeStyle();

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
                ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class);
                // get all posts
                final Set<String> locs = new HashSet<String>(); // this will contain a no-duplicate set of locations hwere pictures have been taken
                final ArrayList<Pics> pictures = new ArrayList<>(); // this will contain a list of Pics (filtered so that it only contains the most recent picture taken at each location)
                query.orderByDescending("createdAt"); // so query returns results in order of most recent pictures
                query.findInBackground(new FindCallback<Pics>(){
                    public void done(List<Pics> itemList, ParseException e){
                        Log.d("MapFragment", "Query done");
                       // Log.d("MapFragment", "ItemList array size : " + itemList.size());
                        // if no errors
                        if(e == null){
                            Log.d("MapFragment", "No errors in querying");

                            if (mSearchLocation != null) {
                                ArrayList<Pics> filteredList = filterList(itemList, mSearchLocation);

                                for (Pics p : filteredList) {
                                    boolean added = locs.add(p.getLocation());
                                    Log.d("MapFragment", "Attempt to add to pictures");

                                    if (added) {
                                        pictures.add(p);
                                        Log.d("MapFragment", "Item added to pictures");
                                    }
                                }
                            } else {
                                for (Pics p : itemList) {
                                    boolean added = locs.add(p.getLocation());
                                    Log.d("MapFragment", "Attempt to add to pictures");
                                    if (added) {
                                        pictures.add(p);
                                        Log.d("MapFragment", "Item added to pictures");
                                    }
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

                            if (mSearchLocation != null) {
                                zoomLevel = (float)13.5;

                                if (mRadius >= 2 && mRadius <= 8) {
                                    zoomLevel = (float)12.5;
                                } else if (mRadius > 8 && mRadius <= 15) {
                                    zoomLevel = (float)9.5;
                                } else if (mRadius > 15 && mRadius < 30) {
                                    zoomLevel = (float)8.5;
                                } else if (mRadius >= 30) { // max radius is 50
                                    zoomLevel = (float)7.8;
                                }

                                map.moveCamera(CameraUpdateFactory.newLatLng(mSearchLocation));
                                map.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));

                                if (radiusCircle != null) {
                                    radiusCircle.remove();
                                }

                                CircleOptions circleOptions = new CircleOptions()
                                        .center(mSearchLocation)
                                        .radius((double)mRadius / 0.00062137)
                                        .strokeWidth(10)
                                        .strokeColor(Color.WHITE);
                                radiusCircle = map.addCircle(circleOptions);
                            }
                        }
                        else {
                            Log.d("item", "Error: " + e.getMessage());
                        }
                    }

                });
            }
        });
    }

    public static void setmSearchLocation(double lat, double lon) {
        mSearchLocation = new LatLng(lat, lon);
    }

    public static ArrayList<Pics> filterList (List<Pics> toFiler, LatLng fromLoc) {
        ArrayList<Pics> filtered = new ArrayList<>();

        if (fromLoc != null) {
            for (Pics p : toFiler) {
                LatLng to = new LatLng(p.getLat(), p.getLong());
                double distanceMeters = SphericalUtil.computeDistanceBetween(fromLoc, to);
                double distanceMiles = distanceMeters * 0.00062137;

                if (distanceMiles <= mRadius) {
                    filtered.add(p);
                }
            }
        }

        if (filtered != null) {
            return filtered;
        } else {
            return (ArrayList<Pics>) toFiler;
        }
    }

    public void setRadius(int radius) {
        this.mRadius = radius;
    }

    public void setTimeframe(int timeframe) {
        this.mTimeframe = timeframe;
    }

    @Override
    public void onViewCreated(View view, Bundle SavedInstanceState) {
        // initialize the switch and set listener so that when it's "checked" (clicked), map style changes from night to day or vice versa
        swStyle = (Switch) view.findViewById(R.id.swStyle);
        swStyle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                changeStyle();
            }
        });

        // initialize the cardview for messages but set as invisible
        cvMess = (CardView) view.findViewById(R.id.cvMess);
        cvMess.setVisibility(View.INVISIBLE); // it starts off as invisible but becomes visible when certain conditions are met
        // initialize the text view within the cardview
        tvmessage = (TextView) view.findViewById(R.id.tvMessage);
        tvmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cvMess.setVisibility(View.INVISIBLE); // this is so that the "notification"/message goes away when the text is clicked.
            }
        });

        new Thread(new Runnable() {
            public void run() {
                final ParseUser user = ParseUser.getCurrentUser();
                Date last;
                try {
                    last = user.fetchIfNeeded().getDate("lastnotification");
                } catch (ParseException e) {
                    e.printStackTrace();
                    last = null;
                }
                // if there is a notification sent to this user a minute or less ago, wait
                while (last != null && getRelativeTimeAgo(last.toString()).indexOf("minutes") < 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
                // otherwise if there hasn't been a notification recently or ever, set a personalized message and set the cardview to be visible aka send notification
                String username;
                try {
                    username = user.fetchIfNeeded().getString("username");
                } catch (ParseException e) {
                    e.printStackTrace();
                    username = "You";
                }
//                tvmessage.setText(username + ", there is a Pic of Interest near you!");
//                cvMess.setVisibility(View.VISIBLE);


                Date currentTime = Calendar.getInstance().getTime();
                String mess = "";
                user.put("lastnotification", currentTime);
                user.saveInBackground();
                tvmessage.setText(mess + " " + username + ", there is a Pic of Interest near you!"+ currentTime);
                cvMess.setVisibility(View.VISIBLE);
            }
        }).start();

        // we also want to initialize the logout button and set an on click listener so the user is logged out when the button is pressed
//        btnLogout = (Button) view.findViewById(R.id.btnLogOut);
//        btnLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                logout();
//            }
//        });

    }

//    private void logout(){
//        ParseUser.logOutInBackground();
//        // want to go to Log In (main) Activity with intent after successful log out
//        final Intent intent = new Intent(this.getActivity(), LoginActivity.class);
//        startActivity(intent);
//    }

    private void addPins(ArrayList<LatLng> points) {
        for(LatLng p: points)
            addMarker(p);
    }

    private static void changeStyle(){
        if(daymode){
            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                boolean success = map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json));

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
                        MapStyleOptions.loadRawResourceStyle(context, R.raw.retrostyle_json));

                if (!success) {
                    Log.e("MapsActivity", "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e("MapsActivity", "Can't find style.", e);
            }
        }
        daymode = !daymode;
    }

    protected static void loadMap(GoogleMap googleMap) {
        map = googleMap;

        if (map != null) {
            MapFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(thisMapFrag);
            MapFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(thisMapFrag);
        } else {
            Toast.makeText(context, "Error - Map was null!", Toast.LENGTH_SHORT).show();
        }

        map.setOnMarkerClickListener(thisMapFrag);
        map.setOnMyLocationButtonClickListener(thisMapFrag);

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
                //map.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
                //map.animateCamera(CameraUpdateFactory.zoomTo(13));
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

    public static void goToSearchedPlace(Place p){
        mSearchLocation = p.getLatLng();

        map.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
        map.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
    }

    public static void addMarker(LatLng p){
        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(p);
        markerOptions.title("Point of Interest");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLng(p));
        map.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));

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
        map.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
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

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        if (marker != null) {
            //Toast.makeText(getContext(), "Marker click registered", Toast.LENGTH_SHORT).show();

            MainActivity.timelineOpen(marker, geocoder);

        }

        return false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        mSearchLocation = mCurrentLocation;

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

        if ((mSearchLocation != null) &&
                (mSearchLocation.latitude == mCurrentLocation.latitude) &&
                (mSearchLocation.longitude == mCurrentLocation.longitude)) {

            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mSearchLocation = new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            if (mCurrentLocation == null) {
                mSearchLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }

            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        }
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

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return relativeDate;
    }
}
