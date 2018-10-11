package website.todds.saundio.windows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import website.todds.saundio.R;
import website.todds.saundio.util.BroadAction;
import website.todds.saundio.windows.orderby.OrderByView;
import website.todds.saundio.windows.search.SearchFragment;
import website.todds.saundio.windows.tracks.TracksListFragment;
import website.todds.toddlibs.andrutils.BroadcastUtil;
import website.todds.toddlibs.andrutils.DialogUtil;
import website.todds.toddlibs.andrutils.StrUtil;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private Toolbar mToolbar;
    private ConstraintLayout mLayout;
    private ViewPager mPager;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar();

        mLayout = findViewById(R.id.main_layout);

        TracksListFragment tracks = (TracksListFragment)
                Fragment.instantiate(this, TracksListFragment.class.getName());

        tracks.setLayoutManager(this, true, false);

        mPager = findViewById(R.id.main_pager);
        mPager.setAdapter(new MainPagerAdapter(this, getSupportFragmentManager()));

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleBroadcast(intent);
            }
        };
        BroadcastUtil.regLocal(this, mReceiver, SearchFragment.BROADCAST_ACTION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mToolbar.setOnMenuItemClickListener(null);
        BroadcastUtil.unregLocal(this, mReceiver);
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

    private void setupToolbar() {
        mToolbar = findViewById(R.id.main_activity_toolbar);
        mToolbar.setOnMenuItemClickListener(this);

        mToolbar.inflateMenu(R.menu.main_activity_toolbar_menu);
        mToolbar.setTitle(R.string.app_name);
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
}
