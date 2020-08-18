package com.app.sneezyapplication;

import android.graphics.Color;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private MapView mMapView;
    private HeatmapTileProvider mProvider;
    TileOverlay mOverlay;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String CLASS_TAG ="MapsFragment";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = view.findViewById(R.id.map_view);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //dummy data**
        ArrayList<LatLng> sneezeLocations = new ArrayList<>();
        double[] lats = {-37.1886, -37.8361, -38.4034, -38.7597, -36.9672};
        double[] lngs = {145.708, 144.845, 144.192, 143.67, 141.083};

        //make a list of sneeze locations with lat & lng values
        for (int i = 0; i < lats.length; i++) {
            sneezeLocations.add(new LatLng(lats[i],lngs[i]));
        }

        //Create a heat map tile provider and overlay
        try {
            mProvider = new HeatmapTileProvider.Builder().data(sneezeLocations).build();
            mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
        catch (Exception ex){
            Log.e(CLASS_TAG," Heat map could not be generated:\n\t"+ex);
        }

        //TODO show user location
        //TODO set starting location at user location

        //Other customisations..
        /*
        // Create Custom Gradient
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);

// Create the tile provider.
        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .gradient(gradient)
                .build();

        // Add the tile overlay to the map.
        mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        //To change the opacity of an existing heatmap:
        mProvider.setOpacity(0.7);
        mOverlay.clearTileCache();
    }//onMapReady END

    private void addHeatMap(GoogleMap googleMap) {
        List<LatLng> list = null;
        double[] lats = {-37.1886, -37.8361, -38.4034, -38.7597, -36.9672};
        double[] lngs = {145.708, 144.845, 144.192, 143.67, 141.083};
        //make a list of locations with lat & lng values
        for (int i = 0; i < lats.length; i++) {
            LatLng location = new LatLng(lats[i], lngs[i]);
            list.add(location);
        }

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().data(list).build();
        // Add a tile overlay to the map, using the heat map tile provider.
        Object mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
*/
    }//onMapReady END

    //TODO add function to chose between day/week/month sneeze locations
    //TODO ADD floating action button to center map at location
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

}