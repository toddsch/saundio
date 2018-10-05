package website.todds.saundio.tracks;

import android.content.Context;
import android.support.v4.util.ArraySet;

import java.util.Arrays;
import java.util.Set;

import website.todds.toddlibs.andrutils.PrefsHelper;

public class TracksListPrefsHelper extends PrefsHelper {

    private static final String KEY_SELECTION = "SELECTION";
    private static final String KEY_SELECTION_ARGS = "SELECTION_ARGS";
    private static final String KEY_SORT_ORDER = "SORT_ORDER";
    private static final String KEY_PERMISSION_RATIONALE_SHOWN = "PERMISSION_RATIONALE_SHOWN";

    public TracksListPrefsHelper(Context context) {
        super(context, TracksListPrefsHelper.class.getName());
    }

    public String getSelection(String defaultVal) {
        return getString(KEY_SELECTION, defaultVal);
    }

    public void setSelection(String value, boolean async) {
        set(KEY_SELECTION, value, async);
    }

    public String[] getSelectionArgs(String[] defaultVal) {
        Set<String> strings = getStringSet(KEY_SELECTION_ARGS, new ArraySet<String>());
        return strings.size() == 0 ? defaultVal : strings.toArray(new String[]{});
    }

    public void setSelectionArgs(String[] value, boolean async) {
        Set<String> strings = new ArraySet<>();
        strings.addAll(Arrays.asList(value));
        set(KEY_SELECTION_ARGS, strings, async);
    }

    public String getSortOrder(String defaultVal) {
        return getString(KEY_SORT_ORDER, defaultVal);
    }

    public void setSortOrder(String columnName, boolean asc, boolean async) {
        set(KEY_SORT_ORDER, columnName + (asc ? " asc" : " desc"), async);
    }

    public boolean getPermissionRationaleShown(boolean defaultVal) {
        return getBoolean(KEY_PERMISSION_RATIONALE_SHOWN, defaultVal);
    }

    public void setPermissionRationaleShown(boolean value, boolean async) {
        set(KEY_PERMISSION_RATIONALE_SHOWN, value, async);
    }
}
