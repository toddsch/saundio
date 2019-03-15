package website.todds.saundio.views

import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaControllerCompat.Callback
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.subwranglers.pucker.Pucker
import website.todds.saundio.R
import website.todds.saundio.util.LayoutUtil

class PlaybackTray : RelativeLayout {

    val playPause by LayoutUtil.bind<Pucker>(this, R.id.playpause_pucker)

    private var controller: MediaControllerCompat? = null
    private var controls: Callback? = null

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup()
    }

    private fun setup() {
        LayoutInflater.from(context).inflate(R.layout.component_playback_tray, this)
//        playPause = findViewById(R.id.playpause_pucker)

        controls = object : Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                handlePlayPauseState(state)
            }
        }
    }

    fun setMediaControllerCompat(controller: MediaControllerCompat) {
        this.controller = controller
        controller.registerCallback(controls!!)
        handlePlayPauseState(controller.playbackState)
    }

    fun onStop() {
        controller!!.unregisterCallback(controls!!)
    }

    /*

        State Callback Methods

     */

    fun handlePlayPauseState(s: PlaybackStateCompat?) {
        playPause.puck.background = context.getDrawable(

                // Show pause button if playing audio, otherwise show play button
                if (s!!.playbackSpeed > 0) R.drawable.ic_pause_button else R.drawable.ic_play_button
        )
    }
}
