package com.app.sneezyapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.app.sneezyapplication.data.SneezeData;
import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private MapView mMapView;

    private GoogleMap googleMap;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String CLASS_TAG = "MapsFragment";
    private static final LatLng DEFAULT_LOCATION = new LatLng(-30, 133);
    FusedLocationProviderClient fusedLocationClient;
    SneezeRepository repo;
    LatLng userCoords;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }// onCreate END

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userCoords = DEFAULT_LOCATION;
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

        FloatingActionButton myLocationBtn = getView().findViewById(R.id.fab_my_location);
        myLocationBtn.setOnClickListener(v -> recenterUser());

        FloatingActionButton menuBtn = getView().findViewById(R.id.fab_menu);
        menuBtn.setOnClickListener(v -> openMenu());

        LinearLayout  mapMask = getView().findViewById(R.id.map_mask);
        mapMask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Toast.makeText(getContext(), "Map was touched",Toast.LENGTH_SHORT).show();
                closeMenu();
                return false;
            }
        });

    }//onViewCreated END


    public void openMenu(){

        ConstraintLayout mapsMenu = getView().findViewById(R.id.map_menu_layout);
        mapsMenu.setVisibility(View.VISIBLE);
        FloatingActionButton menuFab = getView().findViewById(R.id.fab_menu);
        menuFab.hide();
        LinearLayout  mapMask = getView().findViewById(R.id.map_mask);
        mapMask.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Menu open",Toast.LENGTH_SHORT).show();
    }

    public void closeMenu(){
        FloatingActionButton menuFab = getView().findViewById(R.id.fab_menu);
        menuFab.show();
        ConstraintLayout mapsMenu = getView().findViewById(R.id.map_menu_layout);
        mapsMenu.setVisibility(View.GONE);
        LinearLayout  mapMask = getView().findViewById(R.id.map_mask);
        mapMask.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Menu close",Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }//onSaveInstanceState END

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;

        checkNightMode();
        //check if location services are turned on
        if(isLocationOn()) {
            //Permission check to enable MyLocation marker and MyLocationButton
            if (myLocationPermissionCheck()) {
                //Initialize MyLocation button
                googleMap.setOnMyLocationButtonClickListener(this);
                googleMap.setOnMyLocationClickListener(this);
                //hide default MyLocation button
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                //set starting location to be users location
                Log.d(CLASS_TAG, "MyLocation has been enabled");
            }
        }
        else{
            //set starting location to default location
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(3));
            Log.d(CLASS_TAG,"MyLocation has been disabled");
        }

        updateFab();
