package com.geekbrains.model;

import com.geekbrains.User;

public class LogoutRequest implements CloudMessage {
    User user;

    public User getUser() {
        return user;
    }

    @Override
    public MessageType getType() {
        return MessageType.LOGOUT;
    }
}
