package website.todds.saundio.components;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;

public class SaundioMediaCallback extends MediaSessionCompat.Callback implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {

    public static final float SPEED_ADDITIVE = 2.0f;

    private Context mContext;
    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mPlaybackState;

    private MediaPlayer mPlayer;

    public SaundioMediaCallback(Context context, MediaSessionCompat mediaSession, PlaybackStateCompat.Builder playbackState) {
        mContext = context;
        mMediaSession = mediaSession;
        mPlaybackState = playbackState;
        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        Log.d("saundio", String.format("Media ID: %s", mediaId));

        setPlaying(false);
        mPlayer.reset();

        try {
            mPlayer.setDataSource(mContext, Uri.parse("file://" + mediaId));
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayFromSearch(String query, Bundle extras) {
    }

    @Override
    public void onPlayFromUri(Uri uri, Bundle extras) {
    }

    @Override
    public void onPlay() {
        setPlaying(true);
        mPlayer.start();
    }

    @Override
    public void onPause() {
        setPlaying(false);
        mPlayer.pause();
    }

    private void setPlaying(boolean isPlaying) {
        mMediaSession.setActive(isPlaying);
        PlaybackStateCompat playbackState = mMediaSession.getController().getPlaybackState();

        long play;
        long pause;
        int state;
        float playbackSpeed;

        if (isPlaying) {
            play = ~ACTION_PLAY;
            pause = ACTION_PAUSE;
            state = STATE_PLAYING;
            playbackSpeed = 1.0f;
//            mPlayer.start();
        } else {
            play = ACTION_PLAY;
            pause = ~ACTION_PAUSE;
            state = STATE_PAUSED;
            playbackSpeed = 0.0f;
//            mPlayer.pause();
        }

        mPlaybackState.setActions(playbackState.getActions() & play & pause);
        mPlaybackState.setState(state, PLAYBACK_POSITION_UNKNOWN, playbackSpeed);
        mMediaSession.setPlaybackState(mPlaybackState.build());
    }

    @Override
    public void onSkipToNext() {
    }

    @Override
    public void onSkipToPrevious() {
    }

    @Override
    public void onFastForward() {
        PlaybackParams params = mPlayer.getPlaybackParams();
        params.setSpeed(params.getSpeed() + SPEED_ADDITIVE);
        mPlayer.setPlaybackParams(params);
    }

    @Override
    public void onRewind() {
        PlaybackParams params = mPlayer.getPlaybackParams();
        params.setSpeed(params.getSpeed() - SPEED_ADDITIVE);
        mPlayer.setPlaybackParams(params);
    }

    @Override
    public void onSeekTo(long pos) {
    }

    /*

        MediaPlayer Callbacks

     */

    @Override
    public void onPrepared(MediaPlayer mp) {
        onPlay();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onSkipToNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d("saundio", String.format("Error!: what: %d, extra: %d", what, extra));
        return false;
    }
}
