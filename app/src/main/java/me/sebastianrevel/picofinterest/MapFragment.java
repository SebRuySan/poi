package me.sebastianrevel.picofinterest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.graphics.Matrix;

import java.util.Calendar;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import me.sebastianrevel.picofinterest.Models.Pics;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.parse.Parse.getApplicationContext;

//import android.net.ParseException;

@RuntimePermissions
public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {

    interface Callback {
        /**
         * This method will be implemented by my activity, and my fragment will call this
         * method when there is a text change event.
         */
        void showNotification(String message);

    }

    static MapView mMapView;
    private static GoogleMap map;
    private OnFragmentInteractionListener mListener;
    private static int mRadius = 15;
    private static int mTimeframe = 5;
    private static Circle radiusCircle;
    private static float zoomLevel;

    public static LatLng mCurrentLocation;
    public static LatLng mSearchLocation;
    private LocationRequest mLocationRequest;

    // these are for the in app notifications
    public static String locmax;
    public static int picmax;
    public static ArrayList<Marker> markers;
    public static Marker mostpop;
    private Callback notifyCallback;
    static ProgressBar progressBar;
    static TextView uploadingtxt;
    ImageButton switchBtn;
    Boolean day = true;
    ImageButton tab;
    Dialog d;



    private static Context context;
    private static MapFragment thisMapFrag;
    static int PLACE_AUTOCOMPLETE_REQUEST_CODE = 132;

    private final static String KEY_LOCATION = "location";
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private long UPDATE_INTERVAL = 60000; // 60 seconds
    private long FASTEST_INTERVAL = 5000; // 5 seconds

    Switch swStyle; // this is the button to change the mapstyle
    Button btnLogout; // this is the button to log out
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

        daymode = true;

        context = getContext();
        thisMapFrag = MapFragment.this;
        tab = rootView.findViewById(R.id.options_tab);
        progressBar = rootView.findViewById(R.id.progress_bar);
        uploadingtxt = rootView.findViewById(R.id.uploading);
        progressBar.setVisibility(View.GONE);
        uploadingtxt.setVisibility(View.INVISIBLE);
        showMap();

        tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d = new Dialog(context);
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setContentView(R.layout.options_menu);
                d.setCanceledOnTouchOutside(false);
                switchBtn =  d.findViewById(R.id.style_btn);
                switchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (day) {
                            switchBtn.setImageResource(R.drawable.ic_day);
                            day = false;
                        } else {
                            switchBtn.setImageResource(R.drawable.ic_night);
                            day = true;
                        }

                        changeStyle();
                    }
                });
                ImageButton filterBtn = d.findViewById(R.id.filter_new_btn);
                filterBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        d.dismiss();
                        MainActivity.onFilterAction(view);
                    }
                });

                ImageView closeTab;

                ImageButton profileBtn = d.findViewById(R.id.profile_new_btn);
                profileBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            MainActivity.setScore();
                        } catch (ParseException exception) {
                            exception.printStackTrace();
                        }

                        // Create the scene
                        MainActivity.profileOpen();
                        d.dismiss();
                    }
                });


                closeTab = d.findViewById(R.id.close_tab);
                closeTab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        d.dismiss();
                    }
                });
                d.show();
            }
        });



        // Get the button view
        View locationButton = ((View) rootView.findViewById(1).getParent()).findViewById(2);
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 180, 300, 0);

        return rootView;
    }

    public static void setProgress(Boolean on) {
        if (on) {
            progressBar.setVisibility(View.VISIBLE);
            uploadingtxt.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            uploadingtxt.setVisibility(View.GONE);
        }
    }

    // initialize callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // `instanceof` here is how we check if the containing context (in our case the activity)
        // implements the required callback interface.
        //
        // If it does not implement the required callback, we want
        if (context instanceof Callback) {

            // If it is an instance of our Callback then we want to cast the context to a Callback
            // and store it as a reference so we can later update the callback when there has been
            // a text change event.
            notifyCallback = (Callback) context;
        } else {
            throw new IllegalStateException("Containing context must implement MapFragment.Callback.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Because we grabbed a reference to our containing context in on attach, it is approriate
        // to clean-up our references in onDetach() so that way we don't leak any references and
        // run into any odd runtime errors!
        notifyCallback = null;
    }


    public static void showMap() {
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setPadding(0, 50, 10, 0); // this is so the my location button is positioned better
                loadMap(googleMap);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                daymode = !daymode;
                changeStyle();

                // remove all current markers from map
                map.clear();

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

                markers = new ArrayList<Marker>();

                // Define the class we would like to Query
                ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class);
                // get all posts
                final Set<String> locs = new HashSet<String>(); // this will contain a no-duplicate set of locations hwere pictures have been taken
                final ArrayList<Pics> pictures = new ArrayList<>(); // this will contain a list of Pics (filtered so that it only contains the most recent picture taken at each location)
                query.orderByDescending("createdAt2"); // so query returns results in order of most recent pictures
                query.findInBackground(new FindCallback<Pics>(){
                    public void done(List<Pics> itemList, ParseException e){
                        Log.d("MapFragment", "Query done");
                       // Log.d("MapFragment", "ItemList array size : " + itemList.size());
                        // if no errors
                        if(e == null){
                            Log.d("MapFragment", "No errors in querying");

                            LatLng filterLoc = mCurrentLocation;

                            if (mSearchLocation != null) {
                                filterLoc = mSearchLocation;
                            }

                            ArrayList<Pics> filteredList = filterList(itemList, filterLoc);

                            for (Pics p : filteredList) {
                                boolean added = locs.add(p.getLocation());
                                Log.d("MapFragment", "Attempt to add to pictures");

                                if (added) {
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
                    //int[] date = p.getDate();
                    Date date = p.getCreatedDate();

                    Calendar today = Calendar.getInstance();
                    today.add(Calendar.DAY_OF_YEAR, -1); // subtract a day to only include today

                    Calendar yesterday = Calendar.getInstance();
                    yesterday.add(Calendar.DAY_OF_YEAR, -1); // sets to yesterday's date


                    Calendar weekAgo = Calendar.getInstance();
                    weekAgo.add(Calendar.DAY_OF_YEAR, -7); // sets to a week ago's date
                    weekAgo.add(Calendar.DAY_OF_YEAR, -1); // subtract a day to include the day of a week ago

                    Calendar monthAgo = Calendar.getInstance();
                    monthAgo.add(Calendar.MONTH, -1); // sets to a month ago's date
                    monthAgo.add(Calendar.DAY_OF_YEAR, -1); // subtract a day to include the day of a month ago

                    Calendar yearAgo = Calendar.getInstance();
                    yearAgo.add(Calendar.YEAR, -1); // sets to a year ago's date
                    yearAgo.add(Calendar.DAY_OF_YEAR, -1); // subtract a day to include the day of a year ago

                    switch (mTimeframe) {
                        case 0:

                            if (date.after(today.getTime())) {
                                filtered.add(p);
                            }

                            break;

                        case 1:

                            if (date.after(yesterday.getTime())) {
                                filtered.add(p);
                            }

                            break;

                        case 2:
                            if (date.after(weekAgo.getTime())) {
                                filtered.add(p);
                            }

                            break;

                        case 3:
                            if (date.after(monthAgo.getTime())) {
                                filtered.add(p);
                            }

                            break;

                        case 4:
                            if (date.after(yearAgo.getTime())) {
                                filtered.add(p);
                            }

                            break;

                        case 5:
                            filtered.add(p);

                            break;
                    }
                }
            }
        }

        if (filtered != null) {
        //    Toast.makeText(context, "filtered    not null: " + filtered.size(), 0).show();
            return filtered;
        } else {
        //    Toast.makeText(context, "filtered null", 0).show()
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
        ImageButton timelineTab =  view.findViewById(R.id.timeline);
        timelineTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.dl.openDrawer(Gravity.LEFT);
            }

        });

        // TODO: work on this
        if (map != null) {
         //   Toast.makeText(context, "map not null", 0).show();

            map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    mSearchLocation = mCurrentLocation;
                    return false;
                }
            });
        }

        new Thread(new Runnable() {
            public void run() {
                final ParseUser user = ParseUser.getCurrentUser();
                // Wait 6.5 seconds after app has been open to show notification
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException ignored) {
                }
                final String username;
                String un = "";
                try {
                    un = user.fetchIfNeeded().getString("username");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (un.equals(""))
                    username = "You";
                else
                    username = un;
//                tvmessage.setText(username + ", there is a Pic of Interest near you!");
//                cvMess.setVisibility(View.VISIBLE);


                Date currentTime = Calendar.getInstance().getTime();

                // create Calendar objects so we can check if pic object was created a day/week ago
                final Calendar yesterday = Calendar.getInstance();
                yesterday.add(Calendar.DAY_OF_YEAR, -1); // sets to yesterday's date
                final Calendar weekAgo = Calendar.getInstance();
                weekAgo.add(Calendar.DAY_OF_YEAR, -7); // sets to a week ago's date

                final String mess = "";
                user.put("lastnotification", currentTime);
                user.saveInBackground();

//                final String message = mess + " " + username + ", there is a Pic of Interest near you!"+ currentTime;
//                //tvmessage.setText(mess + " " + username + ", there is a Pic of Interest near you!"+ currentTime);
//                //cvMess.setVisibility(View.VISIBLE);
//                getActivity().runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        changeViews(message);
//
//                    }
//                });

//                changeViews(message);

                // Define the class we would like to Query
                ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class);
                // get all posts
                final Map<String, Integer> locs = new HashMap<String, Integer>(); // locs stores all locations where pictures have been taken in the last day (also stores the amount of pictures taken there in the last day)
                final Map<String, Integer> locs2 = new HashMap<String, Integer>(); // locs2 stores all locations where pictures have been taken in the last week (also stores the amount of pictures taken there in the last week)
                query.orderByDescending("createdAt2"); // so query returns results in order of most recent pictures
                query.findInBackground(new FindCallback<Pics>() {
                    public void done(List<Pics> itemList, ParseException e) {
                        String locationmax;
                        Log.d("MapFragment", "Query done");
                  //      Log.d("MapFragment", "ItemList array size : " + itemList.size());
                        LatLng from = mCurrentLocation;
                        // if no errors
                        if (e == null) {
                            Log.d("MapFragment", "No errors in querying");
                            for (Pics p : itemList) { // however, we don't just want to include all pics, we only want to count them if they happened recently and nearby

                                LatLng to = new LatLng(p.getLat(), p.getLong()); // this is the latlng location of the picture
                                double distanceMeters = SphericalUtil.computeDistanceBetween(from, to);
                                double distanceMiles = distanceMeters * 0.00062137;
                                if(distanceMiles >= 1)
                                    continue; // in other words, if it's not within walking distance from current location (we define this as 1 mile) then don't add to any list, go to next Pic

                                // now we want to check how recent the Pic object was created
                                Date date = p.getCreatedDate();
                                if (date.before(weekAgo.getTime())) {
                                    continue; // if Pic object was created over a week ago, then go to next Pic object
                                }
                                // else we want to add it to locs2 or increment the number of images taken at that location
                                if (locs2.containsKey(p.getLocation())) // hashmap contains the number of images taken at each address
                                    locs2.put(p.getLocation(), locs2.get(p.getLocation()) + 1);
                                else
                                    locs2.put(p.getLocation(), 1);

                                if (date.before(yesterday.getTime())) { //if image is taken before yesterday then continue
                                    continue;
                                }
                                // else then add it to locs or increment the number of images taken at that location
                                if (locs.containsKey(p.getLocation())) // hashmap contains the number of images taken at each address
                                    locs.put(p.getLocation(), locs.get(p.getLocation()) + 1);
                                else
                                    locs.put(p.getLocation(), 1);
                            }
                        } else {
                            Log.d("item", "Error: " + e.getMessage());
                        }
                        // now we want to find the location with the most images, both taken in last week and in last day
                        locationmax = "";
                        Integer most = 0;
                        // first we want to see if there is a location within walking distance with pictures taken in last day (and if so get the one with the most pictures)
                        for (Map.Entry<String, Integer> entry : locs.entrySet()) {
                            String key = entry.getKey();
                            Integer value = entry.getValue();
                            if (value > most) {
                                most = value;
                                picmax = most;
                                locationmax = key;
                                locmax = locationmax;
                            }
                        }
                        // now location max contains address with most images taken
                        locmax = locationmax;
                        picmax = most;
                        String timemess = " day!";

                        // but we also want to check if for some reason there hasn't been in any pictures taken in a location within walking distance in the past day
                        if(picmax == 0){
                            // if this is the case, then find the location within walking distance with the most pictures taken in past week
                            for (Map.Entry<String, Integer> entry : locs2.entrySet()) {
                                String key = entry.getKey();
                                Integer value = entry.getValue();
                                if (value > most) {
                                    most = value;
                                    picmax = most;
                                    locationmax = key;
                                    locmax = locationmax;
                                }
                            }
                            locmax = locationmax;
                            picmax = most;
                            timemess = " week!";
                        }



                        // iterate through all markers in map to find the one at the "pic of interest" location, aka the one with the most tagged pictures
                        for(Marker m: markers)
                            if(m.getTitle().equals(locmax)){
                                //simulateclick(m);
                                mostpop = m;
                                Log.d("Map Fragment", "Most Pop Initialized");
                                break;
                            }
                        Log.d("MapFragment", "Maxes set");
                        String mess = "";
                        if(username.length() > 7) // if the username is too long then don't include in printed message
                            mess = "There is a popular location near you! \nThere have been " + picmax + " pictures taken there in the last" + timemess + "\n";
                        else
                            mess = username + ", there's a popular place near you! \nThere have been " + picmax + " pictures taken there in the last" + timemess + "\n";
                        final String message = mess;
                        Log.d("MapFragment", "Message printed");
                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                //changeViews(message);
                                notifyCallback.showNotification(message);
                            }
                        });
                    }
                });

            }
        }).start();


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
                        MapStyleOptions.loadRawResourceStyle(context, R.raw.bentley_json));

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
                markers.add(driver_marker);
                Log.d("Map Fragment", markers.size() + " markers");
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

    }

    // this function is exactly like the last one except that this is called when a picture is uploaded/taken and results in the same ffect
    public static void addMarker(final Place p, ParseFile parseFile, boolean b) {
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
                markers.add(driver_marker);
                simulateclick(driver_marker);
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
                .resize(200, 200)
                .centerCrop()
                .transform(new CircleBubbleTransformation())
                .into(mTarget);
        Log.d("MapFragment", "Picasso hopefully done");
    }

    // this function is for when a picture is uploaded
    public static void addMarker(final Pics p, ParseFile parseFile, boolean b) {
        Target mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d("MapFragment", "Marker is being created");
                LatLng l = new LatLng(p.getLat(), p.getLong());
                Marker driver_marker = map.addMarker(new MarkerOptions()
                                .position(l)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                .title(p.getLocation())
                        //.snippet("test address")
                );
                Log.d("MapFragment", "Marker created");
                //map.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
                //map.animateCamera(CameraUpdateFactory.zoomTo(13));
                Log.d("MapFragment", "Camera zoomed in hopefully");
                markers.add(driver_marker);
                simulateclick(driver_marker);
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
                .resize(200, 200)
                .centerCrop()
                .transform(new CircleBubbleTransformation())
                .into(mTarget);
        Log.d("MapFragment", "Picasso hopefully done");
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

    public static boolean simulateclick(final Marker marker) {
        Log.d("MapFragment", "Simulate click called");
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        Log.d("MapFragment", "Check if marker is null");
        if (marker != null) {
            Log.d("MapFragment", "Marker not null");
            MainActivity.timelineOpen(marker, geocoder);
            map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            map.animateCamera(CameraUpdateFactory.zoomTo(15));

        }

        return false;
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("RESULTS", "Inside Map");

        super.onActivityResult(requestCode, resultCode, data);
    }
}
