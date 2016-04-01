package com.gmail.sintinium.peacekeeper.db.tables;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.db.utils.SQLUtils;
import lib.PatPeter.SQLibrary.Database;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseTable {

    public Database db;
    public String tableName;
    public Peacekeeper peacekeeper;

    public BaseTable(@Nonnull Peacekeeper peacekeeper, @Nonnull String tableName) {
        this.peacekeeper = peacekeeper;
        db = peacekeeper.database;
        this.tableName = tableName;
    }

    public void init(@Nonnull String tableSet) {
        try {
            db.query("CREATE TABLE IF NOT EXISTS " +
                    "" + tableName +
                    " ( " + tableSet + " );");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param columnList Array of variables that are the names of column names
     * @param valueList  Array of variables that are in the same order as column names
     * @param replace    Should insert or should replace if already exists
     * @return returns the primary key of the inserted values. Returns null if SQLException is thrown
     */
    public Integer insert(@Nonnull Object[] columnList, @Nonnull Object[] valueList) {
        String cols = SQLUtils.getAsSQLStringList(columnList);
        String values = SQLUtils.getAsSQLStringList(valueList);
        try {
//            if (replace) {
//                db.query("INSERT OR REPLACE INTO " + tableName + " " + cols +
//                        " VALUES " + values + ";");
//            } else {
                db.query("INSERT INTO " + tableName + " " + cols +
                        " VALUES " + values + ";");
//            }
            ResultSet set = db.query("SELECT last_insert_rowid()");
            int rowID = set.getInt(1);
            set.close();
            return rowID;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserts values into a table
     *
     * @param columnList Array of variables that are the names of column names
     * @param valueList  Array of variables that are in the same order as column names
     * @return returns the primary key of the inserted values
     */
//    public Integer insert(@Nonnull Object[] columnList, @Nonnull Object[] valueList) {
//        return insert(columnList, valueList, false);
//    }

    /**
     * Inserts or replaces values into a table
     *
     * @param columnList Array of variables that are the names of column names
     * @param valueList  Array of variables that are in the same order as column names
     * @return returns the primary key of the inserted values
     */
//    public Integer insertOrReplace(@Nonnull Object[] columnList, @Nonnull Object[] valueList) {
//        return insert(columnList, valueList, true);
//    }

    /**
     * Deletes the row that contains the given variables
     *
     * @param where Name of variable to find
     * @param value Value of variable by the name of 'where'
     */
    public void deleteRow(@Nonnull String where, @Nonnull String value) {
        if (!StringUtils.isNumeric(value))
            value = "'" + value.replaceAll("\'", "'") + "'";
        try {
            db.query("DELETE FROM " + tableName + " WHERE " + where + "=" + value + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the row that contains the given variables
     *
     * @param where Name of variable to find
     * @param value Value of variable by the name of 'where'
     */
    public void deleteRow(@Nonnull String where, int value) {
        deleteRow(where, String.valueOf(value));
    }

    /**
     * Updates a value in a table
     *
     * @param set   New value to be set
     * @param where Name of variable to update
     * @param value Old value to be replaced
     */
    public void updateValue(@Nonnull String set, @Nonnull String where, @Nonnull String value) {
        try {
            if (!StringUtils.isNumeric(value))
                value = "'" + value.replaceAll("\'", "'") + "'";
            PreparedStatement statement = db.prepare("UPDATE " + tableName +
                    " SET " + set +
                    " WHERE " + where + "=" + value + ";");
            db.query(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a value in a table
     *
     * @param set   New value to be set
     * @param where Name of variable to update
     * @param value Old value to be replaced
     */
    public void updateValue(@Nonnull String set, @Nonnull String where, int value) {
        updateValue(set, where, String.valueOf(value));
    }

    /**
     * Gets string from table
     *
     * @param select Name of column to select
     * @param where  Name of variable to find
     * @param value  Value of variable found
     * @param like   Will be like given value or exact
     * @return returns String of found column
     */
    private String getString(@Nonnull String select, @Nonnull String where, @Nonnull String value, boolean like) {
        try {
            if (!StringUtils.isNumeric(value))
                value = "'" + value.replaceAll("\'", "'") + "'";
            ResultSet set;
            if (like) {
                set = db.query("SELECT " + select + " FROM " + tableName + " WHERE " + where + " LIKE " + value + " ORDER BY Length(" + where + ") ASC;");
            } else {
                set = db.query("SELECT " + select + " FROM " + tableName + " WHERE " + where + "=" + value + ";");
            }
            if (!set.next()) {
                set.close();
                return null;
            }
            String s = set.getString(1);
            set.close();
            return s;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets string from table
     *
     * @param select Name of column to select
     * @param where  Name of variable to find
     * @param value  Value of variable found
     * @return returns String of found column
     */
    public String getString(@Nonnull String select, @Nonnull String where, @Nonnull String value) {
        return getString(select, where, value, false);
    }

    /**
     * Gets string from table
     *
     * @param select Name of column to select
     * @param where  Name of variable to find
     * @param value  Value of variable found
     * @return returns String of found column
     */
    public String getString(@Nonnull String select, @Nonnull String where, int value) {
        return getString(select, where, String.valueOf(value), false);
    }

    /**
     * Gets string from table
     *
     * @param select Name of column to select
     * @param where  Name of variable to find
     * @param value  Value of variable found
     * @return returns String of found column like value
     */
    public String getStringLike(@Nonnull String select, @Nonnull String where, @Nonnull String value) {
        return getString(select, where, value, true);
    }

    /**
     * Gets Long value from table
     *
     * @param select Name of column to select
     * @param where  Name of variable to find
     * @param value  Value of variable found
     * @return returns String of found column like value
     */
    @Nullable
    public Long getLong(@Nonnull String select, @Nonnull String where, @Nonnull String value) {
        String string = getString(select, where, value);
        if (string == null) return null;
        Long result;
        try {
            result = Long.parseLong(string);
        } catch (NumberFormatException e) { // Will throw if String from DB is null Example: PlayerID on IP ban is always null
            return null;
        }
        return result;
    }

    /**
     * Gets Long value from table
     *
     * @param select Name of column to select
     * @param where  Name of variable to find
     * @param value  Value of variable found
     * @return returns String of found column like value
     */
    @Nullable
    public Long getLong(@Nonnull String select, @Nonnull String where, int value) {
        return getLong(select, where, String.valueOf(value));
    }

    /**
     * Gets Integer value from table
     *
     * @param select Name of column to select
     * @param where  Name of variable to find
     * @param value  Value of variable found
     * @return returns String of found column like value
     */
    @Nullable
    public Integer getInt(@Nonnull String select, @Nonnull String where, @Nonnull String value) {
        Long getLong = getLong(select, where, value);
        if (getLong == null) return null;
        if (getLong < Integer.MIN_VALUE || getLong > Integer.MAX_VALUE) {
            return null;
        }
        return getLong.intValue();
    }

    /**
     * Gets Integer value from table
     *
     * @param select Name of column to select
     * @param where  Name of variable to find
     * @param value  Value of variable found
     * @return returns String of found column like value
     */
    public Integer getInt(@Nonnull String select, @Nonnull String where, int value) {
        return getInt(select, where, String.valueOf(value));
    }

    /**
     * Checks if value exists in table
     *
     * @param where Name of variable to find
     * @param value Value of variable found
     * @param like  Should find value like the input value
     * @return returns true if value is found in the table
     */
    public boolean doesValueExist(@Nonnull String where, @Nonnull String value, boolean like) {
        try {
            if (!StringUtils.isNumeric(value))
                value = "'" + value.replaceAll("\'", "'") + "'";
            ResultSet set;
            if (like) {
                set = db.query("SELECT EXISTS (" +
                        " SELECT 1 FROM " + tableName + " WHERE " + where + " LIKE " + value + " LIMIT 1);");
            } else {
                set = db.query("SELECT EXISTS (" +
                        " SELECT 1 FROM " + tableName + " WHERE " + where + "=" + value + " LIMIT 1);");
            }
            boolean b = set.getInt(1) == 1;
            set.close();
            return b;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if value exists in table
     *
     * @param where Name of variable to find
     * @param value Value of variable found
     * @return returns true if value is found in the table
     */
    public boolean doesValueExist(@Nonnull String where, @Nonnull String value) {
        return doesValueExist(where, value, false);
    }

    /**
     * Checks if value exists in table
     *
     * @param where Name of variable to find
     * @param value Value of variable found
     * @return returns true if value is found in the table
     */
    public boolean doesValueExist(@Nonnull String where, int value) {
        return doesValueExist(where, String.valueOf(value));
    }

    /**
     * Gets ResultSet from table
     *
     * @param select Name of column to select
     * @param where  Name of variable to find
     * @param value  Value of variable found
     * @return returns ResultSet of found rows
     */
    @Nullable
    public ResultSet getSet(@Nonnull String select, @Nonnull String where, @Nonnull String value) {
        ResultSet set = null;
        try {
            if (!StringUtils.isNumeric(value)) {
                value = "'" + value.replaceAll("\'", "'") + "'";
            }
            set = db.query("SELECT " + select + " FROM " + tableName + " WHERE " + where + "=" + value + ";");
            if (!set.next()) {
                set.close();
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return set;
    }

    /**
     * Gets a result set for all data at the given row
     *
     * @param rowID Row to fetch data
     * @return Returns a result set for all columns in a row
     * @throws SQLException Thrown if database exception occurred
     */
    public ResultSet getStarSet(int rowID) throws SQLException {
        return db.query("SELECT * FROM " + tableName + " WHERE rowID=" + rowID + ";");
    }

    /**
     * Gets ResultSet from table
     *
     * @param select Name of column to select
     * @param where  Name of variable to find
     * @param value  Value of variable found
     * @return returns ResultSet of found rows
     */
    @Nullable
    public ResultSet getSet(@Nonnull String select, @Nonnull String where, int value) {
        return getSet(select, where, String.valueOf(value));
    }

    /**
     * Gets value count from table
     *
     * @param where Name of variable to find
     * @param value Value of variable found
     * @return returns Count of variables
     */
    public Integer valueCount(@Nonnull String where, @Nonnull String value) {
        ResultSet set = null;
        Integer count = null;
        try {
            set = db.query("SELECT COUNT(*) FROM " + tableName + " WHERE " + where + "=" + value + ";");
            if (!set.next()) {
                set.close();
                return null;
            }
            count = set.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (set == null) return null;
        return count;
    }

    public int tableRowCount() {
        try {
            ResultSet set = db.query("SELECT COUNT(*) FROM " + tableName + ";");
            if (!set.next()) {
                set.close();
                return 0;
            }
            int count = set.getInt(1);
            set.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gets value count from table
     *
     * @param where Name of variable to find
     * @param value Value of variable found
     * @return returns Count of variables
     */
    public Integer valueCount(@Nonnull String where, int value) {
        return valueCount(where, String.valueOf(value));
    }

}
