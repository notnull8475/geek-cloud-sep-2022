package com.geekbrains.db;

import com.geekbrains.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Slf4j
public class DButils implements SQLScripts {

    private Connection connection;

    public DButils(Connection connection) {
        this.connection = connection;
    }

    public User registerUser(String username, String password,String path) {
        try (PreparedStatement ps = connection.prepareStatement(registerUser)) {
            ps.setString(0,username);
            ps.setString(1,password);
            ps.setString(2,path);


        } catch (SQLException e) {
            log.error("ERROR ON SQL SCRIPT " + registerUser);
            log.error("ERROR INFORMATION " + e.getMessage());
            log.error("ERROR SQLSTATE " + e.getSQLState());
        }
        return null;
    }

    public User getUser(String username, String password) {
        try (PreparedStatement pr = connection.prepareStatement(checkUser)) {
            pr.setString(0, username);
            pr.setString(1, password);
            ResultSet resultSet = pr.executeQuery();
            if (resultSet.next()) {
                User user = new User(username,
                        UUID.randomUUID(),
                        resultSet.getInt("id"),
                        resultSet.getString("path"));
                addSession(user);
                return user;
            }
        } catch (SQLException e) {
            log.error("ERROR ON SQL SCRIPT " + checkUser + " " + username + " " + password);
            log.error("ERROR INFORMATION " + e.getMessage());
            log.error("ERROR SQLSTATE " + e.getSQLState());
        }
        return null;
    }

    public void logoutUser(User user) {

    }

    private int getIdOfUser(String username){
        try(PreparedStatement ps = connection.prepareStatement(getUserId)) {
            ps.setString(0, username);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e){
            log.error("ERROR ON SQL SCRIPT " + getUserId);
            log.error("ERROR INFORMATION " + e.getMessage());
            log.error("ERROR SQLSTATE " + e.getSQLState());
        }
        return -1;
    }

    private void addSession(User user) {

    }

    public static void check() {
        String checkUserTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='users';";
        String createUserTable = "create table clients" +
                "(id INTEGER not null constraint clients_pk primary key autoincrement," +
                " username TEXT    not null," +
                " password TEXT," +
                " path     TEXT    not null );";
        String createSessionsTable = "CREATE TABLE IF NOT EXISTS session" +
                "(user_id INTEGER not null constraint session___fk references clients," +
                "UUID    TEXT    not null," +
                "active INTEGER not null," +
                "time    TEXT);";
        log.debug("-------------------------------   INIT SCRIPT ------------------------------------------");
        try {
            Connection connection = DBConnection.getConnection();
            ResultSet rs = connection.prepareStatement(checkUserTable).executeQuery();
            if (rs.next()) {
                log.debug("table is exist");
            } else {
                ResultSet createRS = connection.prepareStatement(createUserTable).executeQuery();
                ResultSet createS = connection.prepareStatement(createSessionsTable).executeQuery();
                if (createRS.next()) log.debug("table USERS created");
                if (createS.next()) log.debug("table SESSIONS is created");
            }

            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

