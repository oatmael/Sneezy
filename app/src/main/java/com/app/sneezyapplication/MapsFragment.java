package com.app.sneezyapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private MapView mMapView;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private GoogleMap googleMap;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String CLASS_TAG = "MapsFragment";
    private static final LatLng DEFAULT_LOCATION = new LatLng(-30, 133);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // *** MapView requires that the Bundle you pass contain _ONLY_ MapView SDK objects or sub-Bundles. ***
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = view.findViewById(R.id.map_view);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

//        FloatingActionButton myLocationBtn = getView()).findViewById(R.id.btn_my_location);
//        myLocationBtn.setOnClickListener(v ->
//                        onMyLocationButtonClick()
//        );
    }//onViewCreated END

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;

        //============== dummy data ==============
        ArrayList<LatLng> sneezeLocations = new ArrayList<>();
        double[] lats = {-37.1886, -37.8361, -38.4034, -38.7597, -36.9672};
        double[] lngs = {145.708, 144.845, 144.192, 143.67, 141.083};

        //make a list of sneeze locations with lat & lng values
        for (int i = 0; i < lats.length; i++) {
            sneezeLocations.add(new LatLng(lats[i], lngs[i]));
        }


        //============== dummy data ==============
        //TODO Try-Catch for no data retrieved
        //TODO check theme to apply dark styled-map
        //Create a heat map tile provider and overlay
        mProvider = new HeatmapTileProvider.Builder().data(sneezeLocations).build();
        mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        //TODO check if location services are enabled before MyLocation permission check

        //Permission check to enable MyLocation and MyLocationButton
        if (MyLocationPermissionCheck()) {
            //Initialize MyLocation button
            Toast.makeText(getContext(), "myLocationEnabled",Toast.LENGTH_LONG).show();
            googleMap.setOnMyLocationButtonClickListener(this);
            googleMap.setOnMyLocationClickListener(this);
            //hide default MyLocation button
//            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            //set starting location to be users location
//            onMyLocationButtonClick();
        }
        else{
            //set starting location to default location
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));

            /*
            //set MyLocationButton drawable to disabled
            FloatingActionButton myLocationBtn = getView().findViewById(R.id.btn_my_location);
            Drawable myLocationDisabled = getResources().getDrawable(R.drawable.ic_my_location_disabled);
            myLocationBtn.setImageDrawable(myLocationDisabled);

        //Set location services disabled
//        Drawable locationDisabled = getResources().getDrawable(R.drawable.ic_my_location_disabled);//<<<< Location services off drawable
//        myLocationBtn.setImageDrawable(locationDisabled);
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_enabled}, // disabled
        };
        int[] colors = new int[] {
                R.color.google_blue,
                R.color.darkButtonMain
        };
        ColorStateList tintList = new ColorStateList(states, colors);
        myLocationBtn.setBackgroundTintList(tintList);
        */

            Toast.makeText(getContext(), "myLocationDisabled",Toast.LENGTH_LONG).show();
        }

    }//onMapReady END

    private void changeTest(){

    }
//    private void addHeatMap(GoogleMap googleMap) { }

    //TODO add function to chose between day/week/month sneeze locations

    private boolean MyLocationPermissionCheck(){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Permission not granted
            return false;
        }
        //Permission granted
        //enable MyLocation marker
        googleMap.setMyLocationEnabled(true);
        return true;

    }
    //OnMapReadyCallback METHODS
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }
    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    //MyLocation METHODS
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        /*
//        Toast.makeText(getContext(),"MyLocationBtn executed",Toast.LENGTH_LONG).show();

        if(location != null) { // Check to ensure coordinates aren't null, probably a better way of doing this...
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }

         */
    }

}//MapsFragment Class END