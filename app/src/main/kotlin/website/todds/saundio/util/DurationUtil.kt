package website.todds.saundio.util

object DurationUtil {

    private val MINUTE = 60L
    private val HOUR = MINUTE * 60L

    fun stampFromMillis(milliseconds: Long): String {
        return stampFromSeconds(milliseconds / 1000L)
    }

    fun stampFromSeconds(seconds: Long): String {
        var seconds = seconds
        var out = ""

        if (seconds >= HOUR) {
            val hours = seconds / HOUR
            seconds %= HOUR
            out += "$hours:"
        }

        val minutes = seconds / MINUTE
        // Todd 2018-10-04 out.length() > 0 checks to see if the hours notation is present. This
        // avoids results like "05:22"
        out += (if (minutes < 10L && out.length > 0) "0" else "") + minutes + ":"

        seconds %= MINUTE
        out += (if (seconds < 10L) "0" else "") + seconds // prepend zero if seconds is 1 digit

        return out
    }
}
