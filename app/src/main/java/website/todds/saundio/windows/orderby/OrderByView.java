package website.todds.saundio.tracks;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import website.todds.dragtag.DragTag;
import website.todds.dragtag.DragTagListener;
import website.todds.saundio.R;
import website.todds.toddlibs.andrutils.ArrayUtil;
import website.todds.toddlibs.andrutils.DeviceUtil;
import website.todds.toddlibs.andrutils.StrUtil;

import static android.provider.MediaStore.Audio.AudioColumns.ALBUM;
import static android.provider.MediaStore.Audio.AudioColumns.ARTIST;
import static android.provider.MediaStore.Audio.AudioColumns.DURATION;
import static android.provider.MediaStore.Audio.AudioColumns.TRACK;
import static android.provider.MediaStore.Audio.AudioColumns.YEAR;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.TITLE;

public class OrderByView extends RelativeLayout implements DragTag.OnPrimaryClickListener, DragTag.OnSecondaryClickListener, DragTag.OnDropListener {

    /**
     * The names of the columns users can sort the tracks list by, in alphabetical order.
     */
    private final String[] SORT_TERMS = {ALBUM, ARTIST, DATE_ADDED, DURATION, TITLE, TRACK, YEAR};

    FlexboxLayout mAvailableTags;
    LinearLayout mSelectedTags;
    TextView mSelectedText;

    public OrderByView(Context context) {
        super(context);
        init();
    }

