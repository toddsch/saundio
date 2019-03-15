package website.todds.saundio.windows.library


import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import website.todds.saundio.R
import website.todds.saundio.data.SaundioTrack
import website.todds.saundio.util.DurationUtil
import website.todds.toddlibs.andrutils.CursorUtil

import android.provider.MediaStore.Audio.AudioColumns.DURATION

class LibraryViewHolder(private val mLayout: View) : RecyclerView.ViewHolder(mLayout) {
    private val mTitle: TextView = mLayout.findViewById(R.id.vh_track_title)
    private val mArtist: TextView = mLayout.findViewById(R.id.vh_track_artist)
    private val mDuration: TextView = mLayout.findViewById(R.id.vh_track_duration)

    private var mTrackInfo: SaundioTrack? = null

    fun setData(cursor: Cursor) {
        mTrackInfo = SaundioTrack(cursor)
        mLayout.tag = mTrackInfo

        mTitle.text = mTrackInfo!!.title
        mArtist.text = mTrackInfo!!.artist
        mDuration.text = DurationUtil.stampFromMillis(CursorUtil.getLong(cursor, DURATION))
    }

    fun setOnClickListener(clickListener: View.OnClickListener) {
        mLayout.setOnClickListener(clickListener)
    }
}