//        addHeatMap();
        addPoints();
    }//onMapReady END


    private void checkNightMode(){
        SharedPref sharedPref = MainActivity.sharedPref;
        if(sharedPref.loadNightModeState()){
            try{
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style_dark_mode));
            }
            catch(Exception ex){
                ex.printStackTrace();
                Log.e(CLASS_TAG,"Map Style Failure: Dark mode could not be enabled\n"+ex);
            }
        }
    }//checkNightMode END

    private void updateHeatMapOptions(){
        //TODO add UI elements to toggle day/week/month sneeze locations
    }//updateHeatMapOptions END

    private void addHeatMap(){
        HeatmapTileProvider mProvider;
        TileOverlay mOverlay;

        /*
        //DUMMY DATA
        try {
            InputStream is = getContext().getAssets().open("au-towns-sample.csv");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String csvString = new String(buffer, StandardCharsets.UTF_8);
            String[] lines = csvString.split("\\r?\\n");

            for(int i = 0; i < lines.length; i++) {
                String[] coords = lines[i].split(",");
                sneezeLocations.add(new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1])));
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("DummyData", "File not found\n"+ e);
        }
        catch (Exception e){
            Log.e("DummyData", "Exception was thrown\n"+e);
        }
        */
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
    //add overlay of sneeze locations in
    private void addPoints(){
        List<LatLng> sneezeLocations = new ArrayList<>(getLatLongList());

        for(int i =0; i < sneezeLocations.size(); i++){
            LatLng sneezeLocation = sneezeLocations.get(i);
            googleMap.addMarker(new MarkerOptions()
                    .position(sneezeLocation));
        }
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
                    //Probably better to make method in SneezeData class to return just LatLong coords instead of Location object
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

    private boolean isLocationOn() {
        LocationManager lm = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //location is turned on
        //                Toast.makeText(getContext(), "Location services turned on",Toast.LENGTH_LONG).show();
        return gps_enabled && network_enabled;
//            Toast.makeText(getContext(), "Location services turned off",Toast.LENGTH_LONG).show();

        //location is not turned on

    }//isLocationEnabled END

    private boolean myLocationPermissionCheck(){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Permission not granted
            return false;
        }
        //Permission granted
        //enable MyLocation marker
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    userCoords = new LatLng(location.getLatitude(), location.getLongitude());
//                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(userCoords));
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(userCoords).zoom(15).build();
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                //TODO else: Request location update https://developer.android.com/training/location/receive-location-updates
            });
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        googleMap.setMyLocationEnabled(true);
        return true;
    }//myLocationPermissionCheck END

    private void recenterUser(){
//        String locationPermissionStatus = locationPermissionStatus();
        switch (locationPermissionStatus()){
            case "LocationPermissionDenied":
                //popup for location not permitted
                Toast.makeText(getContext(),"Location Permission Denied",Toast.LENGTH_LONG).show();
                //TODO popup to request location permission
                break;
            case "LocationOff":
                //popup for location services are not turned on
                Toast.makeText(getContext(),"Location Services Are Off",Toast.LENGTH_LONG).show();
                //TODO popup to make turn on location services
                break;
            case "LocationPermissionGranted":
                //animate camera to user location
//            Toast.makeText(getContext(),"LocationPermissionGranted",Toast.LENGTH_LONG).show();
                CameraPosition cameraPosition = new CameraPosition.Builder().target(userCoords).zoom(15).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                break;
            default:
                Log.e(CLASS_TAG,"recenterUser(): Unexpected value was returned from locationPermissionStatus method ");
        }//locationPermissionStatus switch END
    }//recenterUser END

    private String locationPermissionStatus(){
        if (!googleMap.isMyLocationEnabled()){
            if(!isLocationOn()){
                return "LocationOff";
            }
            else{
                return "LocationPermissionDenied";
            }
        }
        else{
            return "LocationPermissionGranted";
        }
    }//locationPermissionStatus END

    private void updateFab(){
        FloatingActionButton myLocationFab = getView().findViewById(R.id.fab_my_location);

        Drawable fabIcon;
        ColorStateList tintList;
        switch (locationPermissionStatus()){
            case "LocationPermissionDenied"://location not permitted
                fabIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_my_location_disabled, null);
                tintList =  ResourcesCompat.getColorStateList(getResources(), R.color.satelist_maps_fab_alt,null);
                break;
            case "LocationOff"://location services turned off
                fabIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_location_off, null);
                tintList =  ResourcesCompat.getColorStateList(getResources(), R.color.satelist_maps_fab_alt,null);
                break;
            case "LocationPermissionGranted"://location services on & permission granted

                fabIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_my_location, null);
                tintList = ResourcesCompat.getColorStateList(getResources(), R.color.statelist_maps_fab,null);
                break;
            default:
                Log.e(CLASS_TAG,"recenterUser(): Unexpected value was returned from locationPermissionStatus method ");
                fabIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_location_off, null);
                tintList =  ResourcesCompat.getColorStateList(getResources(), R.color.satelist_maps_fab_alt,null);
        }//locationPermissionStatus switch END

        myLocationFab.setBackgroundTintList(tintList);
        myLocationFab.setImageDrawable(fabIcon);
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