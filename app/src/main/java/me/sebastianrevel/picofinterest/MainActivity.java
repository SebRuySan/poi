package me.sebastianrevel.picofinterest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.sebastianrevel.picofinterest.Models.Pics;

//@RuntimePermissions
public class MainActivity extends AppCompatActivity implements FilterFragment.OnFilterInputListener {
    Toolbar toolbar;
    ActionBarDrawerToggle drawerToggle;
    RecyclerView rv;
    static DrawerLayout dl;
    static RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager lm;

    static ArrayList<Pics> arrayList = new ArrayList<>();
    static TextView location;

    Fragment mapFragment = new MapFragment();
    FragmentTransaction fragmentTransaction;

    GPSTracker gps; // to get location of pics taken with camera

    PlaceAutocompleteFragment placeAutoComplete;
    private Button cameraBtn;
    private Button uploadBtn;


    // activity request code to store image
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_GALLERY = 2;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private final static int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 120;
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    public final String APP_TAG = "MyCustomApp";

    // Request code to send to Google Play services to be returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("TEST", "On create called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);

        dl = findViewById(R.id.drawerLayout);

        uploadBtn = findViewById(R.id.upload_btn);

        location = findViewById(R.id.location_tv);

        lm = new LinearLayoutManager(this);

        rv = findViewById(R.id.recyclerView);

        rv.setLayoutManager(lm);

        rv.setHasFixedSize(true);


        lm = new LinearLayoutManager(MainActivity.this);

        rv.setLayoutManager(lm);

        rv.setHasFixedSize(true);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

        StrictMode.setVmPolicy(builder.build());

        adapter = new PicAdapter(arrayList);

        rv.setAdapter(adapter);

