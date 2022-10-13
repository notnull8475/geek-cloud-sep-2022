package com.geekbrains.model;

public record ResultMessage(
        ResultType type,
        String message
) implements CloudMessage {
    @Override
    public MessageType getType() {
        return MessageType.RESULT;
    }
}
