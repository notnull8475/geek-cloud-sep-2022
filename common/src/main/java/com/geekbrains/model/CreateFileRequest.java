package com.geekbrains.model;

public record CreateFileRequest(String name) implements CloudMessage {

    @Override
    public MessageType getType() {
        return MessageType.CREAT_NEW_FILE;
    }
}
