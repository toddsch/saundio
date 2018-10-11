package website.todds.saundio;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import website.todds.saundio.windows.MainPagerAdapter;
import website.todds.saundio.windows.orderby.OrderByView;
import website.todds.saundio.windows.tracks.TracksListFragment;
import website.todds.saundio.util.BroadAction;
import website.todds.toddlibs.andrutils.BroadcastUtil;
import website.todds.toddlibs.andrutils.DialogUtil;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar();

        TracksListFragment tracks = (TracksListFragment)
                Fragment.instantiate(this, TracksListFragment.class.getName());

        tracks.setLayoutManager(this, true, false);

        ViewPager pager = findViewById(R.id.main_pager);
        pager.setAdapter(new MainPagerAdapter(this, getSupportFragmentManager()));
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
            case R.id.action_sort_rules:
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
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mToolbar.setOnMenuItemClickListener(null);
    }
}
