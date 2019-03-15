package website.todds.saundio.views;

import android.content.Context;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaControllerCompat.Callback;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.subwranglers.pucker.Pucker;

import website.todds.saundio.R;

public class PlaybackTray extends RelativeLayout {

    public Pucker playPause;
    private MediaControllerCompat controller;
    private Callback controls;

    public PlaybackTray(Context context) {
        super(context);
        setup();
    }

    public PlaybackTray(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public PlaybackTray(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        LayoutInflater.from(getContext()).inflate(R.layout.component_playback_tray, this);
        playPause = findViewById(R.id.playpause_pucker);

        controls = new Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                handlePlayPauseState(state);
            }
        };
    }

    public void setMediaControllerCompat(MediaControllerCompat controller) {
        this.controller = controller;
        controller.registerCallback(controls);
        handlePlayPauseState(controller.getPlaybackState());
    }

    public void onStop() {
        controller.unregisterCallback(controls);
    }

    /*

        State Callback Methods

     */

    public void handlePlayPauseState(PlaybackStateCompat s) {
        playPause.getPuck().setBackground(getContext().getDrawable(

                // Show pause button if playing audio, otherwise show play button
                s.getPlaybackSpeed() > 0 ? R.drawable.ic_pause_button : R.drawable.ic_play_button
        ));
    }
}
