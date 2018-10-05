package website.todds.saundio.tracks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import website.todds.saundio.R;
import website.todds.saundio.util.BroadAction;
import website.todds.saundio.util.LoaderIds;
import website.todds.toddlibs.andrutils.BroadcastUtil;
import website.todds.toddlibs.andrutils.PermissionUtil;
import website.todds.toddlibs.recyclerfragment.CursorRecyclerAdapter;
import website.todds.toddlibs.recyclerfragment.RecyclerFragment;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.provider.MediaStore.Audio.Media.ALBUM;
import static android.provider.MediaStore.Audio.Media.ALBUM_ID;
import static android.provider.MediaStore.Audio.Media.ARTIST;
import static android.provider.MediaStore.Audio.Media.ARTIST_ID;
import static android.provider.MediaStore.Audio.Media.DATE_ADDED;
import static android.provider.MediaStore.Audio.Media.DURATION;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.Audio.Media.TITLE;
import static android.provider.MediaStore.Audio.Media.TRACK;
import static android.provider.MediaStore.Audio.Media._ID;
import static website.todds.saundio.util.PermissionIds.READ_EXTERNAL_REQ_CODE;

public class TracksListFragment extends RecyclerFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Snackbar mSnackbar;

    // Listens for requests to update the tracks list
    private BroadcastReceiver mReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        // Create the adapter for the RecyclerView now. We'll pass a null for the cursor as it will
        // be handled by the CursorLoader's callbacks later
        setAdapter(new CursorRecyclerAdapter<TrackViewHolder>(getActivity(), null) {

            @Override
            public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new TrackViewHolder(LayoutInflater.from(getActivity())
                        .inflate(R.layout.viewholder_track, parent, false));
            }

            @Override
            public void onBindViewHolder(TrackViewHolder trackViewHolder, Cursor cursor) {
                trackViewHolder.setData(cursor);
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                fetchTracks();
            }
        };
        BroadcastUtil.regLocal(getActivity(), mReceiver, BroadAction.REFRESH_TRACKS_LIST);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Request permission if we don't have it
        if (PermissionUtil.isPermitted(getActivity(), READ_EXTERNAL_STORAGE))
            fetchTracks();
        else
            makePermissionSnackbar(getView()).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BroadcastUtil.unregLocal(getActivity(), mReceiver);
    }

    /*

        Permissions

     */


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted!
                fetchTracks();
            } else
                // Denied! Show the snackbar.
                makePermissionSnackbar(getView()).show();
        }
    }

    private Snackbar makePermissionSnackbar(View view) {
        /*  Todd 2018-10-04

            Originally I was going to use dialog popups to inform the user of permissions but
            they're annoying and easily forgotten about once they're gone. Snackbars are clean,
            look nice and can accomplish the same without frustrating the user.
         */
        mSnackbar = Snackbar.make(
                view,
                getActivity().getString(R.string.permission_required_to_view_library),
                Snackbar.LENGTH_INDEFINITE
        );

        mSnackbar.setAction(R.string.enable, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtil.requestFragmentPermissions(
                        TracksListFragment.this,
                        READ_EXTERNAL_REQ_CODE,
                        READ_EXTERNAL_STORAGE
                );
                mSnackbar.dismiss();
                mSnackbar = null;
            }
        });
        return mSnackbar;
    }

    /*

        LoaderManager.LoaderCallbacks<Cursor>

     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LoaderIds.TRACKS_LIST_FRAGMENT_LOADER) {
            final String[] PROJECTION = {
                    _ID,
                    TITLE,
                    ARTIST,
                    ARTIST_ID,
                    ALBUM,
                    ALBUM_ID,
                    DURATION,
                    DATE_ADDED
            };
            TracksListPrefsHelper prefs = new TracksListPrefsHelper(getActivity());

            return new CursorLoader(
                    getActivity(),
                    EXTERNAL_CONTENT_URI,
                    PROJECTION,
                    // Collect sorting preferences from shared preferences
                    prefs.getSelection(null),
                    prefs.getSelectionArgs(null),
                    prefs.getSortOrder(TRACK + " asc")
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null)
            return;

        if (data.moveToFirst()) {
            swapCursor(data);
        } else {
            // TODO: 2018-10-04 Inform the user we couldn't get data in a persistent, delicate way
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swapCursor(null);
    }

    // Convenience method
    private void swapCursor(Cursor data) {
        CursorRecyclerAdapter adapter = (CursorRecyclerAdapter) getAdapter();
        adapter.swapCursor(data);
        adapter.notifyDataSetChanged();
    }

    private void fetchTracks() {
        // We're ignoring the use of Bundles for the loader since we're using SharedPreferences
        // instead -- we want the user's sorting options to be persistent.
        getLoaderManager().initLoader(LoaderIds.TRACKS_LIST_FRAGMENT_LOADER, null, this);
    }
}