    public OrderByView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.viewgroup_order_by, this);

        mAvailableTags = findViewById(R.id.viewgroup_order_by_available_tags);
        mSelectedTags = findViewById(R.id.viewgroup_order_by_selected_tags);
        mSelectedText = findViewById(R.id.viewgroup_order_by_selected_tags_textview);

        mAvailableTags.setOnDragListener(new DragTagListener());
        mSelectedTags.setOnDragListener(new DragTagListener());

        TracksListPrefs prefs = new TracksListPrefs(getContext());

        // Keep track of which terms weren't selected by the user
        final List<String> REMAINING_TERMS = ArrayUtil.toList(SORT_TERMS);

        // Display tags the user has already selected
        for (String column : StrUtil.explode(prefs.getOrderColumns(), ",")) {
            if (!StrUtil.strchk(column))
                // Skip empty strings
                continue;

            DragTag tag = buildTag(column);

            // Using moveTo() so onDropListener() will be called
            tag.moveTo(mSelectedTags);

            // We've added this tag to selected tags -- remove it from the remaining terms to
            // distribute.
            REMAINING_TERMS.remove(((OrderData) tag.getData()).columnName);
        }

        // Show the rest of the tags the user hasn't selected
        for (String term : REMAINING_TERMS)
            // Using moveTo() so onDropListener() will be called
            buildTag(term).moveTo(mAvailableTags);
    }

    private DragTag buildTag(String columnName) {
        DragTag tag = (DragTag) LayoutInflater.from(getContext())
                .inflate(R.layout.viewgroup_dragtag_order_by, null);

        // Get and show sort direction
        OrderData data = new OrderData(columnName);
        tag.setData(data);
        OrderData.updateSortDirView(tag, data.orderDirection);

        // Format column name for output (replace underscores with spaces)
        tag.setText(data.getColumn().replaceAll("_", " "));

        tag.setOnPrimaryClick(this);
        tag.setOnSecondaryClick(this);
        tag.setOnDropListener(this);

        return tag;
    }

    /**
     * Saves the current custom sorting rules to {@link TracksListPrefs}.
     */
    public void save() {
        mSelectedTags.clearFocus();

        StringBuilder orderBy = new StringBuilder();

        for (int i = 0; i < mSelectedTags.getChildCount(); i++) {
            View view = mSelectedTags.getChildAt(i);
            if (view instanceof DragTag) {
                DragTag tag = (DragTag) view;

                // Append column name and sort direction string
                OrderData data = (OrderData) tag.getData();
                orderBy
                    .append(data.getColumn())
                    .append(data.getOrderStr())
                    .append(",");
            }
        }

        TracksListPrefs prefs = new TracksListPrefs(getContext());
        prefs.setOrderColumns(StrUtil.removeLastChar(orderBy.toString()));
    }

    /*

        DragTag Listeners

     */

    @Override
    public void onPrimaryClick(DragTag dragTag, ImageButton imageButton) {
        // Increment the stored sort direction
        OrderData.updateSortDirView(dragTag, OrderData.getIncSortDir(dragTag));
    }

    @Override
    public void onSecondaryClick(DragTag dragTag, ImageButton imageButton) {
        dragTag.moveTo(mAvailableTags);
    }

    @Override
    public void onMove(DragTag dragTag, ViewGroup prev, ViewGroup next, boolean hasMoved) {
        if (hasMoved)
            // Show a short explanatory quip if there's no selected tags
            mSelectedText.setVisibility(mSelectedTags.getChildCount() < 1 ? View.VISIBLE : View.GONE);
        else {
            // Hide action buttons if we're showing tags in mAvailableTags -- otherwise, show buttons
            int visibility = next.getId() == R.id.viewgroup_order_by_available_tags ?
                    View.GONE : View.VISIBLE;
            dragTag.setButtonsVisibility(visibility, visibility);
        }
    }

    private static class OrderData {

        private static final int TAG_SORT_NO_DIR = 0;
        private static final int TAG_SORT_ASC = 1;
        private static final int TAG_SORT_DESC = 2;

        private String columnName;
        private int orderDirection;

        private OrderData(String columnName) {
            // Get order direction
            this.orderDirection = getDirFromString(columnName);

            // Remove order direction qualifiers, if they exist
            columnName = columnName.trim().replaceAll("( asc$)|( desc$)", "");
            this.columnName = columnName;
        }

        private static int getDirFromString(String column) {
            column = column.toLowerCase();
            if (column.endsWith(" desc"))
                return TAG_SORT_DESC;
            else if (column.endsWith(" asc"))
                return TAG_SORT_ASC;
            else
                return TAG_SORT_NO_DIR;
        }

        private static String getStringFromDir(int dir) {
            if (dir == TAG_SORT_DESC)
                return " desc";
            else if (dir == TAG_SORT_ASC)
                return " asc";
            else // TAG_SORT_NO_DIR
                return "";
        }

        /**
         * Gets the sort increment order integer from a given {@link DragTag} and increments it.
         *
         * @param tag the {@link DragTag} to get sort direction from
         * @return {@link Integer} from one of the values: {@link #TAG_SORT_NO_DIR},
         * {@link #TAG_SORT_ASC}, {@link #TAG_SORT_DESC}
         */
        private static int getIncSortDir(DragTag tag) {
            OrderData data = (OrderData) tag.getData();
            return (data.orderDirection + 1) % 3;
        }

        private static void updateSortDirView(DragTag tag, int newSortDir) {
            OrderData data = (OrderData) tag.getData();
            data.orderDirection = newSortDir;

            tag.setButtonsDrawables(getDrawableFromDir(newSortDir), -1);
        }

        private static int getDrawableFromDir(int dir) {
            if (dir == TAG_SORT_DESC)
                return R.drawable.ic_sort_desc;
            else if (dir == TAG_SORT_ASC)
                return R.drawable.ic_sort_asc;
            else
                return R.drawable.ic_sort_neutral; // TODO: Todd 2018-10-08 remove this
        }

        /**
         * @return {@link String} containing the name of the column
         */
        public String getColumn() {
            return columnName;
        }

        /**
         * Returns the string representation of order direction: "asc", "desc" or an empty string if
         * neither direction has been set.
         * @return An empty string if no direction set, or "asc"/"desc" with a leading space char.
         */
        public String getOrderStr() {
            return getStringFromDir(orderDirection);
        }
    }
}
