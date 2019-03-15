package website.todds.saundio.windows;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.ConnectionCallback;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.subwranglers.pucker.Pucker;

import website.todds.saundio.R;
import website.todds.saundio.components.MediaService;
import website.todds.saundio.util.BroadAction;
import website.todds.saundio.views.PlaybackTray;
import website.todds.saundio.windows.library.LibraryFragment;
import website.todds.saundio.windows.orderby.OrderByView;
import website.todds.saundio.windows.search.SearchFragment;
import website.todds.toddlibs.andrutils.BroadcastUtil;
import website.todds.toddlibs.andrutils.DeviceUtil;
import website.todds.toddlibs.andrutils.DialogUtil;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, Pucker.PuckListener {

    public static final int PUCK_RADIUS = 100; // in DP

    private Toolbar mToolbar;
    private ConstraintLayout mLayout;
    private PlaybackTray mPlaybackTray;

    private ConnectionCallback mConnectionCallback;
    private MediaBrowserCompat mMediaBrowser;

    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;

    private BroadcastReceiver mReceiver;

    private LibraryFragment libFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupMedia();
        setupViews();

        libFrag = (LibraryFragment) Fragment
                .instantiate(this, LibraryFragment.class.getName());

        libFrag.setLayoutManager(this, true, false);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, libFrag)
                .commit();

        BroadcastUtil.regLocal(this, mReceiver, SearchFragment.BROADCAST_ACTION);
    }

    private void setupMedia() {
        mConnectionCallback = new ConnectionCallback() {
            @Override
            public void onConnected() {
                MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                try {
                    mController = new MediaControllerCompat(MainActivity.this, token);
                    MediaControllerCompat.setMediaController(MainActivity.this, mController);

                    // Give playback tray a reference to the media controller
                    mPlaybackTray.setMediaControllerCompat(MediaControllerCompat
                            .getMediaController(MainActivity.this));

                    mTransportControls = mController.getTransportControls();

                    libFrag.setMediaController(mController);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };

        mMediaBrowser = new MediaBrowserCompat(
                this,
                new ComponentName(this, MediaService.class),
                mConnectionCallback,
                null);


        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleBroadcast(intent);
            }
        };
    }

    private void setupViews() {
        mToolbar = findViewById(R.id.main_activity_toolbar);
        mToolbar.setOnMenuItemClickListener(this);

        mToolbar.inflateMenu(R.menu.main_activity_toolbar_menu);
        mToolbar.setTitle(R.string.app_name);

        mLayout = findViewById(R.id.main_layout);

        mPlaybackTray = findViewById(R.id.playback_tray);
        mPlaybackTray
                .playPause.setPuckListener(this)
                .setTranslator(Pucker.FN_X_AXIS_ONLY)
                .setMaxRadius(DeviceUtil.dpToPx(this, PUCK_RADIUS));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlaybackTray.onStop();
        mMediaBrowser.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mToolbar.setOnMenuItemClickListener(null);
        BroadcastUtil.unregLocal(this, mReceiver);
        mConnectionCallback = null;
    }

    private void handleBroadcast(Intent intent) {
        String action = intent.getAction();
        if (action == null)
            // No action given -- ignore broadcast
            return;

        if (action.equals(SearchFragment.BROADCAST_ACTION)) {
            CharSequence seq = intent.getBooleanExtra(SearchFragment.KEY_STARTING, false) ?
                    "" : getString(R.string.app_name);
            mToolbar.setTitle(seq);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search: {
                SearchFragment fragment = new SearchFragment();
                fragment.show(getSupportFragmentManager(), SearchFragment.class.getName());
                break;
            }
            case R.id.action_sort_rules: {
                final OrderByView sortView = new OrderByView(this);

                DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sortView.save();
                        BroadcastUtil.broadcast(MainActivity.this,
                                BroadAction.REFRESH_TRACKS_LIST);
                    }
                };

                DialogUtil.viewDialog(this, sortView, okClick, null).show();
                break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0)
            manager.popBackStack();
        else
            super.onBackPressed();
    }

    @Override
    public void onPuckClicked(Pucker pucker) {
        if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING)
            mTransportControls.pause();
        else
            mTransportControls.play();
    }

    @Override
    public void onPuckMoved(Pucker pucker, float[] distanceFromOrigin) {
        // Use boundary crossing logic to activate FF/R's
    }

    @Override
    public void onPuckReleased(Pucker pucker, float[] distanceFromOrigin) {
        // Resume normal playback speed
    }
}
