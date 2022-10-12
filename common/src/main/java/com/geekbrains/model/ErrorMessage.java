package com.geekbrains.model;

public record ErrorMessage(
        String message
) implements CloudMessage {
    @Override
    public MessageType getType() {
        return MessageType.ERROR;
    }
}
