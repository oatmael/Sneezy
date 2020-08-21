package com.app.sneezyapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
    ArrayList<SneezeItem> sneezeItems;
    private boolean setToWeekly;
    private boolean setToUser;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }// onCreate END

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setToWeekly = true;
        setToUser = true;
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
        //TODO check theme to apply dark styled-map

        addHeatMap();
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
            Log.e(CLASS_TAG,"MyLocation has been disabled");
        }
        else{
            //set starting location to default location
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(15));
//            Toast.makeText(getContext(), "myLocationDisabled",Toast.LENGTH_LONG).show();
            Log.e(CLASS_TAG,"MyLocation has been disabled");
        }

    }//onMapReady END

    private void addHeatMap(){
        List<LatLng> sneezeLocations = getLatLongList();
        Toast.makeText(getContext(),"SneezeLocationsList size: "+ sneezeLocations.size(),Toast.LENGTH_LONG).show();
        try {
            //Create a heat map tile provider and overlay
            mProvider = new HeatmapTileProvider.Builder().data(sneezeLocations).build();
            mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
        catch (NullPointerException ex){
            ex.printStackTrace();
            Log.e(CLASS_TAG, "SneezeLocations was empty" + ex);
            Toast.makeText(getContext(), "No Sneeze Data Provided", Toast.LENGTH_LONG).show();
        }
    }


    private void updateHeatMapOptions(){
        //TODO add UI elements to toggle day/week/month sneeze locations
    }

    //gets data from the repo and returns LatLong list
    private List<LatLng> getLatLongList(){
        List<LatLng> latLongList = makeList();
        List<SneezeItem> siList;
/*
        if(setToUser){
        //get user userdata
            if(setToWeekly){
                //get user weekly data
                siList = repo.getAllSneezeItems();
            }
            //get user daily data *change to monthly
            siList = repo.getAllSneezeItems();
        }
        else{
        //get all/global data
            if(setToWeekly) {//change to weekly data}
                //get all weekly data
                siList = repo.getAllSneezeItems();
            }
            //get weekly
            siList = repo.getAllSneezeItems();
        }*/
        siList = repo.getAllSneezeItems();//**TEMPORARY**
        siList.size();
        //Currently no data available
        RealmList<SneezeData> sdList;
        Location location;
        LatLng coords;

        List<LatLng> sneezeLocations = makeList();
        for(int i = 0; i< siList.size(); i++){
            sdList = siList.get(0).getSneezes();
            for(int j = 0; j < sdList.size(); j++ ){
                //Probably better to make method in SneezeData class to return just LatLong coords instead of Location object
                location = sdList.get(0).locationAsAndroidLocation();
                coords = new LatLng(location.getLatitude(), location.getLongitude());
                sneezeLocations.add(coords);
            }
        }

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

    private List<LatLng> makeList(){
        List<LatLng> list =  new List<LatLng>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(@Nullable Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<LatLng> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] a) {
                return null;
            }

            @Override
            public boolean add(LatLng latLng) {
                return false;
            }

            @Override
            public boolean remove(@Nullable Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends LatLng> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, @NonNull Collection<? extends LatLng> c) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public LatLng get(int index) {
                return null;
            }

            @Override
            public LatLng set(int index, LatLng element) {
                return null;
            }

            @Override
            public void add(int index, LatLng element) {

            }

            @Override
            public LatLng remove(int index) {
                return null;
            }

            @Override
            public int indexOf(@Nullable Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(@Nullable Object o) {
                return 0;
            }

            @NonNull
            @Override
            public ListIterator<LatLng> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<LatLng> listIterator(int index) {
                return null;
            }

            @NonNull
            @Override
            public List<LatLng> subList(int fromIndex, int toIndex) {
                return null;
            }
        };

        return list;
    }//makeList END

}//MapsFragment Class END