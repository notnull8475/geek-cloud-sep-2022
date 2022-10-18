package com.geekbrains.model;

public record RegistrationRequest(
        String username,
        String password
) implements CloudMessage {


    @Override
    public MessageType getType() {
        return MessageType.REGISTRATION;
    }
}
