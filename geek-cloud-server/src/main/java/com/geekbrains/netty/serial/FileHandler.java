package com.geekbrains.netty.serial;

import com.geekbrains.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverDir = Path.of("server_files").toAbsolutePath();
        ctx.writeAndFlush(new ListMessage(serverDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        log.debug("Received: {}", cloudMessage.getType());
        if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(serverDir.resolve(fileMessage.getFileName()), fileMessage.getBytes());
            ctx.writeAndFlush(new ListMessage(serverDir));
        } else if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(serverDir.resolve(fileRequest.fileName())));
        } else if (cloudMessage instanceof DirFileListRequest dirList) {
            if (dirList.getDirName() != null) {
                if (new File(serverDir.toString() + File.separator + dirList.getDirName()).isDirectory()) {
                    serverDir = serverDir.resolve(dirList.getDirName()).normalize();
                    log.debug("serverDir: {}", serverDir);
                }
            }
            ctx.writeAndFlush(new ListMessage(serverDir));

        } else if (cloudMessage instanceof DeleteFile delFile) {
            File toDelete = new File(serverDir + File.separator + delFile.getFileName());
            if (toDelete.exists()) {

                if (toDelete.isFile()) {
                    log.debug("IS FILE");
                    if (toDelete.delete()) {
                        log.debug("File is deleted");
                    } else {
                        log.error("File is not deleted");
                    }
                } else if (toDelete.isDirectory()) {
                    log.debug("IS DIRECTORY");
                    String[] entries = toDelete.list();
                    for (String s : entries) {
                        File currentFile = new File(toDelete.getPath(), s);
                        currentFile.delete();
                    }
                }
            }

        } else if (cloudMessage instanceof RenameFile renameFile) {
            File file = new File(serverDir + File.separator + renameFile.getFileName());
            File newNameFile = new File(serverDir + File.separator + renameFile.getNewFileName());
            if (newNameFile.exists()) {
                log.error("File with name " + renameFile.getNewFileName() + " is exist ");
            } else {
                log.debug("file to rename " + file.getAbsolutePath());
                log.debug("new file name " + newNameFile.getAbsolutePath());
                if (file.renameTo(newNameFile)) {
                    log.debug("File is renamed");
                } else {
                    log.debug("file is not renamed");
                }

            }
        } else if (cloudMessage instanceof CreatePathRequest path) {
            File createPath = new File(serverDir + File.separator + path.name());
            if (createPath.exists()) {
                ctx.writeAndFlush(new ResultMessage("Path " + path.name() + " is exist! "));
                return;
            }
            if (!createPath.mkdir()) {
                ctx.writeAndFlush(new ResultMessage("Path " + path.name() + " is not created! "));
            }
            ctx.writeAndFlush(new ListMessage(serverDir));
        } else if (cloudMessage instanceof CreateFileRequest file) {
            File createFile = new File(serverDir + File.separator + file.name());
            if (createFile.exists()) {
                ctx.writeAndFlush(new ResultMessage("Path " + file.name() + " is exist! "));
                return;
            }
            if (!createFile.createNewFile()) {
                ctx.writeAndFlush(new ResultMessage("Path " + file.name() + " is not created! "));
            }
            ctx.writeAndFlush(new ListMessage(serverDir));
        }
    }
}
