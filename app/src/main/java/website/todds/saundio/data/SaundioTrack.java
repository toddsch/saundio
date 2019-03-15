package website.todds.saundio.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import website.todds.toddlibs.andrutils.CursorUtil;

public class SaundioTrack implements Parcelable {

    private final long _ID;
    private final String ALBUM;
    private final long ALBUM_ID;
    private final String ARTIST;
    private final long ARTIST_ID;
    private final String DATE_ADDED;
    private final long DURATION;
    private final String TITLE;
    private final long TRACK;
    private final String DATA;

    public SaundioTrack(Cursor cursor) {
        _ID = CursorUtil.getLong(cursor, MediaStore.Audio.Media._ID);
        ALBUM = CursorUtil.getStr(cursor, MediaStore.Audio.Media.ALBUM);
        ALBUM_ID = CursorUtil.getLong(cursor, MediaStore.Audio.Media.ALBUM_ID);
        ARTIST = CursorUtil.getStr(cursor, MediaStore.Audio.Media.ARTIST);
        ARTIST_ID = CursorUtil.getLong(cursor, MediaStore.Audio.Media.ARTIST_ID);
        DATE_ADDED = CursorUtil.getStr(cursor, MediaStore.Audio.Media.DATE_ADDED);
        DURATION = CursorUtil.getLong(cursor, MediaStore.Audio.Media.DURATION);
        TITLE = CursorUtil.getStr(cursor, MediaStore.Audio.Media.TITLE);
        TRACK = CursorUtil.getLong(cursor, MediaStore.Audio.Media.TRACK);
        DATA = CursorUtil.getStr(cursor, MediaStore.Audio.Media.DATA);
    }

    public long getId() {
        return _ID;
    }

    public String getAlbum() {
        return ALBUM;
    }

    public long getAlbumId() {
        return ALBUM_ID;
    }

    public String getArtist() {
        return ARTIST;
    }

    public long getArtistId() {
        return ARTIST_ID;
    }

    public String getDateAdded() {
        return DATE_ADDED;
    }

    public long getDuration() {
        return DURATION;
    }

    public String getTitle() {
        return TITLE;
    }

    public long getTrack() {
        return TRACK;
    }

    public String getData() {
        return DATA;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._ID);
        dest.writeString(this.ALBUM);
        dest.writeLong(this.ALBUM_ID);
        dest.writeString(this.ARTIST);
        dest.writeLong(this.ARTIST_ID);
        dest.writeString(this.DATE_ADDED);
        dest.writeLong(this.DURATION);
        dest.writeString(this.TITLE);
        dest.writeLong(this.TRACK);
        dest.writeString(this.DATA);
    }

    protected SaundioTrack(Parcel in) {
        this._ID = in.readLong();
        this.ALBUM = in.readString();
        this.ALBUM_ID = in.readLong();
        this.ARTIST = in.readString();
        this.ARTIST_ID = in.readLong();
        this.DATE_ADDED = in.readString();
        this.DURATION = in.readLong();
        this.TITLE = in.readString();
        this.TRACK = in.readLong();
        this.DATA = in.readString();
    }

    public static final Parcelable.Creator<SaundioTrack> CREATOR = new Parcelable.Creator<SaundioTrack>() {
        @Override
        public SaundioTrack createFromParcel(Parcel source) {
            return new SaundioTrack(source);
        }

        @Override
        public SaundioTrack[] newArray(int size) {
            return new SaundioTrack[size];
        }
    };
}
