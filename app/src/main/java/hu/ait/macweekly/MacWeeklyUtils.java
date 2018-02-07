package hu.ait.macweekly;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.PriorityQueue;

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

    public static String formatDateTimeAgo(String oldDateString) {
        //if date is seconds away from now, show seconds only and ago
        //if date is hours away from now, show hours only and ago
        //if date is days away from now show days only up to day three
        //else show the actual date Mon day, year
//        yyyy-mm-ddThh:mm:ss
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        StringBuilder resDifference = new StringBuilder();
        try {
            Date oldDate = dateFormat.parse(oldDateString);
//            String testString = "2017-12-08T01:11:11"; //Can use to test date parsing
//            Date curDate = dateFormat.parse(testString);
            Date curDate = new Date();
            dateFormat.format(curDate);

            long diff = curDate.getTime() - oldDate.getTime();

            System.out.println();
            long seconds = diff / 1000;
            long min = seconds / 60;
            long hour = min / 60;
            long day = hour / 24;

            if(day > 3) {
                String[] monthOptions = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                String[] dateItems = oldDateString.split("\\-|T|:");
                int monthInteger = Integer.parseInt(dateItems[1]);
                String monthField = monthOptions[monthInteger-1];
                String dayField = dateItems[2];
                String yearField = dateItems[0];
                resDifference = new StringBuilder(formatDateFull(oldDateString));
            } else if(day > 1) {
                resDifference.append(day).append(" days ago");
            }else if(day > 0) {
                resDifference.append(day).append(" day ago");
            } else if(hour > 1) {
                resDifference.append(hour).append(" hours ago");
            } else if(hour > 0) {
                resDifference.append(hour).append(" hour ago");
            } else if(min > 1) {
                resDifference.append(min).append(" minutes ago");
            } else if(min > 0) {
                resDifference.append(min).append(" minute ago");
            } else if(seconds > 0) {
                resDifference.append(seconds).append(" seconds ago");
            } else {
                resDifference.append("Right now");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return resDifference.toString();
    }

    public static String formatDateFull(String unformattedDate) {
        String[] monthOptions = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] dateItems = unformattedDate.split("\\-|T|:");
        int monthInteger = Integer.parseInt(dateItems[1]);
        String monthField = monthOptions[monthInteger-1];
        String dayField = dateItems[2];
        String yearField = dateItems[0];
        return monthField + " " + dayField + ", " + yearField;
    }
}
