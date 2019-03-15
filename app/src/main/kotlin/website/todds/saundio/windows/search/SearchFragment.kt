package website.todds.saundio.windows.search


import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window

import website.todds.saundio.R
import website.todds.toddlibs.andrutils.BroadcastUtil

/**
 *
 * A fragment used for searching the user's music library. Using
 * [Local broadcasts][android.support.v4.content.LocalBroadcastManager] this fragment will
 * broadcast if it's entered [.onStart] or [.onStop] under the action
 * [.BROADCAST_ACTION].
 *
 *
 *
 * <tt>true</tt> will be set in the intent under [.KEY_STARTING] if
 * in onStart(), <tt>false</tt> if in onStop().
 */
class SearchFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_search, container)
    }

    override fun onStart() {
        super.onStart()
        val window = dialog.window ?: return

        val resources = resources
        val transparentBg: Drawable

        transparentBg = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
            resources.getDrawable(R.drawable.background_translucent, null)
        else
            resources.getDrawable(R.drawable.background_translucent)

        window.setBackgroundDrawable(transparentBg)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra(KEY_STARTING, true)
        BroadcastUtil.broadcast(context, intent)
    }

    override fun onStop() {
        super.onStop()
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra(KEY_STARTING, false)
        BroadcastUtil.broadcast(context, intent)
    }

    companion object {

        const val BROADCAST_ACTION = "website.todds.saundio.windows.search.SearchFragment"
        const val KEY_STARTING = "keyStarting"
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