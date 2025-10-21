package com.example.parkeeriotapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtil {
    public static long parseDateTimeToMillis(String datetime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
        Date date = sdf.parse(datetime);
        return date != null ? date.getTime() : 0;
    }
    public static boolean isExpired(String endDateTime) {
        try {
            long endMillis = parseDateTimeToMillis(endDateTime);
            boolean expired = System.currentTimeMillis() > endMillis;
            return expired;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

