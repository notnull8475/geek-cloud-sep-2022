package com.geekbrains.model;

import lombok.Getter;

public record CreatePathRequest(String name) implements CloudMessage {

    @Override
    public MessageType getType() {
        return MessageType.CREATE_NEW_PATH;
    }
}
