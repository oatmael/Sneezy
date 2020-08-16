package com.app.sneezyapplication;

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

public class MapsFragment extends Fragment implements OnMapReadyCallback {

//    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        MapFragment.onCreate(savedInstanceState);
        try{
            MapView mapView = getView().findViewById(R.id.map_view);
            mapView.getMapAsync(this);
            Log.e("MapsFragment","getMapAsync() executed successfully");
        }
        catch (Exception ex){
            Log.e("MapsFragment","getMapAsync() Threw an exception:\n"+ex);
        }
        Toast.makeText(getContext(),"getMapAsync called", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //TODO call get map async
//        mMap = googleMap;
//
//
//        LatLng marker = new LatLng(-33.867, 151.206);
//
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 13));
//
//        googleMap.addMarker(new MarkerOptions().title("Hello Google Maps!").position(marker));
        Toast.makeText(getContext(),"onMap ready called", Toast.LENGTH_LONG).show();

    }

}