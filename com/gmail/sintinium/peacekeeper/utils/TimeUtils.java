package com.gmail.sintinium.peacekeeper.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    // Translates a TimeUnit to mills. Ex: 1s would be one seconds worth of milliseconds so it would return 1000.
    // 1m1m1s would return 2 minutes and 1 second in milliseconds
    public static long stringToMillis(String input) {
        if (input == null) return 0;
        long result = 0;
        String number = "";
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                number += c;
            } else if (Character.isLetter(c) && !number.isEmpty()) {
                result += convert(Integer.parseInt(number), c);
                number = "";
            }
        }

        return result;
    }

    // Converts units to milliseconds as close as possible without account for leap years etc..
    private static long convert(long value, char unit) {
        switch (unit) {
            case 'y':
                return value * 1000 * 60 * 60 * 24 * 7 * 52;
            case 'w':
                return value * 1000 * 60 * 60 * 24 * 7;
            case 'd':
                return value * 1000 * 60 * 60 * 24;
            case 'h':
                return value * 1000 * 60 * 60;
            case 'm':
                return value * 1000 * 60;
            case 's':
                return value * 1000;
        }
        return 0;
    }

    // Turns milliseconds into readable time that will be displayed to the user. Cannot be reversed in the above methods.
    public static String millsToString(long milliseconds) {
        String finalTime = "";
        int years = (int) TimeUnit.MILLISECONDS.toDays(milliseconds / 365);
        milliseconds -= TimeUnit.DAYS.toMillis(years * 365);

        int weeks = (int) TimeUnit.MILLISECONDS.toDays(milliseconds / 7);
        milliseconds -= TimeUnit.DAYS.toMillis(weeks * 7);

        int days = (int) Math.floor(TimeUnit.MILLISECONDS.toDays(milliseconds));
        milliseconds -= TimeUnit.DAYS.toMillis(days);

        int hours = (int) Math.floor(TimeUnit.MILLISECONDS.toHours(milliseconds));
        milliseconds -= TimeUnit.HOURS.toMillis(hours);

        int minutes = (int) Math.floor(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes);

        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds);

        Map<String, Integer> times = new LinkedHashMap<>();
        times.put("year", years);
        times.put("week", weeks);
        times.put("day", days);
        times.put("hour", hours);
        times.put("minute", minutes);
        times.put("second", seconds);

        Iterator it = times.entrySet().iterator();
        int count = 0;
        while (it.hasNext()) {
            Entry pair = (Entry) it.next();
            int value = (Integer) pair.getValue();
            String unit = (String) pair.getKey();
            String convertToReadable = convertToReadable(value, unit);
            if (convertToReadable == null) continue;
            if (count != 0)
                finalTime += ", ";
            finalTime += convertToReadable;
            count++;
        }

        if (finalTime.equals("")) {
            finalTime = "1 second";
        }
        return finalTime;
    }

    // Actually puts the values and units into readable forum.
    private static String convertToReadable(int value, String unit) {
        if (value <= 0) return null;
        // If value > 1 add s to send to make it plural
        return value + " " + unit + (value > 1 ? "s" : "");
    }

}
