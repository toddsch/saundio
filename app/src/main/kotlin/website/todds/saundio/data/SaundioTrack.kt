package website.todds.saundio.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore

import website.todds.toddlibs.andrutils.CursorUtil

class SaundioTrack : Parcelable {

    val id: Long
    val album: String
    val albumId: Long
    val artist: String
    val artistId: Long
    val dateAdded: String
    val duration: Long
    val title: String
    val track: Long
    val data: String

    constructor(cursor: Cursor) {
        id = CursorUtil.getLong(cursor, MediaStore.Audio.Media._ID)
        album = CursorUtil.getStr(cursor, MediaStore.Audio.Media.ALBUM)
        albumId = CursorUtil.getLong(cursor, MediaStore.Audio.Media.ALBUM_ID)
        artist = CursorUtil.getStr(cursor, MediaStore.Audio.Media.ARTIST)
        artistId = CursorUtil.getLong(cursor, MediaStore.Audio.Media.ARTIST_ID)
        dateAdded = CursorUtil.getStr(cursor, MediaStore.Audio.Media.DATE_ADDED)
        duration = CursorUtil.getLong(cursor, MediaStore.Audio.Media.DURATION)
        title = CursorUtil.getStr(cursor, MediaStore.Audio.Media.TITLE)
        track = CursorUtil.getLong(cursor, MediaStore.Audio.Media.TRACK)
        data = CursorUtil.getStr(cursor, MediaStore.Audio.Media.DATA)
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeString(this.album)
        dest.writeLong(this.albumId)
        dest.writeString(this.artist)
        dest.writeLong(this.artistId)
        dest.writeString(this.dateAdded)
        dest.writeLong(this.duration)
        dest.writeString(this.title)
        dest.writeLong(this.track)
        dest.writeString(this.data)
    }

    protected constructor(`in`: Parcel) {
        this.id = `in`.readLong()
        this.album = `in`.readString()
        this.albumId = `in`.readLong()
        this.artist = `in`.readString()
        this.artistId = `in`.readLong()
        this.dateAdded = `in`.readString()
        this.duration = `in`.readLong()
        this.title = `in`.readString()
        this.track = `in`.readLong()
        this.data = `in`.readString()
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<SaundioTrack> = object : Parcelable.Creator<SaundioTrack> {
            override fun createFromParcel(source: Parcel): SaundioTrack {
                return SaundioTrack(source)
            }

            override fun newArray(size: Int): Array<SaundioTrack?> {
                return arrayOfNulls(size)
            }
        }
    }
}
