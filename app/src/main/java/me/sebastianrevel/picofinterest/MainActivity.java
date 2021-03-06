package me.sebastianrevel.picofinterest;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.sebastianrevel.picofinterest.Models.Pics;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

//@RuntimePermissions
public class MainActivity extends AppCompatActivity implements FilterFragment.OnFilterInputListener, MapFragment.Callback{
    Toolbar toolbar;
    ActionBarDrawerToggle drawerToggle;
    RecyclerView rv;
    static DrawerLayout dl;
    static RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager lm;
    static MainActivity context;

    static ArrayList<Pics> arrayList = new ArrayList<>();
    static TextView locationTv;
    static String address;

    static Marker mMarker;
    static Geocoder mGeocoder;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 132;

    public static boolean mThisAddyOnly;
    public static boolean mSortByLikes;
    public static boolean mSortByScores;
    public static boolean mSortByFollowers;

    public static int mRadius = 15;
    public static int mTimeframe = 5;

    public static int likeScore, followScore, totalScore;

    static MapFragment mapFragment = new MapFragment();
    FragmentTransaction fragmentTransaction;

    static android.app.FragmentManager fragMang;

    GPSTracker gps; // to get location of pics taken with camera

    PlaceAutocompleteFragment placeAutoComplete;
    private Button uploadBtn, signoutBtn, profileBtn, archiveBtn, closeBtn;
    private ImageButton cameraBtn;
    private ImageButton searchBtn, refreshBtn;
    public static TextView profileTv, createdAtTv, userScoreTv, timeframeTv;
    private SwipeRefreshLayout swipeContainer;


    // views for notification
    private CardView cvMess;
    private TextView tvmessage;
    private TextView tvMostPop;

    // this is a string to store the filepath of an uploaded image, and use it to hopefully get the location where it was taken
    private String filepath;

    private ImageButton btnExit;
    private Vibrator v;

    private Uri mFileUri;

    // for filter, to be acessed from description activity
    public static Bitmap bitm;

    boolean on = true;
    boolean off = false;

    // activity request code to store image
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_GALLERY = 2;
    public static final int GET_DESCRIPTION = 12345;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private final static int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 120;
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

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

        searchBtn = findViewById(R.id.search_btn);
        refreshBtn = findViewById(R.id.refresh_btn);

        toolbar = findViewById(R.id.toolBar);
        archiveBtn = findViewById(R.id.archives_btn);
        profileBtn = findViewById(R.id.profile_btn);
        uploadBtn = findViewById(R.id.upload_btn);
        createdAtTv = findViewById(R.id.date_joined_tv);
        userScoreTv = findViewById(R.id.user_score_tv);
        //timeframeTv = findViewById(R.id.tvTimeframeOnMap);
        profileTv = findViewById(R.id.profile_name_tv);
        locationTv = findViewById(R.id.location_tv);
        dl = findViewById(R.id.drawerLayout);
        rv = findViewById(R.id.recyclerView);

        profileTv.setText(ParseUser.getCurrentUser().getUsername());
        userScoreTv.setText("User Score: " + ParseUser.getCurrentUser().getInt("userScore"));
        createdAtTv.setText("Joined: " + getRelativeTimeAgo(ParseUser.getCurrentUser().getCreatedAt().toString()));

        setSupportActionBar(toolbar);

        lm = new LinearLayoutManager(this);

        rv.setLayoutManager(lm);
        rv.setHasFixedSize(true);

