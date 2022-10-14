package com.geekbrains.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class DButils {

    private static Connection connection;


    public static void init() {
        String checkUserTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='users';";
        String checkPathsTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='path';";
        log.debug("-------------------------------   INIT SCRIPT ------------------------------------------");
        try {
            connection = DBConnection.getConnection();
            ResultSet rs = connection.prepareStatement(checkUserTable).executeQuery();
            if (rs.next()) {
                log.debug("table is exist");
            } else {
                log.debug("table to create");
            }


        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

