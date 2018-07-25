package me.sebastianrevel.picofinterest;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.sebastianrevel.picofinterest.Models.Pics;

import static android.app.Activity.RESULT_OK;

//@RuntimePermissions
public class MainActivity extends AppCompatActivity implements FilterFragment.OnFilterInputListener{
    Toolbar toolbar;
    ActionBarDrawerToggle drawerToggle;
    RecyclerView rv;
    static DrawerLayout dl;
    static RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager lm;
    static ArrayList<Pics> arrayList = new ArrayList<>();
    static TextView location;
    static String address;

    static Marker mMarker;
    static Geocoder mGeocoder;

    private static boolean mThisAddyOnly = true;
    private static int mRadius = 15;
    private static int mTimeframe = 5;

    static MapFragment mapFragment = new MapFragment();
    FragmentTransaction fragmentTransaction;

    GPSTracker gps; // to get location of pics taken with camera

    PlaceAutocompleteFragment placeAutoComplete;
    private Button cameraBtn;
    private SwipeRefreshLayout swipeContainer;

    private final static String KEY_LOCATION = "location";
    private final static double CURRENT_LATITUDE = 47.629157;
    private final static double CURRENT_LONGITUDE = -122.341167;

    // activity request code to store image
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private final static int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 120;
    public final String APP_TAG = "MyCustomApp";

    // Request code to send to Google Play services to be returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static String TAG = "MainActivity";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("TEST", "On create called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        rv = findViewById(R.id.recyclerView);

        lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        dl = findViewById(R.id.drawerLayout);
        rv.setHasFixedSize(true);
        location = findViewById(R.id.location_tv);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        String[] items = getResources().getStringArray(R.array.topics);

//        for ( Item : items) {
//            arrayList.add(Item);
//        }

        adapter = new PicAdapter(arrayList);
        rv.setAdapter(adapter);

        drawerToggle = new ActionBarDrawerToggle(this, dl, toolbar, R.string.drawer_open, R.string.drawer_close);
        dl.addDrawerListener(drawerToggle);

