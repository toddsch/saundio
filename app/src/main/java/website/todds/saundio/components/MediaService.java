package website.todds.saundio.components;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import java.util.List;

import website.todds.saundio.R;

import static android.support.v4.media.session.PlaybackStateCompat.*;

public class MediaService extends MediaBrowserServiceCompat {

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private SaundioMediaCallback mCallbacks;

    private static final long POSSIBLE_STATE_FLAGS =
            ACTION_PLAY |
            ACTION_PAUSE |
            ACTION_PLAY_PAUSE |
            ACTION_PLAY_FROM_MEDIA_ID |
            ACTION_PLAY_FROM_URI |
            ACTION_PLAY_FROM_SEARCH |
            ACTION_FAST_FORWARD |
            ACTION_REWIND |
            ACTION_SKIP_TO_NEXT |
            ACTION_SKIP_TO_PREVIOUS |
            ACTION_SEEK_TO;

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaSession = new MediaSessionCompat(getApplicationContext(), MediaService.class.getSimpleName());
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mStateBuilder = new PlaybackStateCompat.Builder().setActions(POSSIBLE_STATE_FLAGS);
        mMediaSession.setPlaybackState(mStateBuilder.build());

        mCallbacks = new SaundioMediaCallback(this, mMediaSession, mStateBuilder);
        mMediaSession.setCallback(mCallbacks);

        setSessionToken(mMediaSession.getSessionToken());
    }

    @Override
    public void onDestroy() {
        if (mMediaSession != null) {
            mMediaSession.release();
            mMediaSession = null;
        }
        mStateBuilder = null;
        mCallbacks = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (TextUtils.equals(clientPackageName, getPackageName()))
            return new BrowserRoot(getString(R.string.app_name), null);

        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }
}
