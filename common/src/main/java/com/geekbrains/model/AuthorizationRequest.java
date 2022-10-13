package com.geekbrains.model;

public record AuthorizationRequest(
        String username,
        String password
) implements CloudMessage {


    @Override
    public MessageType getType() {
        return MessageType.AUTHORIZATION;
    }
}
