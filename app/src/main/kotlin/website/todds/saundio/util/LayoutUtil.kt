package website.todds.saundio.util

import android.support.annotation.IdRes
import android.view.View

object LayoutUtil {
    fun <T : View> bind(view: View, @IdRes res: Int): Lazy<T> {
        return lazy(LazyThreadSafetyMode.NONE) { view.findViewById<T>(res) }
    }
}