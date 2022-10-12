package com.geekbrains.model;

import lombok.Getter;

@Getter
public class RenameFile implements CloudMessage {

    private final String fileName;
    private final String newFileName;

    public RenameFile(String fileName, String newFileName) {
        this.fileName = fileName;
        this.newFileName = newFileName;
    }

    @Override
    public MessageType getType() {
        return MessageType.FILE_RENAME;
    }
}
