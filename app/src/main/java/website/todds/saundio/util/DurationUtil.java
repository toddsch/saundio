package website.todds.saundio.util;

public class DurationUtil {

    private static final long MINUTE = 60L;
    private static final long HOUR = MINUTE * 60L;

    public static String stampFromMillis(long milliseconds) {
        return stampFromSeconds(milliseconds / 1000L);
    }

    public static String stampFromSeconds(long seconds) {
        String out = "";

        if (seconds >= HOUR) {
            long hours = seconds / HOUR;
            seconds %= HOUR;
            out += hours + ":";
        }

        long minutes = seconds / MINUTE;
        // Todd 2018-10-04 out.length() > 0 checks to see if the hours notation is present. This
        // avoids results like "05:22"
        out += (minutes < 10L && out.length() > 0 ? "0" : "") + minutes + ":";

        seconds %= MINUTE;
        out += (seconds < 10L ? "0" : "") + seconds; // prepend zero if seconds is 1 digit

        return out;
    }
}
