package website.todds.saundio.windows;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import website.todds.saundio.windows.nowplaying.NowPlayingFragment;
import website.todds.saundio.windows.tracks.TracksListFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {

    private static final int NUM_PAGES = 2;

    private Activity activity;

    public MainPagerAdapter(Activity activity, FragmentManager fm) {
        super(fm);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;

        if (position == 0) {
            // TracksListFragment

            fragment = Fragment.instantiate(activity, TracksListFragment.class.getName());
            ((TracksListFragment) fragment).setLayoutManager(activity, true, false);
        } else
            // NowPlayingFragment
            fragment = Fragment.instantiate(activity, NowPlayingFragment.class.getName());

        return fragment;
    }
}
