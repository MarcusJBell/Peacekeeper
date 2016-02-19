package com.gmail.sintinium.peacekeeper.db.utils;

public class SQLTableUtils {

    public static final String TEXT = "varchar(500)", INTEGER = "INTEGER", VARCHAR = "varchar";

    // Username TEXT
    public static String getTableSet(String[] names, String[] types) {
        String result = "";
        for (int i = 0; i < names.length; i++) {
            if (i != 0) result += ",";
            result += "'" + names[i] + "' " + types[i];
        }
        return result;
    }

}