        context = MainActivity.this;

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        closeBtn = findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dl.closeDrawer(Gravity.RIGHT);
            }
        });

        adapter = new PicAdapter(arrayList);
        rv.setAdapter(adapter);

        drawerToggle = new ActionBarDrawerToggle(this, dl, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerToggle = new ActionBarDrawerToggle(this,
                dl,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        dl.addDrawerListener(drawerToggle);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });
        refreshBtn.setOnClickListener(new View.OnClickListener() {  // refresh map and add markers
            @Override
            public void onClick(View view) {
                clear();
                try {
                    loadAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (ParseException f){

                }
                mapFragment.showMap();
            }
        });

        // initialize the cardview for messages but set as invisible
        cvMess = (CardView) findViewById(R.id.cvMess);
        cvMess.setVisibility(View.INVISIBLE); // it starts off as invisible but becomes visible when certain conditions are met
        // initialize the text view within the cardview
        tvmessage = (TextView) findViewById(R.id.tvMessage);
        tvmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapFragment.simulateclick(MapFragment.mostpop);
                Log.d("Map Fragment", "simulate click supposed to have been called by notification");
                // this is so that the "notification"/message goes away when the text is clicked.
                cvMess.animate().setDuration(250).alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cvMess.setVisibility(View.GONE);
                        //searchBtn.setVisibility(View.VISIBLE);

                    }
                });
                refreshBtn.animate().setDuration(250).alpha(1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        refreshBtn.setVisibility(View.VISIBLE);
                    }
                });
                searchBtn.animate().setDuration(250).alpha(1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        searchBtn.setVisibility(View.VISIBLE);
                    }
                });
                v.cancel(); // stop vibrating in case it hasn't already
            }
        });

        tvMostPop = (TextView) findViewById(R.id.tvMostPop);
        tvMostPop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Map Fragment", "simulate click supposed to have been called by notification");
                MapFragment.simulateclick(MapFragment.mostpop);
                // this is so that the "notification"/message goes away when the text is clicked.
                cvMess.animate().setDuration(250).alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cvMess.setVisibility(View.GONE);
                        //searchBtn.setVisibility(View.VISIBLE);
                    }
                });
                refreshBtn.animate().setDuration(250).alpha(1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        refreshBtn.setVisibility(View.VISIBLE);
                    }
                });
                searchBtn.animate().setDuration(250).alpha(1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        searchBtn.setVisibility(View.VISIBLE);
                    }
                });
                v.cancel();
            }
        });

        btnExit = (ImageButton) findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this is so that the "notification"/message goes away when the text is clicked.
                cvMess.animate().setDuration(250).alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cvMess.setVisibility(View.GONE);
                    }
                });
                refreshBtn.animate().setDuration(250).alpha(1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        refreshBtn.setVisibility(View.VISIBLE);
                    }
                });
                searchBtn.animate().setDuration(250).alpha(1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        searchBtn.setVisibility(View.VISIBLE);
                    }
                });
                v.cancel();
            }
        });

        clear();

        signoutBtn = (Button) findViewById(R.id.signout_btn);
        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                logout();
            }
        });

