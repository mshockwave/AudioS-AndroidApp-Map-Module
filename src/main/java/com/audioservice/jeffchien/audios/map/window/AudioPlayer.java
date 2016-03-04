package com.audioservice.jeffchien.audios.map.window;

import android.app.Dialog;
import android.app.DialogFragment;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.audioservice.jeffchien.audios.map.R;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.PlayerControl;

public class AudioPlayer extends DialogFragment{
    private static final String TAG = AudioPlayer.class.getName();

    private Handler mMainHandler;

    private StaticMediaControllerView mMediaController;

    public static final String AUDIO_TITLE_KEY = "audio-title";
    private String mAudioTitleString;

    private ExoPlayer mPlayer;

    public static final String RESOURCE_URI_KEY = "resource-uri";
    private Uri mResourceUri;

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        mMainHandler = new Handler(getActivity().getMainLooper());

        Bundle args = getArguments();
        if(args != null){
            mResourceUri = args.getParcelable(RESOURCE_URI_KEY);
            mAudioTitleString = args.getString(AUDIO_TITLE_KEY, getString(R.string.untitle));
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        initializePlayer();
    }

    @Override
    public void onPause(){
        releasePlayer();

        super.onPause();
    }

    private void initializePlayer(){
        mPlayer = ExoPlayer.Factory.newInstance(1);
        PlayerControl playerControl = new PlayerControl(mPlayer);

        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mMainHandler, null);
        DataSource dataSource = new DefaultUriDataSource(getActivity(), bandwidthMeter, "android"/*TODO: userAgent*/);
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(mResourceUri,
                                                dataSource, allocator,
                                                BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT);
        MediaCodecAudioTrackRenderer audioRenderer
                = new MediaCodecAudioTrackRenderer(sampleSource, null, true, mMainHandler, mAudioTrackEventCallback, AudioCapabilities.getCapabilities(getActivity()));

        mPlayer.prepare(audioRenderer);

        if(mMediaController != null){
            mMediaController.setMediaController(playerControl);
        }

        mPlayer.setPlayWhenReady(true);
    }
    private void releasePlayer(){
        if(mPlayer != null){
            mPlayer.release();
        }
    }

    private final MediaCodecAudioTrackRenderer.EventListener mAudioTrackEventCallback = new MediaCodecAudioTrackRenderer.EventListener() {
        @Override
        public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
            Log.e(TAG, "onAudioTrackInitializationError: ");
            e.printStackTrace();
        }

        @Override
        public void onAudioTrackWriteError(AudioTrack.WriteException e) {

        }

        @Override
        public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

        }

        @Override
        public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
            Log.e(TAG, "onDecoderInitializationError: ");
            e.printStackTrace();
        }

        @Override
        public void onCryptoError(MediaCodec.CryptoException e) {}
        @Override
        public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {
            Log.i(TAG, "Use decoder: " + decoderName);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View window = inflater.inflate(R.layout.layout_audio_player, container, false);

        TextView title = (TextView)window.findViewById(R.id.text_media_title);
        mMediaController = (StaticMediaControllerView)window.findViewById(R.id.media_controller);

        title.setText(mAudioTitleString);

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
