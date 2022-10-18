package com.geekbrains.db;

public interface SQLScripts {
    String registerUser = "insert into users(username,password,path) values(?,?,?)";
    String addSession = "insert into session(user_id,UUID,time,active) values(?,?,?,1)";
    String getUserId = "select id from users where username=?";
    String checkUser = "select * from users where username=? and password=?";
    String logoutUser = "update session set active = 0 where id = ?";
}
