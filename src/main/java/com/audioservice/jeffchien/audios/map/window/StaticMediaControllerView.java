package com.audioservice.jeffchien.audios.map.window;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.audioservice.jeffchien.audios.map.R;

public class StaticMediaControllerView extends RelativeLayout {
    //private static final String TAG = StaticMediaControllerView.class.getName();

    private Context mContext;

    private MediaController.MediaPlayerControl mCtrlInterface;

    private static final int PROGRESS_CYCLE_DURATION_MS = 500; //0.5s

    private enum PlayerState{
        RESET,
        PLAYING,
        PAUSING,
        SEEKING
    }
    private PlayerState mState = PlayerState.RESET;

    private SeekBar mProgressBar;
    private ImageButton mButPlayPause;
    private TextView mTextProgress;

    public StaticMediaControllerView(Context context) {
        super(context);
        init(context);
    }

    public StaticMediaControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StaticMediaControllerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    private void init(Context context){
        mContext = context;
        initView(context);
        mState = PlayerState.RESET;
    }

    private void initView(Context context){
        inflate(context, R.layout.layout_static_media_controller, this);

        mButPlayPause = (ImageButton)findViewById(R.id.player_but_play_pause);
        mProgressBar = (SeekBar)findViewById(R.id.player_seekbar);
        mTextProgress = (TextView)findViewById(R.id.player_progress_text);

        mButPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCtrlInterface != null) {
                    switch (mState) {
                        case PLAYING: {
                            mCtrlInterface.pause();
                            mState = PlayerState.PAUSING;
                            break;
                        }

                        case PAUSING: {
                            mCtrlInterface.start();
                            mState = PlayerState.PLAYING;
                            break;
                        }

                        case RESET: {
                            mCtrlInterface.start();
                        }

                        default: {
                            //Reserved
                        }
                    }
                }
            }
        });

        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mState = PlayerState.SEEKING;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mCtrlInterface != null) {
                    mCtrlInterface.seekTo(seekBar.getProgress());

                    if(mCtrlInterface.isPlaying()){
                        mState = PlayerState.PLAYING;
                    }else{
                        mState = PlayerState.PAUSING;
                    }
                }
            }
        });
    }

    private final Runnable mPlayerStateLooper = new Runnable() {
        @Override
        public void run() {

            if(mCtrlInterface != null){
                boolean reachEnd = false;

                //Progress
                if(mState != PlayerState.SEEKING){
                    int total = mCtrlInterface.getDuration();
                    int progress = mCtrlInterface.getCurrentPosition();

                    mProgressBar.setMax(total);
                    mProgressBar.setProgress(progress);

                    if(progress >= total && progress != 0){
                        //Reach the end
                        mState = PlayerState.RESET;
                        reachEnd = true;
                    }
                }

                //TODO: Show standard time
                mTextProgress.setText(mContext.getString(R.string.media_progress_pattern,
                            mProgressBar.getProgress() / 1000, mProgressBar.getMax() / 1000));

                switch (mState){
                    case PLAYING:{
                        if(!mProgressBar.isEnabled()) mProgressBar.setEnabled(true);

                        mButPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                        break;
                    }

                    case PAUSING:{
                        mButPlayPause.setImageResource(android.R.drawable.ic_media_play);
                        break;
                    }

                    case SEEKING:{
                        //Nothing
                        break;
                    }

                    case RESET:{
                        if(reachEnd){
                            mCtrlInterface.pause();
                            mCtrlInterface.seekTo(0);

                            mProgressBar.setProgress(0);
                            mProgressBar.setMax(0);
                            mProgressBar.setEnabled(false);
                            mButPlayPause.setImageResource(android.R.drawable.ic_media_play);
                        }else{
                            if(mCtrlInterface.isPlaying()){
                                mState = PlayerState.PLAYING;
                            }
                        }
                    }
                }
            }

            postDelayed(this, PROGRESS_CYCLE_DURATION_MS);
        }
    };

    @Override
    protected void onDetachedFromWindow(){
        removeCallbacks(mPlayerStateLooper);
        super.onDetachedFromWindow();
    }

    public void setMediaController(MediaController.MediaPlayerControl controller){
        if(controller == null) return;

        mCtrlInterface = controller;

        mState = PlayerState.RESET;

        removeCallbacks(mPlayerStateLooper);
        post(mPlayerStateLooper);
    }
}