        drawerToggle = new ActionBarDrawerToggle(this,
                dl,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        dl.addDrawerListener(drawerToggle);


        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("You forgot to supply a Google Maps API key");
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);

        }

        cameraBtn = findViewById(R.id.camera_btn);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                startActivityForResult(i, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);

            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onPickPhoto(view);

            }
        });


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

        clear();

        loadAll();
    }


    public static void drawerOpen(Marker m, Geocoder g) throws IOException {

        LatLng pos = m.getPosition();

        final Double lat = pos.latitude;

        final Double lon = pos.longitude;

        List<Address> listAddresses = g.getFromLocation(lat, lon, 1);

        String address = listAddresses.get(0).getAddressLine(0);

        location.setTextSize(14);
        location.setText(address);

        final ParseQuery<Pics> query =
                ParseQuery
                        .getQuery(Pics.class)
                        .whereEqualTo("location", address);

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

        Log.e("ADDRESS", address);

        dl.openDrawer(Gravity.LEFT);
    }

    // gets the Uri from the output media file
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    // gets the output media file
    private File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                APP_TAG);

        File galleryStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_ALARMS),
                APP_TAG);


        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());

        File mediaFileName;

        if (type == MEDIA_TYPE_IMAGE) {

            mediaFileName = new File(mediaStorageDir.getPath() + ".jpg");

        } else if (type == MEDIA_GALLERY) {

            mediaFileName = new File(galleryStorageDir.getPath() + ".jpg");

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

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {

        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        byte[] imageByte = byteArrayOutputStream.toByteArray();

        ParseFile parseFile = new ParseFile("image_file.png", imageByte);

        return parseFile;
    }

    // this function is called when picture is taken, it adds marker at image location (using phone's gps in gpstracker class) and adds it to Parse
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE || requestCode == PICK_PHOTO_CODE) {

            gps = new GPSTracker(MainActivity.this);

            if (resultCode == RESULT_OK) {
                if (gps.canGetLocation()) {

                    final double latitude = gps.getLatitude();

                    final double longitude = gps.getLongitude();

                    // by this point we have the user's location so add a marker there
                    // we also want to add the image to Parse
                    if (requestCode == PICK_PHOTO_CODE) {

                        Log.e("UPLOAD", "returning");

                        Bitmap bm = null;

                        Uri imageUri = data.getData();

                        try {

                            bm = BitmapFactory.decodeStream(getContentResolver()
                                    .openInputStream(imageUri));

                        } catch (FileNotFoundException e) {

                            e.printStackTrace();

                        }

                        Geocoder geocoder = new Geocoder(getApplicationContext(),
                                Locale.getDefault());

                        List<Address> listAddresses = null;

                        try {

                            listAddresses = geocoder
                                    .getFromLocation(latitude,
                                            longitude,
                                            1);

                            // set this address as the location of the picture
                        } catch (IOException e) {

                            e.printStackTrace();

                        }
                        final String address = listAddresses
                                .get(0)
                                .getAddressLine(0);

                        final Pics pic = new Pics();

                        final ParseFile pFile = conversionBitmapParseFile(bm);

                        pFile.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                if (null == e) {
                                    Log.e("UPLOAD", "there is a file returned");

                                    pic.setLocation(address);

                                    pic.setLong(longitude);

                                    pic.setLat(latitude);

                                    final ParseUser user = ParseUser.getCurrentUser();

                                    pic.setUser(user);

                                    pic.setPic(pFile);

                                    pic.setLike();

                                    pic.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {

                                            if (e == null) { // no errors

                                                Log.e("UPLOAD", "Added Image success!");

                                                Toast.makeText(MainActivity.this,
                                                        "Image added to Parse!",
                                                        Toast.LENGTH_SHORT).show();

                                            } else {

                                                Log.e("UPLOAD", "Added Image FAILURE!");

                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                } else {

                                    e.printStackTrace();

                                }
                            }
                        });
                        return;
                    }

                    final Pics newPic = new Pics();

                    final ParseFile parseFile = new ParseFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));

                    // save Parse file in background (image)
                    parseFile.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            // If successful add image to Pics object
                            if (null == e) {

                                newPic.setPic(parseFile);
                                Log.e("PARSE", "INSIDE FIRST IF");

                                if (newPic.getPic() != null) {
                                    Log.e("PARSE", "INSIDE SECOND IF");

                                    // if added include the coordinates of picture
                                    Log.d("mainactivity", "there is a file returned");

                                    newPic.setLat(latitude);

                                    newPic.setLong(longitude);

                                    newPic.setLike();

                                    final ParseUser user = ParseUser.getCurrentUser();

                                    newPic.setUser(user);
                                    // now using coordinates, use geocoder get from location to get address of where picture was taken
                                    Geocoder geocoder = new Geocoder(getApplicationContext(),
                                            Locale.getDefault());

                                    Place place;

                                    try {

                                        List<Address> listAddresses = geocoder
                                                .getFromLocation(latitude, longitude, 1);

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

                                                Log.d("MainActivity", "Added Image success!");

                                                Toast.makeText(MainActivity.this,
                                                        "Image added to Parse!",
                                                        Toast.LENGTH_SHORT).show();

                                            } else {

                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                } else
                                    Log.d("mainactivity", "there is no file returned");

                                Log.d("mainactivity", "Pic save requested");

                            } else {

                                e.printStackTrace();

                                Log.d("Main Activity", "Pic save failed");

                            }
                        }
                    });
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_filter, menu);

        menu.getItem(0).setVisible(true);

        return true;
    }

    public void onFilterAction(MenuItem menuItem) {

        FilterFragment filterDialog = new FilterFragment();

        filterDialog.show(getFragmentManager(), "FilterFragment");

    }

    @Override
    public void sendFilterInput(String input) {

        Log.d("MainActivity", "sendFilterInput: got the input");

        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();

    }

    public void loadAll() {

        final ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class);

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

    public static void clear() {

        arrayList.clear();

        adapter.notifyDataSetChanged();

    }
//    public void likeSwitch() {
//        if (isLiked) {
//            likeBtn.setBackgroundResource(R.drawable.ic_star_off);
//        } else {
//            likeBtn.setBackgroundResource(R.drawable.ic_star_on);
//        }
//    }
}
