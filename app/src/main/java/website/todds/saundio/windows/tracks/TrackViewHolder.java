package website.todds.saundio.tracks;


import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import website.todds.saundio.R;
import website.todds.saundio.util.DurationUtil;
import website.todds.toddlibs.andrutils.CursorUtil;

import static android.provider.MediaStore.Audio.AudioColumns.ARTIST;
import static android.provider.MediaStore.Audio.AudioColumns.DURATION;
import static android.provider.MediaStore.MediaColumns.TITLE;

public class TrackViewHolder extends RecyclerView.ViewHolder {

    private TextView mTitle;
    private TextView mArtist;
    private TextView mDuration;

    public TrackViewHolder(View itemView) {
        super(itemView);

        mTitle = itemView.findViewById(R.id.vh_track_title);
        mArtist = itemView.findViewById(R.id.vh_track_artist);
        mDuration = itemView.findViewById(R.id.vh_track_duration);
    }

    public void setData(Cursor cursor) {
        mTitle.setText(CursorUtil.getStr(cursor, TITLE));
        mArtist.setText(CursorUtil.getStr(cursor, ARTIST));
        mDuration.setText(DurationUtil.stampFromMillis(CursorUtil.getLong(cursor, DURATION)));
    }
}
