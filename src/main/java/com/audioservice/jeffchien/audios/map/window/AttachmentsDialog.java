package com.audioservice.jeffchien.audios.map.window;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.audioservice.jeffchien.audios.map.IAudioSProxy;
import com.audioservice.jeffchien.audios.map.R;
import com.audioservice.jeffchien.audios.rest.Constants;
import com.audioservice.jeffchien.audios.rest.layouts.ResourceObject;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class AttachmentsDialog extends DialogFragment{
    private static final String TAG = AttachmentsDialog.class.getName();

    private List<String> mAttachmentIdList = new ArrayList<>();

    private final AttachmentAdapter mAttachmentAdapter = new AttachmentAdapter();

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
    }

    public void setAttachmentIdList(List<String> list){ mAttachmentIdList = list; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View window = inflater.inflate(R.layout.layout_attachment_dialog, container, false);

        RecyclerView attachmentList = (RecyclerView)window.findViewById(R.id.list_attachment);
        attachmentList.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        attachmentList.setAdapter(mAttachmentAdapter);
        attachmentList.setHasFixedSize(true);
        IAudioSProxy.AudioSInterface.getObjectMetaBatch(mAttachmentIdList).enqueue(mAttachmentCallback);

        return window;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private final retrofit.Callback<List<ResourceObject>> mAttachmentCallback = new Callback<List<ResourceObject>>() {
        @Override
        public void onResponse(Response<List<ResourceObject>> response, Retrofit retrofit) {
            if(response.code() == 200 && response.body() != null){
                //Log.d(TAG, "Attachments size: " + response.body().size());
                mAttachmentAdapter.setAttachments(response.body());
                mAttachmentAdapter.notifyDataSetChanged();
            }else{
                Log.e(TAG, "Get resource object failed");
            }
        }

        @Override
        public void onFailure(Throwable t) {
            t.printStackTrace();
        }
    };

    private class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder>{

        //protected static final String OBJECT_REQUEST_PATTERN = Constants.BASE_URL + Constants.BASE_API_PATH + "/object/%1$s/!/%2$s";

        public final class ViewHolder extends RecyclerView.ViewHolder {

            public View mContainer;
            public TextView mTextTitle;
            public ImageView mImagePreview;

            public ViewHolder(View containerView) {
                super(containerView);

                mContainer = containerView;
                mTextTitle = (TextView)containerView.findViewById(R.id.text_title);
                mImagePreview = (ImageView)containerView.findViewById(R.id.image_preview);
            }
        }

        private final MimeTypeMap mMimeMap = MimeTypeMap.getSingleton();

        private int mViewWidth = 0;

        private List<ResourceObject> mAttachmentList = new ArrayList<>();
        public void setAttachments(List<ResourceObject> list){ mAttachmentList = list; }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_attachment_list_item, parent, false);

            mViewWidth = parent.getWidth();

            return new AttachmentAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final ResourceObject obj = mAttachmentList.get(position);

            holder.mTextTitle.setText(obj.name);

            Uri.Builder uriBuilder = Constants.getBaseUriBuilder();
            final Uri requestUri = uriBuilder.appendPath("object")
                                .appendPath(obj.id)
                                .appendEncodedPath("!")
                                .appendPath(obj.name)
                                .appendQueryParameter("width", "" + mViewWidth)
                                .build();

            String fileExt = obj.name.substring(obj.name.lastIndexOf('.') + 1);
            String fileMime = mMimeMap.getMimeTypeFromExtension(fileExt);
            if(fileMime.startsWith("image")){
                holder.mImagePreview.setVisibility(View.VISIBLE);
                holder.mTextTitle.setBackgroundResource(R.color.colorBlackTrans50);
                holder.mContainer.setBackgroundResource(android.R.color.transparent);
                holder.mTextTitle.setTextColor(Color.WHITE);

                Glide.with(AttachmentsDialog.this)
                        .load(requestUri.toString())
                        .placeholder(R.drawable.loading)
                        .crossFade()
                        .into(holder.mImagePreview);
            }else{
                holder.mImagePreview.setVisibility(View.INVISIBLE);
                holder.mContainer.setBackgroundResource(R.drawable.menu_dropdown_panel_holo_light);
                holder.mTextTitle.setBackgroundResource(android.R.color.transparent);
                holder.mTextTitle.setTextColor(Color.BLACK);
            }


            //OnClick Listeners
            if(fileMime.startsWith("audio")){
                holder.mContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putString(AudioPlayer.AUDIO_TITLE_KEY, obj.name);
                        args.putParcelable(AudioPlayer.RESOURCE_URI_KEY, requestUri);

                        AudioPlayer player = new AudioPlayer();
                        player.setArguments(args);
                        player.show(getFragmentManager(), "audio");
                    }
                });
            }else{
                holder.mContainer.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() { return mAttachmentList.size(); }
    }
}
