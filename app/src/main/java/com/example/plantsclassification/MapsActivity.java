package com.example.plantsclassification;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
   public void markOnMap(double latitude,double longitude,String place)
    {
        LatLng latLng= new LatLng(latitude,longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng).title(place));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        Bundle bundle = getIntent().getExtras();
        double[] latitudes = bundle.getDoubleArray("latitudes");
        double[] longitudes = bundle.getDoubleArray("longitudes");
        String[] places = bundle.getStringArray("places");
        LatLng latLng= new LatLng(latitudes[1],longitudes[1]);
        mMap.addMarker(new MarkerOptions()
                .position(latLng).title(places[1]));
        mMap.getMaxZoomLevel();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        for(int i=2;i<latitudes.length;i++)
        {
            markOnMap(latitudes[i],longitudes[i],places[i]);
        }
    }
}
