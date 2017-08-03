package hu.ait.macweekly;

/**
 * Created by Mack on 7/28/2017.
 */

public class MacWeeklyUtils {

    public static boolean isTextEmpty(String text) {
        if (text == null || text.equals("")) {
            return true;
        }
        return false;
    }
}
