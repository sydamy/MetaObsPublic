package fr.ufc.metaobs.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    /**
     * get the current date
     *
     * @param seperator - the separator between year, month and day
     * @return the current date
     */
    public static String formatDate(String seperator) {
        SimpleDateFormat model = new SimpleDateFormat("yyyy" + seperator + "MM" + seperator + "dd");
        return model.format(new Date());
    }

}
