package website.todds.saundio;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import website.todds.saundio.tracks.TracksListFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TracksListFragment tracks = (TracksListFragment)
                Fragment.instantiate(this, TracksListFragment.class.getName());

        tracks.setLayoutManager(this, true, false);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, tracks)
                .commit();
    }
}
