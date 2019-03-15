package website.todds.saundio.windows

import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.os.RemoteException
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.ConnectionCallback
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.subwranglers.pucker.Pucker
import website.todds.saundio.R
import website.todds.saundio.components.MediaService
import website.todds.saundio.util.BroadAction
import website.todds.saundio.views.PlaybackTray
import website.todds.saundio.windows.library.LibraryFragment
import website.todds.saundio.windows.orderby.OrderByView
import website.todds.saundio.windows.search.SearchFragment
import website.todds.toddlibs.andrutils.BroadcastUtil
import website.todds.toddlibs.andrutils.DeviceUtil
import website.todds.toddlibs.andrutils.DialogUtil

class MainActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener, Pucker.PuckListener {

    private var mToolbar: Toolbar? = null
    private var mLayout: ConstraintLayout? = null
    private var mPlaybackTray: PlaybackTray? = null

    private var mConnectionCallback: ConnectionCallback? = null
    private var mMediaBrowser: MediaBrowserCompat? = null

    private var mController: MediaControllerCompat? = null
    private var mTransportControls: MediaControllerCompat.TransportControls? = null

    private var mReceiver: BroadcastReceiver? = null

    private var libFrag: LibraryFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupMedia()
        setupViews()

        libFrag = Fragment
                .instantiate(this, LibraryFragment::class.java.name) as LibraryFragment

        libFrag!!.setLayoutManager(this, true, false)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, libFrag)
                .commit()

        BroadcastUtil.regLocal(this, mReceiver, SearchFragment.BROADCAST_ACTION)
    }

    private fun setupMedia() {
        mConnectionCallback = object : ConnectionCallback() {
            override fun onConnected() {
                val token = mMediaBrowser!!.sessionToken

                try {
                    mController = MediaControllerCompat(this@MainActivity, token)
                    MediaControllerCompat.setMediaController(this@MainActivity, mController)

                    // Give playback tray a reference to the media controller
                    mPlaybackTray!!.setMediaControllerCompat(MediaControllerCompat
                            .getMediaController(this@MainActivity))

                    mTransportControls = mController!!.transportControls

                    libFrag!!.setMediaController(mController!!)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }
        }

        mMediaBrowser = MediaBrowserCompat(
                this,
                ComponentName(this, MediaService::class.java),
                mConnectionCallback!!, null)


        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleBroadcast(intent)
            }
        }
    }

    private fun setupViews() {
        mToolbar = findViewById(R.id.main_activity_toolbar)
        mToolbar!!.setOnMenuItemClickListener(this)

        mToolbar!!.inflateMenu(R.menu.main_activity_toolbar_menu)
        mToolbar!!.setTitle(R.string.app_name)

        mLayout = findViewById(R.id.main_layout)

        mPlaybackTray = findViewById(R.id.playback_tray)
        mPlaybackTray!!
                .playPause.setPuckListener(this)
                .setTranslator(Pucker.FN_X_AXIS_ONLY).maxRadius = DeviceUtil.dpToPx(this, PUCK_RADIUS)
    }

    override fun onStart() {
        super.onStart()
        mMediaBrowser!!.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        mPlaybackTray!!.onStop()
        mMediaBrowser!!.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        mToolbar!!.setOnMenuItemClickListener(null)
        BroadcastUtil.unregLocal(this, mReceiver)
        mConnectionCallback = null
    }

    private fun handleBroadcast(intent: Intent) {
        val action = intent.action ?: // No action given -- ignore broadcast
        return

        if (action == SearchFragment.BROADCAST_ACTION) {
            val seq = if (intent.getBooleanExtra(SearchFragment.KEY_STARTING, false))
                ""
            else
                getString(R.string.app_name)
            mToolbar!!.title = seq
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                val fragment = SearchFragment()
                fragment.show(supportFragmentManager, SearchFragment::class.java.name)
            }
            R.id.action_sort_rules -> {
                val sortView = OrderByView(this)

                val okClick = DialogInterface.OnClickListener { dialog, which ->
                    sortView.save()
                    BroadcastUtil.broadcast(this@MainActivity,
                            BroadAction.REFRESH_TRACKS_LIST)
                }

                DialogUtil.viewDialog(this, sortView, okClick, null).show()
            }
        }
        return true
    }

    override fun onBackPressed() {
        val manager = supportFragmentManager
        if (manager.backStackEntryCount > 0)
            manager.popBackStack()
        else
            super.onBackPressed()
    }

    override fun onPuckClicked(pucker: Pucker) {
        if (mController!!.playbackState.state == PlaybackStateCompat.STATE_PLAYING)
            mTransportControls!!.pause()
        else
            mTransportControls!!.play()
    }

    override fun onPuckMoved(pucker: Pucker, distanceFromOrigin: FloatArray) {
        // Use boundary crossing logic to activate FF/R's
    }

    override fun onPuckReleased(pucker: Pucker, distanceFromOrigin: FloatArray) {
        // Resume normal playback speed
    }

    companion object {

        const val PUCK_RADIUS = 100 // in DP
    }
}
