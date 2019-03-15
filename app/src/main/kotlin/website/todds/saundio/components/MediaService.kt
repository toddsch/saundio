package website.todds.saundio.components

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils

import website.todds.saundio.R

import android.support.v4.media.session.PlaybackStateCompat.*

class MediaService : MediaBrowserServiceCompat() {

    private lateinit var mMediaSession: MediaSessionCompat
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder
    private lateinit var mCallbacks: SaundioMediaCallback

    override fun onCreate() {
        super.onCreate()

        mMediaSession = MediaSessionCompat(applicationContext, MediaService::class.java.simpleName)
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        mStateBuilder = PlaybackStateCompat.Builder().setActions(POSSIBLE_STATE_FLAGS)
        mMediaSession.setPlaybackState(mStateBuilder.build())

        mCallbacks = SaundioMediaCallback(this, mMediaSession, mStateBuilder)
        mMediaSession.setCallback(mCallbacks)

        sessionToken = mMediaSession.sessionToken
    }

    override fun onDestroy() {
        mMediaSession.release()
        super.onDestroy()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        return if (TextUtils.equals(clientPackageName, packageName)) MediaBrowserServiceCompat.BrowserRoot(getString(R.string.app_name), null) else null

    }

    override fun onLoadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(null)
    }

    companion object {

        private const val POSSIBLE_STATE_FLAGS = ACTION_PLAY or
                ACTION_PAUSE or
                ACTION_PLAY_PAUSE or
                ACTION_PLAY_FROM_MEDIA_ID or
                ACTION_PLAY_FROM_URI or
                ACTION_PLAY_FROM_SEARCH or
                ACTION_FAST_FORWARD or
                ACTION_REWIND or
                ACTION_SKIP_TO_NEXT or
                ACTION_SKIP_TO_PREVIOUS or
                ACTION_SEEK_TO
    }
}