//        mapFragment.searchBtn = findViewById(R.id.search_btn);
//        mapFragment.searchBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    Intent intent =
//                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
//                                    .build(MainActivity.this);
//                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
//                } catch (GooglePlayServicesRepairableException e) {
//                    // TODO: Handle the error.
//                } catch (GooglePlayServicesNotAvailableException e) {
//                    // TODO: Handle the error.
//                }
//            }
//        });
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

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);

        }

        cameraBtn = findViewById(R.id.camera_btn);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog d = new Dialog(context);
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setContentView(R.layout.picture_menu);
                d.setCanceledOnTouchOutside(false);

                ImageButton cameraChooseBtn = d.findViewById(R.id.camera_decision_btn);
                cameraChooseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                        i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        startActivityForResult(i, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                        d.dismiss();
                    }
                });
                ImageButton galleryChooseBtn = d.findViewById(R.id.gallery_decision_btn);
                galleryChooseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onPickPhoto(view);
                        d.dismiss();
                    }
                });
                ImageView closePicTab = d.findViewById(R.id.close_tab_pic);
                closePicTab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        d.dismiss();
                    }
                });

                d.show();


            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onPickPhoto(view);

            }
        });

        dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);


        archiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ArchiveActivity.class);
                startActivity(i);
            }
        });


        try {
        //    firstLoad();
            setScore();
            setNumLikes();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
       // MapFragment.showMap();
        // Create the scene root for the scenes in this app
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flContainer, mapFragment);
        fragmentTransaction.commit();

        fragMang = getFragmentManager();

    }

    private void setNumLikes() {
        final ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class);
        query.findInBackground(new FindCallback<Pics>() {
            @Override
            public void done(List<Pics> objects, ParseException e) {
                if (e == null) {
                    for (Pics p : objects) {
                        p.setNumLikes();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void timelineOpen(Marker m, Geocoder g) {
        Log.d("Main Activity", "Timeline open called");
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

    public static void profileOpen() {

        dl.openDrawer(Gravity.RIGHT);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void logout() {
        ParseUser.logOutInBackground();
        // want to go to Log In (main) Activity with intent after successful log out
        final Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
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

    // this function will hopefully replace the function above
    public ParseFile bittobytetoparse(Bitmap imageBitmap) {

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, blob);
        byte[] bitmapdata = blob.toByteArray();
        final ParseFile imageFile = new ParseFile("image.png", bitmapdata);
        return imageFile;

    }

    // this function is called when picture is taken, it adds marker at image location (using phone's gps in gpstracker class) and adds it to Parse
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("RESULTS", "Inside Main");
        Log.e("RESULTS", String.valueOf(requestCode));
        Log.e("RESULTS", String.valueOf(resultCode));
        if (requestCode == MapFragment.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                mapFragment.goToSearchedPlace(place);
                place = null;
                mMarker = null;

                clear();
                try {
                    loadAll();
                } catch (ParseException exception) {
                    exception.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MapFragment.showMap();
                //Log.e("RESULTS", "Going to" + place.getAddress());
              //  Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE || requestCode == PICK_PHOTO_CODE) {

            gps = new GPSTracker(MainActivity.this);

            if (resultCode == RESULT_OK) {
                if (gps.canGetLocation()) {

                    final double latitude = gps.getLatitude();

                    final double longitude = gps.getLongitude();
//
//                    if (requestCode == GET_DESCRIPTION) {
//                        Pic descPic = data.getExtras().get("pic");
//                        String desc = data.getData().get
//                    }
                    // by this point we have the user's location so add a marker there
                    // we also want to add the image to Parse
                    if (requestCode == PICK_PHOTO_CODE) {
                        MapFragment.setProgress(on);
                       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        //        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        Toast.makeText(MainActivity.this,
                                "Removed Touch",
                                Toast.LENGTH_SHORT).show();

                        Log.e("UPLOAD", "returning");

                        Bitmap bm = null;

                        Log.d("Main Activity", "Got to point A");
                        Uri imageUri = data.getData();

              // USE THIS          final ParseFile pFile = new ParseFile(new File(String.valueOf(imageUri)));


                        try {

                            bm = BitmapFactory.decodeStream(getContentResolver()
                                    .openInputStream(imageUri));
                            //ImageProcessor imageProcessor = new ImageProcessor();

                            //bm = imageProcessor.doGreyScale(bm); // adds the greyscale filter on the image, this is automatic and only temporary

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

//                        //final ParseFile pFile = conversionBitmapParseFile(bm);
//                        final ParseFile pFile = bittobytetoparse(bm);

                        filepath = getRealPathFromUri(getApplicationContext(), imageUri);

                        // rotate bitmap if necesssary
                        BitmapFactory.Options bounds = new BitmapFactory.Options();
                        bounds.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(filepath, bounds);
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        Bitmap b = BitmapFactory.decodeFile(filepath, opts);
                        // Read EXIF Data
                        ExifInterface exif2 = null;
                        try {
                            exif2 = new ExifInterface(filepath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String orientString = exif2.getAttribute(ExifInterface.TAG_ORIENTATION);
                        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
                        int rotationAngle = 0;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
                        // Rotate Bitmap
                        Matrix matrix = new Matrix();
                        matrix.setRotate(rotationAngle, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);


                        //final ParseFile pFile = conversionBitmapParseFile(bm);
                        final ParseFile pFile = bittobytetoparse(rotatedBitmap);

                        boolean hasLoc = false;

                        try {
                            ExifInterface exif = new ExifInterface(filepath);
                            Log.d("Main Activity", "Got to point B");
                            String lat = ExifInterface.TAG_GPS_LATITUDE;
                            String lat_data = exif.getAttribute(lat); // this is the latitude of where the image was taken in a weird format
                            String lng = ExifInterface.TAG_GPS_LONGITUDE;
                            String lng_data = exif.getAttribute(lng); // this is the longitude of where the image was taken in a weird format
                            if(lat_data != null && lng_data != null) {
                                Log.d("Main Activity", lat_data);
                                Log.d("Main Activity", lng_data);
                                double lati = formatCoordinates(lat_data);
                                double longi = formatCoordinates(lng_data) * -1;
                                Log.d("Main Activity", "Formatted: " + lati);
                                Log.d("Main Activity", "Formatted: " + longi);

                                // set the location of the picture to be the reformatted gps coordinates
                                pic.setLat(lati);
                                pic.setLong(longi);
                                hasLoc = true;

                                Geocoder geocoder2 = new Geocoder(getApplicationContext(),
                                        Locale.getDefault());

                                List<Address> listAddresses2 = null;

                                try {

                                    listAddresses2 = geocoder2
                                            .getFromLocation(lati,
                                                    longi,
                                                    1);

                                    // set this address as the location of the picture
                                } catch (IOException e) {

                                    e.printStackTrace();

                                }
                                final String address2 = listAddresses2
                                        .get(0)
                                        .getAddressLine(0);
                                pic.setLocation(address2);
                                Log.d("Main Activity", address2);
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("Main Activity", "Problem with exifinterface");
                        }

                        final boolean foundLoc = hasLoc;

                        pFile.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                if (null == e) {
                                    Log.e("UPLOAD", "there is a file returned");

                                    if(!foundLoc) {

                                        pic.setLocation(address);
                                        pic.setLong(longitude);
                                        pic.setLat(latitude);

                                    }

                                    final ParseUser user = ParseUser.getCurrentUser();

                                    pic.setUser(user);
                                    pic.setPic(pFile);
                                    pic.setLike();
                                    pic.setNumLikeColumn();
                                    pic.setCreatedAt();


                                    mapFragment.addMarker(pic, pFile, true);

                                    pic.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {

                                            if (e == null) { // no errors
                                                MapFragment.setProgress(off);
                                               // getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                                Log.e("UPLOAD", "Added Image success!");

                                                Toast.makeText(MainActivity.this,
                                                        "Image added to Parse!",
                                                        Toast.LENGTH_SHORT).show();

                                                try {
                                                    setScore();
                                                } catch (ParseException exception) {
                                                    exception.printStackTrace();
                                                }

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

                    // get file using getoutput media file, and save it
                    File file = getOutputMediaFile(MEDIA_TYPE_IMAGE);

                    String path = file.getAbsolutePath();
                    /*
                    // get bitmap from file using bitmapfactory.decodefile
                    Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());

                    // this will hopefully compress the bitmap
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
                    Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

                    // assign the global static variable to be this
                    this.bitm = decoded;
                    final ParseFile parseFile = bittobytetoparse(decoded);

                    //final ParseFile parseFile = new ParseFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));
                    */
                    final ParseFile parseFile = new ParseFile(file);
                    parseFile.saveInBackground();
                    newPic.setPic(parseFile);
                    newPic.setLat(latitude);
                    newPic.setLong(longitude);
                    final ParseUser user = ParseUser.getCurrentUser();
                    newPic.setLike();
                    newPic.setUser(user);
                    newPic.setCreatedAt();
                    newPic.setNumLikeColumn();

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
                    mapFragment.addMarker(place, parseFile, true);

                    String mFilePath = getOutputMediaFileUri(MEDIA_TYPE_IMAGE).toString();
                    if (mFilePath != null) {
                        Log.e("PATH", "NOT NULL");
                        Intent intent = new Intent(MainActivity.this, DescriptionActivity.class);
                        //intent.putExtra("filepath", mFilePath);
                        intent.putExtra("pic", newPic);
                        intent.putExtra("path", path);
                        startActivity(intent);

                    }
//
//                    try {
//                        parseFile.save();
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                    Intent i = new Intent(MainActivity.this, DescriptionActivity.class);
//                    i.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
//                    startActivity(i);

                    // save Parse file in background (image)

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

    // this gets the uri of the image and returns the filepath that can be used to get the image's location if available
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // this method takes in a latitude/longitude in a weird format and uses math to format it to normal gps coordinates
    public static double formatCoordinates(String lat){ // we're going to call it lat but the same function works for longitude
        String[] words  = lat.split(","); // splits lat into three "words" like 51/1, "42/1", "234/2321"
        double[] nums = new double[3];
        for(int i = 0; i < 3; i ++){
            String w = words[i];
            double a = Double.parseDouble(w.substring(0,w.indexOf("/")));
            double b = Double.parseDouble(w.substring(w.indexOf("/") + 1));
            nums[i] = a/b;

        }
        double whole = nums[0];
        double seconds = nums[1] * 60 + nums[2];
        double fractional = seconds / 3600;
        return whole + fractional;
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

    public static void onFilterAction(View view) {
        FilterFragment filterDialog = new FilterFragment();
        filterDialog.show(fragMang, "FilterFragment");
    }

    @Override
    public void sendFilterInput(boolean thisAddyOnly, boolean sortByLikes, boolean sortByScores, boolean sortByFollowers, int radius, int timeframe) {
        Log.d(TAG, "sendFilterInput: got the input");
        //Toast.makeText(this, "Radius: " + radius + " Timeframe: " + timeframe, Toast.LENGTH_SHORT).show();

        mThisAddyOnly = thisAddyOnly;
        mSortByLikes = sortByLikes;
        mSortByScores = sortByScores;
        mSortByFollowers = sortByFollowers;

        mRadius = radius;
        mTimeframe = timeframe;

        mapFragment.setRadius(radius);
        mapFragment.setTimeframe(timeframe);

       // timeframeTv.setText("Results for " + FilterFragment.timeframes[mTimeframe]);

        if (mThisAddyOnly) {
            locationTv.setText(address + "\nShowing results for "
                    + FilterFragment.timeframes[mTimeframe].toLowerCase()
                    + "\n\t at this address only.");
        } else {
            if (mRadius > 1) {
                locationTv.setText(address + "\nShowing results for "
                        + FilterFragment.timeframes[mTimeframe].toLowerCase()
                        + "\n\t and up to " + mRadius + " miles away.");
            } else {
                locationTv.setText(address + "\nShowing results for "
                        + FilterFragment.timeframes[mTimeframe].toLowerCase()
                        + "\n\t and within walking distance.");
            }
        }
    }

    // for timestamp
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
//   }
    public static void firstLoad() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        clear();
        final ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class);
        query.findInBackground(new FindCallback<Pics>() {
            @Override
            public void done(List<Pics> objects, ParseException e) {
                if (e == null) {
                    if (objects == null) {
                        Log.d("CreateFragment", "Objects is null!");
                    } else {
                        Log.d("CreateFragment", "Adding pics: " + objects.size());
                        arrayList.addAll(objects);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
    public static void loadAll() throws IOException, ParseException {
        ;
        LatLng pos;

        // this essentially checks if the function was called after a marker was clicked
        //   or if it was called after filters were updated.
        // this is check is necessary for using the correct location (to filter and to display)
        if (mMarker != null) {
            pos = mMarker.getPosition();
            mapFragment.setmSearchLocation(pos.latitude, pos.longitude);
        } else {
            if (mapFragment.mSearchLocation != null) {
                pos = mapFragment.mSearchLocation;
            } else {
                pos = mapFragment.mCurrentLocation;
            }

//            if (isNotificationClick) {
//                pos = mapFragment.mCurrentLocation;
//            }
            //xpos = new LatLng(loc.getLatitude(), loc.getLongitude());
        }
        final Double lat = pos.latitude;
        final Double lon = pos.longitude;

        final LatLng latLng = new LatLng(lat, lon);

        if (mGeocoder != null) {
            List<Address> listAddresses = mGeocoder.getFromLocation(lat, lon, 1);
            address = listAddresses.get(0).getAddressLine(0);
        }

        if (mThisAddyOnly) {
            locationTv.setTextSize(14);
            locationTv.setText(address + "\nShowing results for "
                    + FilterFragment.timeframes[mTimeframe].toLowerCase()
                    + "\n\t at this address only.");

            final ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class).whereEqualTo("location", address);

            // sort by
            if (mSortByLikes || mSortByScores) {
                if (mSortByLikes) {
                    query.orderByDescending("number_of_likes");
                } else {
                    // TODO: sort by scores

                }

                query.addDescendingOrder("createdAt2");
            } else {
                query.orderByDescending("createdAt2"); // so query returns results in order of most recent pictures
            }

            query.findInBackground(new FindCallback<Pics>() {
                @Override
                public void done(List<Pics> objects, ParseException e) {
                    if (e == null) {
                        if (objects == null) {
                            Log.d("CreateFragment", "Objects is null!");
                        } else {
                            Log.d("CreateFragment", "Adding pics: " + objects.size());
                        }

                        ArrayList<Pics> filteredPics = mapFragment.filterList(objects, latLng);

                        clear();

                        if (filteredPics != null) {
                            if (filteredPics.isEmpty()) {
                                Toast.makeText(context, "No posts to show for this search. Adjust filters to view more.", Toast.LENGTH_LONG).show();
                            } else {
                                //Toast.makeText(context, "NUMBER pins to show: " + filteredPics.size(), Toast.LENGTH_SHORT).show();

                                if (mSortByFollowers) {
                                    for (Pics p : filteredPics) {
                                        String pUsername = "";

                                        try {
                                            pUsername = p.getUser().fetchIfNeeded().getString("username");
                                        } catch (ParseException excep) {
                                            excep.printStackTrace();
                                        }

                                        if (ParseUser.getCurrentUser().getList("following").contains(pUsername)) {
                                            arrayList.add(p);
                                        }
                                    }
                                } else {
                                    arrayList.addAll(filteredPics);
                                }
                            }
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
                locationTv.setTextSize(14);
                locationTv.setText(address + "\nShowing results for "
                        + FilterFragment.timeframes[mTimeframe].toLowerCase()
                        + "\n\t and up to " + mRadius + " miles away.");
            } else {
                locationTv.setTextSize(14);
                locationTv.setText(address + "\nShowing results for "
                        + FilterFragment.timeframes[mTimeframe].toLowerCase()
                        + "\n\t and within walking distance.");
            }

            ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class);

            // sort by
            if (mSortByLikes || mSortByScores) {
                if (mSortByLikes) {
                    query.orderByDescending("number_of_likes");
                } else {
                    // TODO: sort by scores
                }

                query.addDescendingOrder("createdAt2");
            } else {
                query.orderByDescending("createdAt2"); // so query returns results in order of most recent pictures
            }

            query.findInBackground(new FindCallback<Pics>() {
                public void done(List<Pics> itemList, ParseException e) {
                    Log.d(TAG, "Query done");

                    if (itemList == null) {
                        Log.d("CreateFragment", "Objects is null!");
                    } else {
                        Log.d(TAG, "ItemList array size : " + itemList.size());
                    }

                    // if no errors
                    if (e == null) {
                        Log.d(TAG, "No errors in querying");

                        ArrayList<Pics> filteredPics = mapFragment.filterList(itemList, latLng);

                        clear();

                        if (filteredPics != null) {
                            if (filteredPics.isEmpty()) {
                                Toast.makeText(context, "No posts to show for this search. Adjust filters to view more.", Toast.LENGTH_LONG).show();
                            } else {
                                if (mSortByFollowers) {
                                    for (Pics p : filteredPics) {
                                        String pUsername = "";

                                        try {
                                            pUsername = p.getUser().fetchIfNeeded().getString("username");
                                        } catch (ParseException excep) {
                                            excep.printStackTrace();
                                        }

                                        if (ParseUser.getCurrentUser().getList("following").contains(pUsername)) {
                                            arrayList.add(p);
                                        }
                                    }
                                } else {
                                    //Toast.makeText(context, "Number pins to show: " + filteredPics.size(), Toast.LENGTH_SHORT).show();
                                    arrayList.addAll(filteredPics);
                                }
                            }
                        } else {
                            arrayList.addAll(itemList);
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("item", "Error: " + e.getMessage());
                    }
                }

            });
        }
    }

    public static void setScore() throws ParseException {
        final ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class).whereEqualTo("user", ParseUser.getCurrentUser());
        final int amountOfPicsPostedScore = query.count() * 10;

        query.findInBackground(new FindCallback<Pics>() {
            @Override
            public void done(List<Pics> objects, ParseException e) {
                int likes = 0;

                if (e == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        likes += (int) (objects.get(i).get("number_of_likes"));

                        Log.e("USERSCORE", String.valueOf(likes));
                    }

                    likeScore = likes * 50;

                } else {
                    e.printStackTrace();
                }

                final ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                userQuery.findInBackground(
                        new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> objects, ParseException e) {
                                int followers = 0;

                                if (e == null) {
                                    for (int i = 0; i < objects.size(); i++) {
                                        List<String> followingList = objects.get(i).getList("following");
                                        boolean isFollower = false;

                                        if (followingList != null) { //dont count people you follow!!!!!
                                            isFollower = followingList.contains(ParseUser.getCurrentUser().getUsername());
                                        }

                                        if (isFollower) {
                                            followers++;
                                        }
                                    }

                                    followScore = followers * 100;

                                } else {
                                    e.printStackTrace();
                                }
                            }
                        }
                );

                totalScore = likeScore + followScore + amountOfPicsPostedScore;

                ParseUser user = ParseUser.getCurrentUser();
                user.put("userScore", totalScore);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        if (e == null) { // no errors

                            userScoreTv.setText("User Score: " + ParseUser.getCurrentUser().getInt("userScore"));
                            Log.e("SCORE", "Updated Score");

                        } else {

                            Log.e("Score", "Failed to update score");

                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    public static void clear() {
        arrayList.clear();
        adapter.notifyDataSetChanged();
    }

    public void showNotification(String message){
        tvmessage.setText(message);
        //cvMess.setVisibility(View.VISIBLE);
        // animate to fade in
        cvMess.setAlpha(0);
        cvMess.animate().setDuration(400).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cvMess.setVisibility(View.VISIBLE);
                //searchBtn.setVisibility(View.GONE);
            }
        });

        refreshBtn.animate().setDuration(450).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                refreshBtn.setVisibility(View.GONE);
            }
        });
        searchBtn.animate().setDuration(450).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                searchBtn.setVisibility(View.GONE);
            }
        });

        // Vibrate for 400 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        }else {
            //deprecated in API 26
            v.vibrate(400);
        }
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
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

}
