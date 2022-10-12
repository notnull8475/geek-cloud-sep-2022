package com.geekbrains.model;

public class AuthorizationRequest implements CloudMessage{


    @Override
    public MessageType getType() {
        return MessageType.AUTHORIZATION;
    }
}
