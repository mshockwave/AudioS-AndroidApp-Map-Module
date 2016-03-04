package com.audioservice.jeffchien.audios.map.window;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.audioservice.jeffchien.audios.map.R;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InformationDialog extends DialogFragment{
    //private static final String TAG = InformationDialog.class.getName();

    public static final String ARG_KEY_TITLE = "title";
    public static final String ARG_KEY_UPLOAD_TIME = "upload_time";
    public static final String ARG_KEY_LOCATION = "location";
    public static final String ARG_KEY_DESCRIPTION = "description";
    public static final String ARG_KEY_ATTACHMENT_LIST = "attachments";

    private String mTitle = "";
    private Date mUploadTime = new Date();
    private LatLng mLocation = new LatLng(24.795099, 120.994655);
    private String mDescription = "";
    private List<String> mAttachmentIdList = new ArrayList<>();


    public static class Builder {
        private Bundle mArgs;

        public Builder(){
            mArgs = new Bundle();
        }

        public Builder setTitle(String title){
            mArgs.putString(ARG_KEY_TITLE, title);
            return this;
        }
        public Builder setLocation(double latitude, double longitude){
            mArgs.putParcelable(ARG_KEY_LOCATION, new LatLng(latitude, longitude));
            return this;
        }
        public Builder setUploadTime(Date uploadTime){
            mArgs.putSerializable(ARG_KEY_UPLOAD_TIME, uploadTime);
            return this;
        }
        public Builder setDescription(String description){
            mArgs.putString(ARG_KEY_DESCRIPTION, description);
            return this;
        }
        public Builder setAttachmentIds(ArrayList<String> idList){
            mArgs.putStringArrayList(ARG_KEY_ATTACHMENT_LIST, idList);
            return this;
        }

        public InformationDialog build(){
            InformationDialog dialog = new InformationDialog();
            dialog.setArguments(mArgs);

            return dialog;
        }
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        Bundle args = getArguments();
        if(args != null){
            mTitle = args.getString(ARG_KEY_TITLE, "");
            mLocation = args.getParcelable(ARG_KEY_LOCATION);
            if(mLocation == null) mLocation = new LatLng(24.795099, 120.994655); //NTHU
            mUploadTime = (Date)args.getSerializable(ARG_KEY_UPLOAD_TIME);
            if(mUploadTime == null) mUploadTime = new Date();
            mDescription = args.getString(ARG_KEY_DESCRIPTION, "");

            mAttachmentIdList = args.getStringArrayList(ARG_KEY_ATTACHMENT_LIST);
            if(mAttachmentIdList == null) mAttachmentIdList = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View window = inflater.inflate(R.layout.layout_information_dialog, container, false);

        TextView title = (TextView)window.findViewById(R.id.text_title);
        TextView location = (TextView)window.findViewById(R.id.text_location);
        TextView description = (TextView)window.findViewById(R.id.text_description);
        TextView uploadTime = (TextView)window.findViewById(R.id.text_upload_time);

        Button butAttachment = (Button)window.findViewById(R.id.but_attachments);

        title.setText(mTitle);
        double latitude = mLocation.latitude;
        double longitude = mLocation.longitude;
        location.setText(getString(R.string.lat_lng_format,
                    Math.abs(latitude), (latitude > 0)? 'N' : 'S',
                    Math.abs(longitude), (longitude > 0)? 'E' : 'W'));
        uploadTime.setText((new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())).format(mUploadTime));
        description.setText(mDescription);
        butAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttachmentsDialog attachmentsDialog = new AttachmentsDialog();
                attachmentsDialog.setAttachmentIdList(mAttachmentIdList);
                attachmentsDialog.show(getFragmentManager(), "attachment");
            }
        });

        return window;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


}
