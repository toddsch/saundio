package website.todds.saundio.windows.library;


import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import website.todds.saundio.R;
import website.todds.saundio.data.SaundioTrack;
import website.todds.saundio.util.DurationUtil;
import website.todds.toddlibs.andrutils.CursorUtil;

import static android.provider.MediaStore.Audio.AudioColumns.DURATION;

public class LibraryViewHolder extends RecyclerView.ViewHolder {

    private View mLayout;
    private TextView mTitle;
    private TextView mArtist;
    private TextView mDuration;

    private SaundioTrack mTrackInfo;

    public LibraryViewHolder(View itemView) {
        super(itemView);

        mLayout = itemView;
        mTitle = itemView.findViewById(R.id.vh_track_title);
        mArtist = itemView.findViewById(R.id.vh_track_artist);
        mDuration = itemView.findViewById(R.id.vh_track_duration);
    }

    public void setData(Cursor cursor) {
        mTrackInfo = new SaundioTrack(cursor);
        mLayout.setTag(mTrackInfo);

        mTitle.setText(mTrackInfo.getTitle());
        mArtist.setText(mTrackInfo.getArtist());
        mDuration.setText(DurationUtil.stampFromMillis(CursorUtil.getLong(cursor, DURATION)));
    }

    public void setOnClickListener(View.OnClickListener clickListener) {
        mLayout.setOnClickListener(clickListener);
    }
}