        clear();
//        try {
//            loadAll();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clear();
                try {
                    loadAll();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright);

        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("You forgot to supply a Google Maps API key");
        }


        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        }
        //TODO - TRACK LOCATION AND STORE IMAGE
        cameraBtn = findViewById(R.id.camera_btn);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i,CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
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

    }

    public static void drawerOpen(Marker m, Geocoder g) {
        mMarker = m;
        mGeocoder = g;

        try {
            loadAll();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.e("ADDRESS", address);
        dl.openDrawer(Gravity.LEFT);
    }

    public void loadGeoPics(String address) {
        final ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class).whereEqualTo("location", address);
        query.findInBackground(new FindCallback<Pics>() {
            @Override
            public void done(List<Pics> objects, ParseException e) {
                if (e == null) {
                    if (objects == null) {
                        Log.d("CreateFragment", "Objects is null!");
                    } else {
                        Log.d("CreateFragment", "Adding pics: " + objects.size());
                    }

                    clear();
                    arrayList.addAll(objects);
                    adapter.notifyDataSetChanged();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    // gets the Uri from the output media file
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    // gets the output media file
    private File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);


        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFileName;
        if (type == MEDIA_TYPE_IMAGE) {
            //mediaFileName = new File(mediaStorageDir.getPath() + File.separator
            //        + "IMG_" + timeStamp + ".jpg");
            mediaFileName = new File(mediaStorageDir.getPath() +".jpg");
        } else {
            return null;
        }

        return mediaFileName;
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    // this function is called when picture is taken, it adds marker at image location (using phone's gps in gpstracker class) and adds it to Parse
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            gps = new GPSTracker(MainActivity.this);

            if (resultCode == RESULT_OK) {
                if (gps.canGetLocation()) {

                    final double latitude = gps.getLatitude();
                    final double longitude = gps.getLongitude();
                    // by this point we have the user's location so add a marker there
                    // we also want to add the image to Parse
                    final Pics newPic = new Pics();

                    //newPic.setLocation(description); we don't have this implemented yet, but we could add a popup or edit text where the user can type a description of the location
                    final ParseFile parseFile = new ParseFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));
                    // save Parse file in background (image)
                    parseFile.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            // If successful add image to Pics object
                            if (null == e) {
                                newPic.setPic(parseFile);
                                if (newPic.getPic() != null) {
                                    // if added include the coordinates of picture
                                    Log.d(TAG, "there is a file returned");
                                    newPic.setLat(latitude);
                                    newPic.setLong(longitude);
                                    // now using coordinates, use geocoder get from location to get address of where picture was taken
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    Place place;
                                    try {
                                        List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                                        if (null != listAddresses && listAddresses.size() > 0) {
                                            String address = listAddresses.get(0).getAddressLine(0);
                                            // set this address as the location of the picture
                                            newPic.setLocation(address);
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
                                                    return null;
                                                }

                                                @Override
                                                public Locale getLocale() {
                                                    return null;
                                                }

                                                @Override
                                                public CharSequence getName() {
                                                    return newPic.getLocation();
                                                }

                                                @Override
                                                public LatLng getLatLng() {
                                                    return new LatLng(latitude, longitude);
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

                                        } else {
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
                                                    return null;
                                                }

                                                @Override
                                                public Locale getLocale() {
                                                    return null;
                                                }

                                                @Override
                                                public CharSequence getName() {
                                                    return "Picture Location";
                                                }

                                                @Override
                                                public LatLng getLatLng() {
                                                    return new LatLng(latitude, longitude);
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
                                        }
                                    } catch (IOException f) {
                                        f.printStackTrace();
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
                                                return null;
                                            }

                                            @Override
                                            public Locale getLocale() {
                                                return null;
                                            }

                                            @Override
                                            public CharSequence getName() {
                                                return "Picture Location";
                                            }

                                            @Override
                                            public LatLng getLatLng() {
                                                return new LatLng(latitude, longitude);
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
                                    }
                                    MapFragment.addMarker(place, parseFile);
                                    // save the picture to parse
                                    newPic.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) { // no errors
                                                Log.d(TAG, "Added Image success!");
                                                Toast.makeText(MainActivity.this, "Image added to Parse!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                } else
                                    Log.d(TAG, "there is no file returned");
                                Log.d(TAG, "Pic save requested");
                            } else {
                                e.printStackTrace();
                                Log.d("Main Activity", "Pic save failed");
                            }
                        }
                    });
                    //newPic.setUser(); TO DO : implement when we have log in/sign up


                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    // Can't get location.
                    // GPS or network is not enabled.
                    // Ask user to enable GPS/network in settings.

                }

            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "Cancelled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Error!", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        menu.getItem(0).setVisible(true);
        return true;
    }

    public void onFilterAction(MenuItem menuItem) {
//        Intent intent = new Intent(this, FilterFragment.class);
//        intent.putExtra("isReply", false);
//        startActivityForResult(intent, 17);

        FilterFragment filterDialog = new FilterFragment();
        filterDialog.show(getFragmentManager(), "FilterFragment");
    }

    public void onFilterAction(View view) {
        FilterFragment filterDialog = new FilterFragment();
        filterDialog.show(getFragmentManager(), "FilterFragment");
    }

    @Override
    public void sendFilterInput(boolean thisAddyOnly, int radius, int timeframe) {
        Log.d(TAG, "sendFilterInput: got the input");
        //Toast.makeText(this, "Radius: " + radius + " Timeframe: " + timeframe, Toast.LENGTH_SHORT).show();

        mThisAddyOnly = thisAddyOnly;
        mRadius = radius;
        mTimeframe = timeframe;
        mapFragment.setRadius(radius);
        mapFragment.setTimeframe(timeframe);

        if (mThisAddyOnly) {
            location.setText(address + "\nShowing results for "
                    + FilterFragment.timeframes[mTimeframe].toLowerCase()
                    + "\n\t at this address only.");
        } else {
            if (mRadius > 1) {
                location.setText(address + "\nShowing results for "
                        + FilterFragment.timeframes[mTimeframe].toLowerCase()
                        + "\n\t and up to " + mRadius + " miles away.");
            } else {
                location.setText(address + "\nShowing results for "
                        + FilterFragment.timeframes[mTimeframe].toLowerCase()
                        + "\n\t and within walking distance.");
            }
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
//    public void loadGeoPics(String address) {
//        final ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class).whereEqualTo("location", address);
//        query.findInBackground(new FindCallback<Pics>() {
//            @Override
//            public void done(List<Pics> objects, ParseException e) {
//                if (e == null) {
//                    if (objects == null) {
//                        Log.d("CreateFragment", "Objects is null!");
//                    } else {
//                        Log.d("CreateFragment", "Adding pics: " + objects.size());
//                    }
//
//                    clear();
//                    arrayList.addAll(objects);
//                    adapter.notifyDataSetChanged();
//                } else {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
    public static void loadAll() throws IOException, ParseException {
//        final ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class);
//        query.findInBackground(new FindCallback<Pics>() {
//            @Override
//            public void done(List<Pics> objects, ParseException e) {
//                if (e == null) {
//                    if (objects == null) {
//                        Log.d("CreateFragment", "Objects is null!");
//                    } else {
//                        Log.d("CreateFragment", "Adding pics: " + objects.size());
//                    }
//
//                    clear();
//                    arrayList.addAll(objects);
//                    adapter.notifyDataSetChanged();
//                } else {
//                    e.printStackTrace();
//                }
//            }
//        });
        LatLng pos;
        pos = mMarker.getPosition();
//        if (mMarker != null && mGeocoder != null) {
//            pos = mMarker.getPosition();
//        } else {
//            Location loc = mapFragment.mCurrentLocation;
//            pos = new LatLng(loc.latitude, loc.getLongitude());
//        }

        final Double lat = pos.latitude;
        final Double lon = pos.longitude;

        final LatLng latLng = new LatLng(lat, lon);

        List<Address> listAddresses = mGeocoder.getFromLocation(lat, lon, 1);
        address = listAddresses.get(0).getAddressLine(0);

        if (mThisAddyOnly) {
            location.setText(address + "\nShowing results for "
                    + FilterFragment.timeframes[mTimeframe].toLowerCase()
                    + "\n\t at this address only.");

            final ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class).whereEqualTo("location", address);
            query.orderByDescending("createdAt"); // so query returns results in order of most recent pictures
            query.findInBackground(new FindCallback<Pics>() {
                @Override
                public void done(List<Pics> objects, ParseException e) {
                    if (e == null) {
                        if (objects == null) {
                            Log.d("CreateFragment", "Objects is null!");
                        } else {
                            Log.d("CreateFragment", "Adding pics: " + objects.size());
                        }

                        ArrayList<Pics> picsInRadius = MapFragment.filterList(objects, latLng);

                        clear();

                        if (picsInRadius.size() == 0) {
                            //Toast.makeText(MainActivity.this, "Number pins to show: " + picsInRadius.size(), Toast.LENGTH_SHORT).show();
                            arrayList.addAll(picsInRadius);
                        } else {
                            arrayList.addAll(objects);
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            if (mRadius > 1) {
                location.setText(address + "\nShowing results for "
                        + FilterFragment.timeframes[mTimeframe].toLowerCase()
                        + "\n\t and up to " + mRadius + " miles away.");
            } else {
                location.setText(address + "\nShowing results for "
                        + FilterFragment.timeframes[mTimeframe].toLowerCase()
                        + "\n\t and within walking distance.");
            }

            ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class);
            query.orderByDescending("createdAt"); // so query returns results in order of most recent pictures
            query.findInBackground(new FindCallback<Pics>(){
                public void done(List<Pics> itemList, ParseException e){
                    Log.d(TAG, "Query done");
                    Log.d(TAG, "ItemList array size : " + itemList.size());
                    // if no errors
                    if(e == null){
                        Log.d(TAG, "No errors in querying");

                        ArrayList<Pics> picsInRadius = MapFragment.filterList(itemList, latLng);

                        clear();

                        if (picsInRadius.size() == 0) {
                            //Toast.makeText(MainActivity.this, "Number pins to show: " + picsInRadius.size(), Toast.LENGTH_SHORT).show();
                            arrayList.addAll(picsInRadius);
                        } else {
                            arrayList.addAll(itemList);
                        }

                        adapter.notifyDataSetChanged();
                    }
                    else {
                        Log.d("item", "Error: " + e.getMessage());
                    }
                }

            });
        }
    }
    public static void clear() {
        arrayList.clear();
        adapter.notifyDataSetChanged();
    }
}
