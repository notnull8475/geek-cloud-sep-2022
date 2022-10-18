package com.geekbrains.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        return getSQLiteConnection();
    }

    public static void closeQuietly(Connection connection){
        try {
            connection.close();
        } catch (Exception ignored){}
    }
    public static void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception ignored) {
        }
    }

    private static Connection getSQLiteConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return  DriverManager.getConnection("jdbc:sqlite:clients_db.sqlite");
    }
}
