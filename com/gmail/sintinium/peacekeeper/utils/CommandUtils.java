package com.gmail.sintinium.peacekeeper.utils;

import java.util.regex.Pattern;

public class CommandUtils {

    // Regex pattern to check if string is IP or not
    public static final String IP_ADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    public static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";

    // Turns array to single string starting at 'startIndex' index
    public static String argsToReason(String[] args, int startIndex) {
        if (startIndex >= args.length) return null;
        String reason = "";
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex)
                reason += " ";
            reason += args[i];
        }
        return reason;
    }

    // Regex to check if input is IP
    public static boolean isIP(String input) {
        Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);
        return pattern.matcher(input).find();
    }

    public static boolean isUUID(String input) {
        Pattern pattern = Pattern.compile(UUID_PATTERN);
        return pattern.matcher(input).find();
    }

}
