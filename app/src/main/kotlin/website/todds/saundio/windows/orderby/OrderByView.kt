package website.todds.saundio.windows.orderby

import android.content.Context
import android.provider.MediaStore.Audio.AudioColumns.*
import android.provider.MediaStore.MediaColumns.DATE_ADDED
import android.provider.MediaStore.MediaColumns.TITLE
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import website.todds.dragtag.DragTag
import website.todds.dragtag.DragTagListener
import website.todds.saundio.R
import website.todds.saundio.windows.library.LibraryPrefs
import website.todds.toddlibs.andrutils.ArrayUtil
import website.todds.toddlibs.andrutils.StrUtil

class OrderByView : RelativeLayout, DragTag.OnPrimaryClickListener, DragTag.OnSecondaryClickListener, DragTag.OnDropListener {

    /**
     * The names of the columns users can sort the tracks list by, in alphabetical order.
     */
    private val SORT_TERMS = arrayOf(ALBUM, ARTIST, DATE_ADDED, DURATION, TITLE, TRACK, YEAR)

    internal lateinit var mAvailableTags: FlexboxLayout
    internal lateinit var mSelectedTags: LinearLayout
    internal lateinit var mSelectedText: TextView

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.viewgroup_order_by, this)

        mAvailableTags = findViewById(R.id.viewgroup_order_by_available_tags)
        mSelectedTags = findViewById(R.id.viewgroup_order_by_selected_tags)
        mSelectedText = findViewById(R.id.viewgroup_order_by_selected_tags_textview)

        mAvailableTags.setOnDragListener(DragTagListener())
        mSelectedTags.setOnDragListener(DragTagListener())

        val prefs = LibraryPrefs(context)

        // Keep track of which terms weren't selected by the user
        val remainingTerms = ArrayUtil.toList(SORT_TERMS)

        // Display tags the user has already selected
        for (column in StrUtil.explode(prefs.orderColumns, ",")) {
            if (!StrUtil.strchk(column))
            // Skip empty strings
                continue

            val tag = buildTag(column)

            // Using moveTo() so onDropListener() will be called
            tag.moveTo(mSelectedTags)

            // We've added this tag to selected tags -- remove it from the remaining terms to
            // distribute.
            remainingTerms.remove((tag.data as OrderData).column)
        }

        // Show the rest of the tags the user hasn't selected
        for (term in remainingTerms)
        // Using moveTo() so onDropListener() will be called
            buildTag(term).moveTo(mAvailableTags)
    }

    private fun buildTag(columnName: String): DragTag {
        val tag = LayoutInflater.from(context)
                .inflate(R.layout.viewgroup_dragtag_order_by, null) as DragTag

        // Get and show sort direction
        val data = OrderData(columnName)
        tag.data = data
        OrderData.updateSortDirView(tag, data.orderDirection)

        // Format column name for output (replace underscores with spaces)
        tag.setText(data.column.replace("_".toRegex(), " "))

        tag.setOnPrimaryClick(this)
        tag.setOnSecondaryClick(this)
        tag.setOnDropListener(this)

        return tag
    }

    /**
     * Saves the current custom sorting rules to [LibraryPrefs].
     */
    fun save() {
        mSelectedTags.clearFocus()

        val orderBy = StringBuilder()

        for (i in 0 until mSelectedTags.childCount) {
            val view = mSelectedTags.getChildAt(i)
            if (view is DragTag) {

                // Append column name and sort direction string
                val data = view.data as OrderData
                orderBy
                        .append(data.column)
                        .append(data.orderStr)
                        .append(",")
            }
        }

        val prefs = LibraryPrefs(context)
        prefs.orderColumns = StrUtil.removeLastChar(orderBy.toString())
    }

    /*

        DragTag Listeners

     */

    override fun onPrimaryClick(dragTag: DragTag, imageButton: ImageButton) {
        // Increment the stored sort direction
        OrderData.updateSortDirView(dragTag, OrderData.getIncSortDir(dragTag))
    }

    override fun onSecondaryClick(dragTag: DragTag, imageButton: ImageButton) {
        dragTag.moveTo(mAvailableTags)
    }

    override fun onMove(dragTag: DragTag, prev: ViewGroup, next: ViewGroup, hasMoved: Boolean) {
        if (hasMoved)
        // Show a short explanatory quip if there's no selected tags
            mSelectedText.visibility = if (mSelectedTags.childCount < 1) View.VISIBLE else View.GONE
        else {
            // Hide action buttons if we're showing tags in mAvailableTags -- otherwise, show buttons
            val visibility = if (next.id == R.id.viewgroup_order_by_available_tags)
                View.GONE
            else
                View.VISIBLE
            dragTag.setButtonsVisibility(visibility, visibility)
        }
    }

    private class OrderData (columnName: String) {

        /**
         * @return [String] containing the name of the column
         */
        val column: String
        var orderDirection: Int = 0

        /**
         * Returns the string representation of order direction: "asc", "desc" or an empty string if
         * neither direction has been set.
         * @return An empty string if no direction set, or "asc"/"desc" with a leading space char.
         */
        val orderStr: String
            get() = getStringFromDir(orderDirection)

        init {
            var columnName = columnName
            // Get order direction
            this.orderDirection = getDirFromString(columnName)

            // Remove order direction qualifiers, if they exist
            columnName = columnName.trim { it <= ' ' }.replace("( asc$)|( desc$)".toRegex(), "")
            this.column = columnName
        }

        companion object {

            private const val TAG_SORT_NO_DIR = 0
            private const val TAG_SORT_ASC = 1
            private const val TAG_SORT_DESC = 2

            private fun getDirFromString(column: String): Int {
                var column = column
                column = column.toLowerCase()
                return when {
                    column.endsWith(" desc") -> TAG_SORT_DESC
                    column.endsWith(" asc") -> TAG_SORT_ASC
                    else -> TAG_SORT_NO_DIR
                }
            }

            private fun getStringFromDir(dir: Int): String {
                return if (dir == TAG_SORT_DESC)
                    " desc"
                else if (dir == TAG_SORT_ASC)
                    " asc"
                else
// TAG_SORT_NO_DIR
                    ""
            }

            /**
             * Gets the sort increment order integer from a given [DragTag] and increments it.
             *
             * @param tag the [DragTag] to get sort direction from
             * @return [Integer] from one of the values: [.TAG_SORT_NO_DIR],
             * [.TAG_SORT_ASC], [.TAG_SORT_DESC]
             */
            fun getIncSortDir(tag: DragTag): Int {
                val data = tag.data as OrderData
                return (data.orderDirection + 1) % 3
            }

            fun updateSortDirView(tag: DragTag, newSortDir: Int) {
                val data = tag.data as OrderData
                data.orderDirection = newSortDir

                tag.setButtonsDrawables(getDrawableFromDir(newSortDir), -1)
            }

            private fun getDrawableFromDir(dir: Int): Int {
                return when (dir) {
                    TAG_SORT_DESC -> R.drawable.ic_sort_desc
                    TAG_SORT_ASC -> R.drawable.ic_sort_asc
                    else -> R.drawable.ic_sort_neutral // TODO: Todd 2018-10-08 remove this
                }
            }
        }
    }
}
