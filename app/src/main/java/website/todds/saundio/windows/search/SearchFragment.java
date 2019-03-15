package website.todds.saundio.windows.search;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import website.todds.saundio.R;
import website.todds.toddlibs.andrutils.BroadcastUtil;

/**
 * <p>A fragment used for searching the user's music library. Using
 * {@link android.support.v4.content.LocalBroadcastManager Local broadcasts} this fragment will
 * broadcast if it's entered {@link #onStart()} or {@link #onStop()} under the action
 * {@link #BROADCAST_ACTION}.</p>
 * <p>
 * <p><tt>true</tt> will be set in the intent under {@link #KEY_STARTING} if
 * in onStart(), <tt>false</tt> if in onStop().</p>
 */
public class SearchFragment extends DialogFragment {

    public static final String BROADCAST_ACTION = SearchFragment.class.getName();
    public static final String KEY_STARTING = "keyStarting";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window == null)
            return;

        Resources resources = getResources();
        Drawable transparentBg;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
            transparentBg = resources.getDrawable(R.drawable.background_translucent, null);
        else
            transparentBg = resources.getDrawable(R.drawable.background_translucent);

        window.setBackgroundDrawable(transparentBg);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(KEY_STARTING, true);
        BroadcastUtil.broadcast(getContext(), intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(KEY_STARTING, false);
        BroadcastUtil.broadcast(getContext(), intent);
    }
}

/*  Todd 2018-10-10

    I really really REALLY wanted to implement some sort of background blurring for this fragment
    but it was either computationally too expensive or it just didn't look good with only blurring
    text.

    Upon further research I found:

    https://android.jlelse.eu/the-blurry-frosted-background-is-a-common-pattern-on-ios-where-they-have-simply-controls-to-cbd0c5843e5f

    ... which seems to be a decently solid implementation. Maybe it'll still be too expensive to
    blur the entire phone screen? Who knows until I come back to trying our blurs again.

    All that being said, I'm not yet ready to throw out this code:

    ... implements ViewTreeObserver.OnPreDrawListener

    private static final float SCALE_FACTOR = 1.0f;
    private static final float RADIUS = 10.0f;

    private Bitmap mBackground;

    public void getBitmapFromView(View view) {
        view.buildDrawingCache();
        mBackground = Bitmap.createBitmap(view.getDrawingCache());
    }

    onViewCreated() {
        ...
        // Need to attach this to the tree so we can use it once it's been measured
        view.getViewTreeObserver().addOnPreDrawListener(this);
        ...
    }

    private void blur(final Bitmap bmp, final View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int width = (int) (view.getMeasuredWidth() / SCALE_FACTOR);
                int height = (int) (view.getMeasuredHeight() / SCALE_FACTOR);

                final Bitmap overlay = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(overlay);

                canvas.translate(-view.getLeft(), -view.getTop());
                canvas.scale(1 / SCALE_FACTOR, 1 / SCALE_FACTOR);

                Paint paint = new Paint();
                paint.setFlags(Paint.FILTER_BITMAP_FLAG);
                canvas.drawBitmap(bmp, 0, 0, paint);

                RenderScript rs = RenderScript.create(getActivity());
                Allocation overlayAlloc = Allocation.createFromBitmap(rs, overlay);
                ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());

                blur.setInput(overlayAlloc);
                blur.setRadius(RADIUS);
                blur.forEach(overlayAlloc);

                overlayAlloc.copyTo(overlay);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setBackground(new BitmapDrawable(getResources(), overlay));
                    }
                });

                rs.destroy();
            }
        }).start();
    }

    @Override
    public boolean onPreDraw() {
        View view = getView();
        if (view == null)
            return false;

        // Only blur this once
        view.getViewTreeObserver().removeOnPreDrawListener(this);
        blur(mBackground, view);
        return true;
    }
 */