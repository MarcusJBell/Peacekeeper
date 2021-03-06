package com.gmail.sintinium.peacekeeper.db.utils;

import org.apache.commons.lang3.StringUtils;

public class SQLUtils {

    // 'bob', 5, 'jim', etc..
    public static String getAsSQLStringList(Object[] strings) {
        String string = "(";
        for (int i = 0; i < strings.length - 1; i++) {
            if (strings[i] == null) {
                strings[i] = "null";
            }
            if (!StringUtils.isNumeric(String.valueOf(strings[i]))) {
                string += "'" + strings[i].toString().replaceAll("\'", "''") + "',";
            } else
                string += strings[i] + ",";
        }
        String finalString = String.valueOf(strings[strings.length - 1]);
        if (!StringUtils.isNumeric(finalString)) {
            if (strings[strings.length - 1] == null) {
                string += "'" + null + "'";
            } else
                string += "'" + strings[strings.length - 1].toString().replaceAll("\'", "'") + "'";
        } else
            string += finalString;

        return string + ")";
    }

    // new String[] { "'value1'", "2", "'value2'" };
    public static String[] getAsSQLArray(Object[] strings) {
        String[] returnList = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            String value = StringUtils.isNumeric(String.valueOf(strings[i])) ? String.valueOf(strings[i]) : "'" + strings[i].toString().replaceAll("\'", "'") + "'";
            returnList[i] = value;
        }
        return returnList;
    }

    // col1=val1, col2=val2, etc..
    public static String getAsSQLSet(Object[] colNames, Object[] values) {
        String string = "";

        for (int i = 0; i < colNames.length; i++) {
            if (i != 0)
                string += ",";
            String colName = String.valueOf(colNames[i]);
            String value = String.valueOf(values[i]);
            if (!StringUtils.isNumeric(value)) {
                value = "'" + value.replaceAll("\'", "'") + "'";
            }
            string += colName + "=" + value;
        }

        return string;
    }

}
