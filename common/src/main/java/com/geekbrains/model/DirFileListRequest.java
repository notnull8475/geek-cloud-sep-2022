package com.geekbrains.model;

import lombok.Getter;

@Getter
public class DirFileListRequest implements CloudMessage {

    private final String dirName;

    public DirFileListRequest(String dirName) {
        this.dirName = dirName;
    }

    @Override
    public MessageType getType() {
        return MessageType.DIR_FILE_LIST_REQUEST;
    }
}
