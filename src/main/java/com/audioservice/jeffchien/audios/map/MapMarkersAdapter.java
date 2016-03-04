package com.audioservice.jeffchien.audios.map;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.audioservice.jeffchien.audios.map.window.InformationDialog;
import com.audioservice.jeffchien.audios.rest.layouts.Resource;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MapMarkersAdapter implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener{

    private Activity mActivity;

    private Map<String, Resource> mResourceMap;

    public MapMarkersAdapter(Activity activity, Map<String, Resource> resourceMap){
        mActivity = activity;
        mResourceMap = resourceMap;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View windowView = View.inflate(mActivity, R.layout.view_marker_info_window, null);
        TextView title = (TextView)windowView.findViewById(R.id.marker_info_title);
        title.setText(marker.getTitle());

        return windowView;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        InformationDialog.Builder dialogBuilder = new InformationDialog.Builder()
                                    .setTitle(marker.getTitle());
        if(mResourceMap.containsKey(marker.getId())){
            Resource resource = mResourceMap.get(marker.getId());
            dialogBuilder.setDescription(resource.description)
                    .setLocation(resource.location.latitude, resource.location.longitude);
            try {
                dialogBuilder.setUploadTime(DateFormat.getDateTimeInstance().parse(resource.uploadTime));
            }catch (ParseException e){
                dialogBuilder.setUploadTime(new Date());
            }

            if(resource.attachments != null){
                dialogBuilder.setAttachmentIds(new ArrayList<>(resource.attachments));
            }else{
                dialogBuilder.setAttachmentIds(null);
            }
        }

        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        dialogBuilder.build().show(transaction, "detail");
    }

    @Override
    public View getInfoContents(Marker marker) {return null;}
}
