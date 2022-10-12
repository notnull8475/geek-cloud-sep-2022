package com.geekbrains.model;

import lombok.Getter;

@Getter
public class DirFileListRequest implements CloudMessage {

    private String dirName = null;

    public DirFileListRequest(String dirName) {
        this.dirName = dirName;
    }
    public DirFileListRequest(){}

    @Override
    public MessageType getType() {
        return MessageType.DIR_FILE_LIST_REQUEST;
    }
}
