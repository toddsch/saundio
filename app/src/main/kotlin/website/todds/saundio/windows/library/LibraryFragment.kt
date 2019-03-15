package website.todds.saundio.windows.library

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.MediaColumns.DATA
import android.support.design.widget.Snackbar
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import website.todds.saundio.R
import website.todds.saundio.data.SaundioTrack
import website.todds.saundio.util.BroadAction
import website.todds.saundio.util.LoaderIds
import website.todds.saundio.util.PermissionIds.READ_EXTERNAL_REQ_CODE
import website.todds.toddlibs.andrutils.BroadcastUtil
import website.todds.toddlibs.andrutils.PermissionUtil
import website.todds.toddlibs.recyclerfragment.CursorRecyclerAdapter
import website.todds.toddlibs.recyclerfragment.RecyclerFragment

class LibraryFragment : RecyclerFragment(), LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private lateinit var mSnackbar: Snackbar

    // Listens for requests to update the tracks list
    private var mTracksReceiver: BroadcastReceiver? = null

    private var mMediaController: MediaControllerCompat? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)

        // Create the adapter for the RecyclerView now. We'll pass a null for the cursor as it will
        // be handled by the CursorLoader's callbacks later
        setAdapter(object : CursorRecyclerAdapter<LibraryViewHolder>(activity, null) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
                return LibraryViewHolder(LayoutInflater.from(activity)
                        .inflate(R.layout.viewholder_track, parent, false))
            }

            override fun onBindViewHolder(trackViewHolder: LibraryViewHolder, cursor: Cursor) {
                trackViewHolder.setData(cursor)
                trackViewHolder.setOnClickListener(this@LibraryFragment)
            }
        })

        mSnackbar = Snackbar.make(
                activity.findViewById(R.id.main_frame),
                activity.getString(R.string.permission_required_to_view_library),
                Snackbar.LENGTH_INDEFINITE
        )

        mSnackbar.setAction(R.string.enable) {
            PermissionUtil.requestFragmentPermissions(
                    this@LibraryFragment,
                    READ_EXTERNAL_REQ_CODE,
                    READ_EXTERNAL_STORAGE
            )
            mSnackbar.dismiss()
        }

        mTracksReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                fetchTracks()
            }
        }
        BroadcastUtil.regLocal(activity, mTracksReceiver, BroadAction.REFRESH_TRACKS_LIST)

        return root
    }

    override fun onStart() {
        super.onStart()

        // Request permission if we don't have it
        if (PermissionUtil.isPermitted(activity, READ_EXTERNAL_STORAGE))
            fetchTracks()
        else
            mSnackbar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        BroadcastUtil.unregLocal(activity, mTracksReceiver)
    }

    fun setMediaController(mediaController: MediaControllerCompat) {
        this.mMediaController = mediaController
    }

    /*

        Permissions

     */


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == READ_EXTERNAL_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted!
                fetchTracks()
            } else
                // Denied! Show the snackbar.
                mSnackbar.show()
        }
    }

    /*

        LoaderManager.LoaderCallbacks<Cursor>

     */

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor>? {
        if (id == LoaderIds.TRACKS_LIST_FRAGMENT_LOADER) {
            val projection = arrayOf(BaseColumns._ID, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.AudioColumns.ALBUM_ID, MediaStore.Audio.AudioColumns.ARTIST, MediaStore.Audio.AudioColumns.ARTIST_ID, MediaStore.MediaColumns.DATE_ADDED, MediaStore.Audio.AudioColumns.DURATION, MediaStore.MediaColumns.TITLE, MediaStore.Audio.AudioColumns.TRACK, DATA)
            val prefs = LibraryPrefs(activity)

            return CursorLoader(
                    activity,
                    EXTERNAL_CONTENT_URI,
                    projection,
                    // Collect sorting preferences from shared preferences
                    prefs.getSelection(""),
                    prefs.getSelectionArgs(arrayOf()),
                    prefs.orderColumns
            )
        }
        return null
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (data == null)
            return

        if (data.moveToFirst()) {
            swapCursor(data)
        } else {
            // TODO: 2018-10-04 Inform the user we couldn't get data in a persistent, delicate way
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        swapCursor(null)
    }

    // Convenience method
    private fun swapCursor(data: Cursor?) {
        val adapter = adapter as CursorRecyclerAdapter<*>
        adapter.swapCursor(data)
        adapter.notifyDataSetChanged()
    }

    private fun fetchTracks() {
        val manager = loaderManager

        // Destroy the current loader if it exists
        if (manager.getLoader<Any>(LoaderIds.TRACKS_LIST_FRAGMENT_LOADER) != null)
            manager.destroyLoader(LoaderIds.TRACKS_LIST_FRAGMENT_LOADER)

        // We're ignoring the use of Bundles for the loader since we're using SharedPreferences
        // instead -- we want the user's sorting options to be persistent.
        manager.initLoader(LoaderIds.TRACKS_LIST_FRAGMENT_LOADER, Bundle(), this)
    }

    /*

        Other Callbacks

     */

    override fun onClick(v: View) {
        val path = (v.tag as SaundioTrack).data
        mMediaController!!.transportControls.playFromMediaId(path, null)
    }
}
