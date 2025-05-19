package icu.grely;

import arc.util.Strings;

public class Utils {
    public static long parseTime(String time) {
        if (time.length() == 0 || !Character.isDigit(time.charAt(0)))
            return -1;
        char timeMod = Character.toLowerCase(time.charAt(time.length() - 1));

        if (Character.isDigit(timeMod)) {
            if (!Strings.canParseInt(time))
                return -1;
            return Long.parseLong(time) * 60;
        }

        time = time.substring(0, time.length() - 1);
        if (!Strings.canParseInt(time))
            return -1;

        long parsed = Long.parseLong(time);
        if (timeMod == 'h')
            return parsed * 60 * 60;
        if (timeMod == 'd')
            return parsed * 60 * 60 * 24;
        if (timeMod == 'w')
            return parsed * 60 * 60 * 24 * 7;
        if (timeMod == 'm')
            return parsed * 60 * 60 * 24 * 30;
        if (timeMod == 'y')
            return parsed * 60 * 60 * 24 * 365;
        return parsed;
    }
}

