package com.example.vuquang.goncheck;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.graphics.*;
import android.widget.*;
import android.view.*;

import com.example.library.SlidingUpPanelLayout;
import com.example.library.SlidingUpPanelLayout.PanelSlideListener;
import com.example.library.SlidingUpPanelLayout.PanelState;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static com.example.vuquang.goncheck.R.id.map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "DemoActivity";
    private static final int CAMERA_REQUEST = 1888;
    private static final int S_WIDTH = 120;
    private static final int S_HEIGHT = 120;
    private static final int B_WIDTH = 600;
    private static final int B_HEIGHT = 600;
    public static final String pathAlbum = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES) + "/" + "GoNCheck/";

    String[] items = {"VietNam", "ThaiLand", "Laos"};
    SearchView txtSearchValue;

    private SlidingUpPanelLayout mLayout;

    private GoogleMap mMap;
    public
    ViewGroup scrollViewgroup;
    ImageView icon;
    ImageView imageSelected;
    Button location;
    //Small image
    Bitmap[] thumbnails = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        imageSelected = (ImageView) findViewById(R.id.imageSelected);

        scrollViewgroup = (ViewGroup) findViewById(R.id.viewgroup);

        //load images to thumbails and largeimgae
        loadPic();
        loadSlidePanel();
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        location = (Button)findViewById(R.id.buttonlocation);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TuiDangODau();
            }
        });
        mLayout.addPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(PanelState.COLLAPSED);
            }
        });

        TextView t = (TextView) findViewById(R.id.name);
        t.setText(Html.fromHtml(getString(R.string.position)));
//        Button f = (Button) findViewById(R.id.follow);
//        f.setText(Html.fromHtml(getString(R.string.follow)));
//        f.setMovementMethod(LinkMovementMethod.getInstance());
//        f.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse("http://www.twitter.com/umanoapp"));
//                startActivity(i);
//            }
//        });
    }

    private void loadSlidePanel() {
        if (thumbnails != null) {

            for (int i = 0; i < thumbnails.length; i++) {
                //create single frames [icon & caption] using XML inflater
                final View singleFrame = getLayoutInflater().inflate(
                        R.layout.frame_icon_scroll_view, null);
                //frame: 0, frame: 1, frame: 2, ... and so on
                singleFrame.setId(i);
                //internal plumbing to reach elements inside single frame
                ImageView icon = (ImageView) singleFrame.findViewById(R.id.icon);
                //put data [icon, caption] in each frame
                icon.setImageBitmap(Bitmap.createScaledBitmap(thumbnails[i], S_WIDTH, S_HEIGHT, false));
                //add frame to the scrollView
                scrollViewgroup.addView(singleFrame);
                //each single frame gets its own click listener
                singleFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLargeImage(singleFrame.getId());
                    }
                }); // listener
            }// for – populating ScrollView
        }
    }

    //display a high-quality version of the image selected using thumbnails
    protected void showLargeImage(int frameId) {
        Drawable selectedLargeImage = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(thumbnails[frameId], B_WIDTH, B_HEIGHT, false));
        imageSelected.setBackground(selectedLargeImage);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        TuiDangODau();


    }

    private void TuiDangODau() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (lastLocation != null)
        {
            LatLng latLng=new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//Thêm MarketOption cho Map:
            MarkerOptions option=new MarkerOptions();
            option.title("Chỗ Tui đang ngồi đó");
            option.snippet("Gần làng SOS");
            option.position(latLng);
            Marker currentMarker= mMap.addMarker(option);
            currentMarker.showInfoWindow();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        super.onCreateOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                items)); // set the adapter

        MenuItem itemSearch = menu.findItem(R.id. action_search);
        txtSearchValue = (SearchView) MenuItemCompat.getActionView(itemSearch);
        // set searchView listener (look for text changes, and submit event)
        txtSearchValue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast. makeText(getApplicationContext(), "1-SUBMIT..." + query,
                        Toast. LENGTH_SHORT).show();
                // recreate the 'original' ActionBar (collapse the SearchBox)
                invalidateOptionsMenu();
                // clear searchView text
                txtSearchValue.setQuery("", false);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
            // accept input one character at the time
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
// user clicked a menu-item from ActionBar
// perform SEARCH operations...
        switch (item.getItemId()){
            case R.id.action_camera: {
                cameraAct();
                return true;
            }//Camera
            case R.id.action_search:{

                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == PanelState.EXPANDED || mLayout.getPanelState() == PanelState.ANCHORED)) {
            mLayout.setPanelState(PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }//SlidePanel

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            imageSelected.setImageBitmap(Bitmap.createScaledBitmap(
//                    photo,
//                    imageSelected.getWidth(),
//                    imageSelected.getHeight(),
//                    false));
            saveImage(photo);
            loadPic();
        }
    }//Camera load imageselected and save/load images

    private void loadPic() {
        thumbnails = loadImages();
        loadSlidePanel();
    }

    private void cameraAct(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }//Camera

    private void saveImage(Bitmap finalBitmap) {

        File myDir = new File(pathAlbum);
        myDir.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +timeStamp +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//Camera

    private Bitmap[] loadImages() {
        File path = new File(pathAlbum);
        String[] fileNames = null;
        if(path.exists())
        {
            fileNames = path.list();
        }
        else {//thoat
            return null;
        }
        Bitmap[] bitmaps = new Bitmap[fileNames.length];
        for(int i = 0; i < fileNames.length; i++)
        {
            Bitmap mBitmap = BitmapFactory.decodeFile(pathAlbum+"/"+ fileNames[i]);
            bitmaps[i] = mBitmap;
            ///Now set this bitmap on imageview
        }
        return bitmaps;

    }//Camera

//    private void cameraAct(){
//        Intent intent = new Intent(this,PhotoIntentActivity.class);
//        startActivity(intent);
//    }
}
