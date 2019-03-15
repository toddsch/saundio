package website.todds.saundio.components

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log

import java.io.IOException

import android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY
import android.support.v4.media.session.PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING

class SaundioMediaCallback(private val mContext: Context, private val mMediaSession: MediaSessionCompat, private val mPlaybackState: PlaybackStateCompat.Builder) : MediaSessionCompat.Callback(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private val mPlayer: MediaPlayer = MediaPlayer()

    init {
        mPlayer.setOnPreparedListener(this)
        mPlayer.setOnCompletionListener(this)
        mPlayer.setOnErrorListener(this)
    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        Log.d("saundio", String.format("Media ID: %s", mediaId))

        setPlaying(false)
        mPlayer.reset()

        try {
            mPlayer.setDataSource(mContext, Uri.parse("file://" + mediaId!!))
            mPlayer.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {}

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {}

    override fun onPlay() {
        setPlaying(true)
        mPlayer.start()
    }

    override fun onPause() {
        setPlaying(false)
        mPlayer.pause()
    }

    private fun setPlaying(isPlaying: Boolean) {
        mMediaSession.isActive = isPlaying
        val playbackState = mMediaSession.controller.playbackState

        val play: Long
        val pause: Long
        val state: Int
        val playbackSpeed: Float

        if (isPlaying) {
            play = ACTION_PLAY.inv()
            pause = ACTION_PAUSE
            state = STATE_PLAYING
            playbackSpeed = 1.0f
            //            mPlayer.start();
        } else {
            play = ACTION_PLAY
            pause = ACTION_PAUSE.inv()
            state = STATE_PAUSED
            playbackSpeed = 0.0f
            //            mPlayer.pause();
        }

        mPlaybackState.setActions(playbackState.actions and play and pause)
        mPlaybackState.setState(state, PLAYBACK_POSITION_UNKNOWN, playbackSpeed)
        mMediaSession.setPlaybackState(mPlaybackState.build())
    }

    override fun onSkipToNext() {}

    override fun onSkipToPrevious() {}

    override fun onFastForward() {
        val params = mPlayer.playbackParams
        params.speed = params.speed + SPEED_ADDITIVE
        mPlayer.playbackParams = params
    }

    override fun onRewind() {
        val params = mPlayer.playbackParams
        params.speed = params.speed - SPEED_ADDITIVE
        mPlayer.playbackParams = params
    }

    override fun onSeekTo(pos: Long) {}

    /*

        MediaPlayer Callbacks

     */

    override fun onPrepared(mp: MediaPlayer) {
        onPlay()
    }

    override fun onCompletion(mp: MediaPlayer) {
        onSkipToNext()
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.d("saundio", String.format("Error!: what: %d, extra: %d", what, extra))
        return false
    }

    companion object {

        const val SPEED_ADDITIVE = 2.0f
    }
}
