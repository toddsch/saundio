package website.todds.saundio.windows.library;

import android.content.Context;
import android.support.v4.util.ArraySet;

import java.util.Arrays;
import java.util.Set;

import website.todds.toddlibs.andrutils.PrefsHelper;

public class LibraryPrefs extends PrefsHelper {

    private static final String KEY_SELECTION = "SELECTION";
    private static final String KEY_SELECTION_ARGS = "SELECTION_ARGS";
    private static final String KEY_ORDER_BY = "SORT_ORDER";

    public LibraryPrefs(Context context) {
        this(context, false);
    }

    public LibraryPrefs(Context context, boolean async) {
        super(context, LibraryPrefs.class.getName(), async);
    }

    /**
     * Gets the SQL selection string from preferences.
     * @param defaultVal a value to return if the requested one does not exist in preferences
     * @return {@link String}
     */
    public String getSelection(String defaultVal) {
        return getString(KEY_SELECTION, defaultVal);
    }

    public void setSelection(String value) {
        set(KEY_SELECTION, value);
    }

    /**
     * Gets the collections of arguments to be used with the string from
     * {@link #getSelection(String)}.
     * @param defaultVal a value to return if the requested one does not exist in preferences
     * @return {@link String[]}
     */
    public String[] getSelectionArgs(String[] defaultVal) {
        Set<String> strings = getStringSet(KEY_SELECTION_ARGS, new ArraySet<String>());
        return strings.size() == 0 ? defaultVal : strings.toArray(new String[]{});
    }

    public void setSelectionArgs(String[] value) {
        Set<String> strings = new ArraySet<>();
        strings.addAll(Arrays.asList(value));
        set(KEY_SELECTION_ARGS, strings);
    }

    /**
     * Gets a comma-separated list of the columns that are used by SQL's ORDER BY clause.
     * @return {@link String} containing the columns to sort by. Returns an empty string by default.
     */
    public String getOrderColumns() {
        return getString(KEY_ORDER_BY, "");
    }

    public void setOrderColumns(String orderByClause) {
        set(KEY_ORDER_BY, orderByClause);
    }
}
