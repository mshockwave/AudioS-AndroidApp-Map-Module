package com.audioservice.jeffchien.audios.map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.audioservice.jeffchien.audios.rest.layouts.Resource;
import com.audioservice.jeffchien.audios.rest.layouts.ResourceList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainMapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MainMapActivity.class.getName();

    private GoogleMap mMap = null;

    private final Map<String, Resource> mResourceMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    final retrofit.Callback<ResourceList> mTotalResourceListCallback = new Callback<ResourceList>() {
        @Override
        public void onResponse(Response<ResourceList> response, Retrofit retrofit) {
            if(response.code() == 200 && response.body() != null){
                ResourceList resources = response.body();

                if(mMap != null){
                    mMap.clear();
                    mResourceMap.clear();

                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                    for(Resource resource : resources.resources) {
                        LatLng location = new LatLng(resource.location.latitude,
                                            resource.location.longitude);
                        boundsBuilder.include(location);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(location)
                                            .draggable(false)
                                            .title(resource.title));
                        mResourceMap.put(marker.getId(), resource);
                    }

                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 20));
                }
            }else{
                if(response.code() != 200){
                    Log.e(TAG, "Get all resource return code : " + response.code());
                    Log.e(TAG, "Message: " + response.message());
                }else{
                    Log.e(TAG, "Get all resource return body null");
                }
            }
        }

        @Override
        public void onFailure(Throwable t) {
            //TODO: Handle error
            t.printStackTrace();
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        if(mMap != null){
            IAudioSProxy.AudioSInterface.getAllResources().enqueue(mTotalResourceListCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        MapMarkersAdapter adapter = new MapMarkersAdapter(this, mResourceMap);
        mMap.setInfoWindowAdapter(adapter);
        mMap.setOnInfoWindowClickListener(adapter);
        // Add a marker in Sydney and move the camera
        /*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
        IAudioSProxy.AudioSInterface.getAllResources().enqueue(mTotalResourceListCallback);
    }
}
