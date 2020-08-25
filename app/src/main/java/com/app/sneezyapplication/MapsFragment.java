package com.app.sneezyapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.app.sneezyapplication.data.SneezeData;
import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeRepository;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;


public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private MapView mMapView;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private GoogleMap googleMap;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String CLASS_TAG = "MapsFragment";
    private static final LatLng DEFAULT_LOCATION = new LatLng(-30, 133);
    SneezeRepository repo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }// onCreate END

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateHeatMapOptions();
        this.repo = MainActivity.repo;

        // *** MapView requires that the Bundle you pass contain _ONLY_ MapView SDK objects or sub-Bundles. ***
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = view.findViewById(R.id.map_view);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
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

        checkNightMode();
        //TODO check if location services are enabled before MyLocation permission check

        //Permission check to enable MyLocation marker and MyLocationButton
        if (MyLocationPermissionCheck()) {
            //Initialize MyLocation button
            googleMap.setOnMyLocationButtonClickListener(this);
            googleMap.setOnMyLocationClickListener(this);
            //hide default MyLocation button
//            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            //set starting location to be users location
//            onMyLocationButtonClick();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));//***TEMPORARY
//            googleMap.moveCamera(CameraUpdateFactory.zoomTo(3));//***TEMPORARY
            Log.d(CLASS_TAG,"MyLocation has been enabled");
        }
        else{
            //set starting location to default location
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
//            googleMap.moveCamera(CameraUpdateFactory.zoomTo(3));
            Log.d(CLASS_TAG,"MyLocation has been disabled");
        }
        addHeatMap();
    }//onMapReady END

    private void addHeatMap(){
        //getLatLong list of coordinates
        List<LatLng> sneezeLocations = new ArrayList<>(getLatLongList());

        try {
            //Create a heat map tile provider and overlay
            mProvider = new HeatmapTileProvider.Builder().data(sneezeLocations).build();
            mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
        catch (NullPointerException ex){
            ex.printStackTrace();
            Log.e(CLASS_TAG, "SneezeLocations was empty" + ex);
        }
    }//addHeatMap END

    private void checkNightMode(){
        SharedPref sharedPref = MainActivity.sharedPref;
        if(sharedPref.loadNightModeState()){
            try{
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_dark_mode));
            }
            catch(Exception ex){
                ex.printStackTrace();
                Log.e(CLASS_TAG,"Map Style Failure: Dark mode could not be enabled\n"+ex);
            }
        }
    }//checkNightMode END

    private void updateHeatMapOptions(){
        //TODO add UI elements to toggle day/week/month sneeze locations
    }

    //gets data from the repo and returns LatLong list
    private ArrayList<LatLng> getLatLongList(){
        ArrayList<LatLng> latLongList = new ArrayList<>();
        List<SneezeItem> siList;

        siList = repo.getAllSneezeItems();//**TEMPORARY**
        RealmList<SneezeData> sdList;
        Location location;
        LatLng coords;

        try{
            for(int i = 0; i< siList.size(); i++){
                sdList = siList.get(i).getSneezes();
                for(int j = 0; j < sdList.size(); j++ ){
                    //Probably better to make method in SneezeData class to return just LatLong coords instead of Location objec
                    try{
                        location = sdList.get(j).locationAsAndroidLocation();
                        if(location != null){
//                            Log.d(CLASS_TAG,"locationAsAndroidLocation Not null || i count "+i+"|| j count "+j+" : \n"+location);
                              coords = new LatLng(location.getLatitude(), location.getLongitude());
                              latLongList.add(coords);
                        }
                        else{
                            Log.d(CLASS_TAG,"locationAsAndroidLocation Returned null");
                        }
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                        Log.e(CLASS_TAG, "getLatLongList: location as android location error\n"+ ex);
                    }//try-catch
                }//Inner for loop
            }//Main for loop
        }//try catch
        catch (Exception ex){
            ex.printStackTrace();
            Log.e(CLASS_TAG, "Error getting sneeze locations: \n"+ex);
        }//try-catch END
        return latLongList;
    }//getLatLongList END


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
    }

}//MapsFragment Class END