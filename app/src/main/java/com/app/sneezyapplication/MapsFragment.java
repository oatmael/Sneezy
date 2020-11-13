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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private MapView mMapView;

    private GoogleMap googleMap;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String CLASS_TAG = "MapsFragment";
    private static final LatLng DEFAULT_LOCATION = new LatLng(-30, 133);
    private static final int MAX_ZOOM = 14;
    private static final int POINT_SIZE = 65;

    FusedLocationProviderClient fusedLocationClient;
    SneezeRepository repo;
    LatLng userCoords;
    private ClusterManager<mapClusterItem> clusterManager;

    private enum eLocationPermission {GRANTED, DENIED, OFF}

    private enum UserScope {ALL, USER;
        UserScope fromString(String value){
            switch(value){
                case "ALL":
                    return ALL;
                case "USER":
                    return USER;
            }
            return ALL;
        }
    }

    private enum DateRange {WEEK, MONTH;
        DateRange fromString(String value){
            switch (value){
                case "WEEK":
                    return WEEK;
                case "MONTH":
                    return MONTH;
            }
            return WEEK;
        }
    }

    private enum Presentation {
        MARKER, HEATMAP;
        Presentation fromString(String value) {
            switch (value) {
                case "MARKER":
                    return MARKER;
                case "HEATMAP":
                    return HEATMAP;
            }
            return MARKER;
//            return null;//cant be bothered accounting for null values
        }
    }

    UserScope selectedUserScope;
    DateRange selectedDateRange;
    Presentation selectedPresentation;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);

    }// onCreate END

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userCoords = DEFAULT_LOCATION;
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

        LinearLayout mapMask = getView().findViewById(R.id.map_mask);
        mapMask.setOnClickListener(v -> closeMenu());
    }//onViewCreated END


    public void setupMenu(){
        //load mapPreferences saved from other sessions
        String[] mapPreferences = MainActivity.sharedPref.loadMapPreferences();
        //temp values to avoid null object reference (probably a better solution)
        selectedDateRange = DateRange.WEEK;
        selectedUserScope= UserScope.ALL;
        selectedPresentation = Presentation.HEATMAP;
        //assign actual values
        selectedDateRange = selectedDateRange.fromString(mapPreferences[0]);
        selectedUserScope = selectedUserScope.fromString(mapPreferences[1]);
        selectedPresentation = selectedPresentation.fromString(mapPreferences[2]);
        //assign correct values to menu radio buttons
        RadioGroup rg;
        rg = getView().findViewById(R.id.radioGMapDateRange);
        switch (selectedDateRange){
            case WEEK:
                rg.check(R.id.radio_weekly);
                break;
            case MONTH:
                rg.check(R.id.radio_monthly);
                break;
        }

        rg = getView().findViewById(R.id.radioGUserScope);
        switch (selectedUserScope){
            case ALL:
                rg.check(R.id.radio_all);
                break;
            case USER:
                rg.check(R.id.radio_user);
                break;
        }

        rg = getView().findViewById(R.id.radioGPresentation);
        switch (selectedPresentation){
            case MARKER:
                rg.check(R.id.radio_marker);
                break;
            case HEATMAP:
                rg.check(R.id.radio_heatmap);
                break;
        }
    }//setupMenu END

    public void closeMenu() {
        FloatingActionButton menuFab = getView().findViewById(R.id.fab_menu);
        menuFab.show();
        ConstraintLayout mapsMenu = getView().findViewById(R.id.map_menu_layout);
        mapsMenu.setVisibility(View.GONE);
        LinearLayout mapMask = getView().findViewById(R.id.map_mask);
        mapMask.setVisibility(View.GONE);
        //TODO replace with enable save button
        savePreferences();
    }//close menu END


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
        //limit zoom to not show houses and their numbers (<17 to not show houses)
        googleMap.setMaxZoomPreference(MAX_ZOOM);

        checkNightMode();
        //check if location services are turned on
        if (isLocationOn()) {
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
        } else {
            //set starting location to default location
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(3));
            Log.d(CLASS_TAG, "MyLocation has been disabled");
        }
        updateFab();
        setupMenu();
        updateMapOverlay();
    }//onMapReady END

    private void checkNightMode() {
        try {
            if (MainActivity.sharedPref.loadNightModeState()) {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style_dark_mode));
            }
            else {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style_light_mode));
            }
        } catch (Exception ex) {
        ex.printStackTrace();
        Log.e(CLASS_TAG, "Map Style Failure: correct map mode could not be enabled\n" + ex);
    }
    }//checkNightMode END

    private void updateMapOverlay() {
        //clear heat map/marker cache
        googleMap.clear();
        //add heat map or markers with new settings
        switch (selectedPresentation) {
            case MARKER:
//                addPoints();
                addClusterMarkers();
                break;
            case HEATMAP:
                addHeatMap();
                if(clusterManager != null){
                    clusterManager.clearItems();
                }
                break;
        }
    }//updateMapOverlay END

    private void addHeatMap() {
        HeatmapTileProvider mProvider;
        TileOverlay mOverlay;

        //getLatLong list of coordinates
        List<LatLng> sneezeLocations = new ArrayList<>(getLatLongList());

        try {
            //Create a heat map tile provider and overlay
            mProvider = new HeatmapTileProvider.Builder().data(sneezeLocations).build();
            mProvider.setRadius(POINT_SIZE);
            mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        } catch (NullPointerException | IllegalArgumentException ex) {
            ex.printStackTrace();
            Log.e(CLASS_TAG, "SneezeLocations was empty" + ex);
        }
    }//addHeatMap END

    private void addClusterMarkers() {
        setupClusterManager();
        addClusterPoints();
    }

    //add non-cluster markers of sneeze locations
    private void addPoints() {
        List<LatLng> sneezeLocations = new ArrayList<>(getLatLongList());

        for (int i = 0; i < sneezeLocations.size(); i++) {
            LatLng sneezeLocation = sneezeLocations.get(i);
            googleMap.addMarker(new MarkerOptions()
                    .position(sneezeLocation));
        }
    }//addPoints


    //gets data from the repo and returns LatLong list
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
        return gps_enabled && network_enabled;
    }//isLocationEnabled END

    private ArrayList<LatLng> getLatLongList() {
        ArrayList<LatLng> latLongList = new ArrayList<>();
//        List<SneezeItem> siList;

        SneezeRepository.Scope scope = SneezeRepository.Scope.COMBINED;
        switch (selectedUserScope) {
            case ALL:
                scope = SneezeRepository.Scope.COMBINED;
                break;
            case USER:
                scope = SneezeRepository.Scope.USER;
                break;
            //for now
        }//selectedUserScope switch END

        //get staring and current calendars
        Calendar currentCal = Calendar.getInstance();
        Calendar startCal = Calendar.getInstance();
        switch (selectedDateRange) {
            case WEEK:
                startCal.add(Calendar.DATE, -7);
                break;
            case MONTH:
                startCal.add(Calendar.MONTH, -1);
                break;
        }//selectedDateRange switch END
        //convert calendars to dates

        Date startCalAsDate = startCal.getTime();
        Date currentCalAsDate = currentCal.getTime();

//        ArrayList<SneezeItem> siList;
        try{
            List<SneezeItem> siList = repo.getSneezeItems(startCalAsDate, currentCalAsDate, scope);

            RealmList<SneezeData> sdList;
            Location location;
            LatLng coords;

            for (int i = 0; i < siList.size(); i++) {
                sdList = siList.get(i).getSneezes();
                for (int j = 0; j < sdList.size(); j++) {
                    //Probably better to make method in SneezeData class to return just LatLong coords instead of Location object
                    location = sdList.get(j).locationAsAndroidLocation();
                    if (location != null) {
                        coords = new LatLng(location.getLatitude(), location.getLongitude());
                        latLongList.add(coords);
                    } else {
                        Log.d(CLASS_TAG, "locationAsAndroidLocation Returned null");
                    }
                }//Inner for loop
            }//Main for loop
        }//try catch
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e(CLASS_TAG, "Error getting sneeze locations: \n" + ex);
        }//try-catch END
        return latLongList;
    }//getLatLongList END

    private boolean myLocationPermissionCheck() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission not granted
            return false;
        }
        //Permission granted - enable MyLocation marker
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    userCoords = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(userCoords).zoom(MAX_ZOOM).build();
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                //TODO else: Request location update https://developer.android.com/training/location/receive-location-updates
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        googleMap.setMyLocationEnabled(true);
        return true;
    }//myLocationPermissionCheck END

    private void recenterUser() {
        switch (locationPermissionStatus()) {
            case DENIED:
                Toast.makeText(getContext(), "Location Permission Denied", Toast.LENGTH_LONG).show();
                //TODO popup to request location permission
                break;
            case OFF:
                Toast.makeText(getContext(), "Location Services Are Off", Toast.LENGTH_LONG).show();
                //TODO popup to allow user to turn on location services
                break;
            case GRANTED:
                animateToUserLocation();
                break;
                default:
                    Log.e(CLASS_TAG, "recenterUser(): Unexpected value was returned from locationPermissionStatus method ");
        }//locationPermissionStatus switch END
    }//recenterUser END

    private void animateToUserLocation(){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    userCoords = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(userCoords).zoom(MAX_ZOOM).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }//IF END
            });//FusedLocationListener END
        }//IF END
    }

    private eLocationPermission locationPermissionStatus() {
        if (!googleMap.isMyLocationEnabled()) {
            if (!isLocationOn()) {
                return eLocationPermission.OFF;
            } else {
                return eLocationPermission.DENIED;
            }
        }
        else {
            return eLocationPermission.GRANTED;
        }
    }//locationPermissionStatus END

    public void openMenu() {
        //Show get view and set as visible
        ConstraintLayout mapsMenu = getView().findViewById(R.id.map_menu_layout);
        mapsMenu.setVisibility(View.VISIBLE);
        //Hide fab
        FloatingActionButton menuFab = getView().findViewById(R.id.fab_menu);
        menuFab.hide();
        //Add mask for behind menu
        LinearLayout mapMask = getView().findViewById(R.id.map_mask);
        mapMask.setVisibility(View.VISIBLE);

        //Radio group onChange listeners
        //User scope
        RadioButton markerRadioBtn = getView().findViewById(R.id.radio_marker);
        switch (selectedUserScope){
            case ALL:
                //enable marker button
                markerRadioBtn.setEnabled(false);
                markerRadioBtn.setVisibility(View.INVISIBLE);
                break;
            case USER:
                //enable marker button
                markerRadioBtn.setEnabled(true);
                markerRadioBtn.setVisibility(View.VISIBLE);
                break;
        }
        RadioGroup radioGUserScope = getView().findViewById(R.id.radioGUserScope);
        radioGUserScope.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_all:
                    RadioButton heatmapRadioBtn = getView().findViewById(R.id.radio_heatmap);
                    heatmapRadioBtn.setChecked(true);
                    selectedUserScope = UserScope.ALL;
                    markerRadioBtn.setEnabled(false);
                    markerRadioBtn.setVisibility(View.INVISIBLE);
                    break;
                case R.id.radio_user:
                    selectedUserScope = UserScope.USER;
                    markerRadioBtn.setEnabled(true);
                    markerRadioBtn.setVisibility(View.VISIBLE);
                    break;

            }
            //TODO call enableSaveBtn
            updateMapOverlay();
        });
        //Date range
        RadioGroup radioGDateRange = getView().findViewById(R.id.radioGMapDateRange);
        radioGDateRange.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_weekly:
                    selectedDateRange = DateRange.WEEK;
                    break;
                case R.id.radio_monthly:
                    selectedDateRange = DateRange.MONTH;
                    break;
            }
            //TODO call enableSaveBtn
            updateMapOverlay();
        });
        //Presentation
        RadioGroup radioGPresentation = getView().findViewById(R.id.radioGPresentation);
        radioGPresentation.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_marker:
                    selectedPresentation = Presentation.MARKER;
                    break;
                case R.id.radio_heatmap:
                    selectedPresentation = Presentation.HEATMAP;
                    break;
            }
            //TODO call enableSaveBtn
            updateMapOverlay();
        });
    }//openMenu END
    private void enableMenuSaveBtn(){
        //TODO enable menu save button
        //SetOnClickListener
    }

    private void savePreferences(){
        MainActivity.sharedPref.saveMapPreferences(selectedDateRange, selectedUserScope, selectedPresentation);
        //TODO disable menu save button
    }

    private void updateFab () {
            FloatingActionButton myLocationFab = getView().findViewById(R.id.fab_my_location);
            Drawable fabIcon;
            ColorStateList tintList;
            switch (locationPermissionStatus()) {
                case DENIED:
                    fabIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_my_location_disabled, null);
                    tintList = getAltFabStateList();
                    break;
                case OFF:
                    fabIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_location_off, null);
                    tintList = getAltFabStateList();
                    break;
                case GRANTED:
                    fabIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_my_location, null);
                    tintList = ResourcesCompat.getColorStateList(getResources(), R.color.statelist_maps_fab, null);
                    break;
                default:
                    Log.e(CLASS_TAG, "updateFab(): Unexpected value was returned from locationPermissionStatus method ");
                    fabIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_location_off, null);
                    tintList = getAltFabStateList();
            }//locationPermissionStatus switch END
            myLocationFab.setBackgroundTintList(tintList);
            myLocationFab.setImageDrawable(fabIcon);
        }

    private ColorStateList getAltFabStateList(){
            return ResourcesCompat.getColorStateList(getResources(), R.color.satelist_maps_fab_alt, null);
        }

    //OnMapReadyCallback METHODS
    @Override
    public void onResume () {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart () {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop () {
        super.onStop();
        mMapView.onStop();
    }
    @Override
    public void onPause () {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy () {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory () {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    //MyLocation METHODS
    @Override
    public boolean onMyLocationButtonClick () {
        return false;
    }
    @Override
    public void onMyLocationClick (@NonNull Location location){
    }

    private void setupClusterManager(){
        clusterManager = new ClusterManager<mapClusterItem>(getContext(), googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
        addClusterPoints();
        clusterManager.cluster();
    }

    private void addClusterPoints(){
        try{
            List<LatLng> sneezeLocations = new ArrayList<>(getLatLongList());
            LatLng latLng;
            double lat;
            double lng;
            for(int i=0; i < sneezeLocations.size(); i++){
                double offset = i /60d;
                latLng = sneezeLocations.get(i);
                lat = latLng.latitude + offset;
                lng = latLng.longitude + offset;
                mapClusterItem offsetItem = new mapClusterItem(lat, lng, "Tile "+i, "Snippet"+i);
                clusterManager.addItem(offsetItem);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Log.e(CLASS_TAG, "Could not add cluster points"+ e);
        }
    }

    private class mapClusterItem implements ClusterItem{
        private final LatLng position;
        private final String title;
        private final String snippet;

        public mapClusterItem(double lat, double lng, String title, String snippet) {
            position = new LatLng(lat, lng);
            this.title = title;
            this.snippet = snippet;
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return position;
        }

        @Nullable
        @Override
        public String getTitle() {
            return title;
        }

        @Nullable
        @Override
        public String getSnippet() {
            return snippet;
        }
    }

    //DUMMY DATA - just return this method in getLatLongList()
    private ArrayList<LatLng> getDummyLatLongs() {
        ArrayList<LatLng> sneezeLocations = new ArrayList<>();
        try {
            InputStream is = getContext().getAssets().open("au-towns-sample.csv");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String csvString = new String(buffer, StandardCharsets.UTF_8);
            String[] lines = csvString.split("\\r?\\n");

            for (int i = 0; i < lines.length; i++) {
                String[] coords = lines[i].split(",");
                sneezeLocations.add(new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1])));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(CLASS_TAG, "DummyData: File not found\n" + e);
        } catch (Exception e) {
            Log.e(CLASS_TAG, "DummyData: Exception was thrown\n" + e);
        }
        return sneezeLocations;
    }
}//MapsFragment Class END