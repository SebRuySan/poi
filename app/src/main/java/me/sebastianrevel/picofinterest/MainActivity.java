package me.sebastianrevel.picofinterest;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.sebastianrevel.picofinterest.Models.Pics;

//@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    ActionBarDrawerToggle drawerToggle;
    RecyclerView rv;
    DrawerLayout dl;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager lm;
    ArrayList<String> arrayList = new ArrayList<>();

    Fragment mapFragment = new MapFragment();
    FragmentTransaction fragmentTransaction;

    GPSTracker gps; // to get location of pics taken with camera

    PlaceAutocompleteFragment placeAutoComplete;
    private Button cameraBtn;
    private final static int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 120;

    private final static String KEY_LOCATION = "location";
    private final static double CURRENT_LATITUDE = 47.629157;
    private final static double CURRENT_LONGITUDE = -122.341167;

    // activity request code to store image
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    public final String APP_TAG = "MyCustomApp";

    // Request code to send to Google Play services to be returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        rv = findViewById(R.id.recyclerView);

        lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        dl = findViewById(R.id.drawerLayout);
        rv.setHasFixedSize(true);

        String[] items = getResources().getStringArray(R.array.topics);

        for (String Item : items) {
            arrayList.add(Item);
        }

        adapter = new PicAdapter(arrayList);

        rv.setAdapter(adapter);

        drawerToggle = new ActionBarDrawerToggle(this, dl, toolbar, R.string.drawer_open, R.string.drawer_close);

        dl.addDrawerListener(drawerToggle);


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
                //Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                //i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
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
    /*
    // gets the Uri from the output media file
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }*/

    // gets the output media file
    private File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);


        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFileName;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFileName = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");

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
//                    BitmapDescriptor defaultMarker =
//                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
//                    LatLng picCoordinates = new LatLng(latitude, longitude);

                    Place place = new Place() {
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
                            return "New Pin thing";
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

//                    Marker marker = MapFragment.addMarker(new MarkerOptions()
//                            .position(picCoordinates)
//                            .title("Picture Location")
//                            .icon(defaultMarker));

                    MapFragment.addMarker(place);

                    //MapFragment.dropPinEffect(marker);


                    // we also want to add the image to Parse
                    final Pics newPic = new Pics();
                    /*
                    //newPic.setLocation(description); we don't have this implemented yet, but we could add a popup or edit text where the user can type a description of the location
                    final ParseFile parseFile = new ParseFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));
                    parseFile.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            // If successful save image as profile picture
                            if(null == e) {
                                newPic.setPic(parseFile);
                                Log.d("mainactivity", "Pic save requested");
                            }
                        }
                    });*/
                    //newPic.setUser(); TO DO : implement when we have log in/sign up
                    newPic.setLat(latitude);
                    newPic.setLong(longitude);
                    newPic.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) { // no errors
                                Log.d("MainActivity", "Added Image success!");
                                Toast.makeText(MainActivity.this, "Image added to Parse!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                e.printStackTrace();
                            }
                        }
                    });

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
}
