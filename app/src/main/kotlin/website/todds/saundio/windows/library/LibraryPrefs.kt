package website.todds.saundio.windows.library

import android.content.Context
import android.support.v4.util.ArraySet

import java.util.Arrays

import website.todds.toddlibs.andrutils.PrefsHelper

class LibraryPrefs @JvmOverloads constructor(context: Context, async: Boolean = false) : PrefsHelper(context, LibraryPrefs::class.java.name, async) {

    /**
     * Gets a comma-separated list of the columns that are used by SQL's ORDER BY clause.
     * @return [String] containing the columns to sort by. Returns an empty string by default.
     */
    var orderColumns: String
        get() = getString(KEY_ORDER_BY, "")
        set(orderByClause) = set(KEY_ORDER_BY, orderByClause)

    /**
     * Gets the SQL selection string from preferences.
     * @param defaultVal a value to return if the requested one does not exist in preferences
     * @return [String]
     */
    fun getSelection(defaultVal: String): String {
        return getString(KEY_SELECTION, defaultVal)
    }

    fun setSelection(value: String) {
        set(KEY_SELECTION, value)
    }

    /**
     * Gets the collections of arguments to be used with the string from
     * [.getSelection].
     * @param defaultVal a value to return if the requested one does not exist in preferences
     * @return [String[]]
     */
    fun getSelectionArgs(defaultVal: Array<String>): Array<String> {
        val strings = getStringSet(KEY_SELECTION_ARGS, ArraySet())
        return if (strings.size == 0) defaultVal else strings.toTypedArray()
    }

    fun setSelectionArgs(value: Array<String>) {
        val strings = ArraySet<String>()
        strings.addAll(Arrays.asList(*value))
        set(KEY_SELECTION_ARGS, strings)
    }

    companion object {

        private const val KEY_SELECTION = "SELECTION"
        private const val KEY_SELECTION_ARGS = "SELECTION_ARGS"
        private const val KEY_ORDER_BY = "SORT_ORDER"
    }
}
