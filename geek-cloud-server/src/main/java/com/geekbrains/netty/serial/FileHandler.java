package com.geekbrains.netty.serial;

import com.geekbrains.User;
import com.geekbrains.db.DBConnection;
import com.geekbrains.db.DButils;
import com.geekbrains.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverDir;
    private User user;
    private DButils dbUtils;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        log.debug("NEW CLIENT IS CONNECTED " + this.getClass().getName());
        dbUtils = new DButils(DBConnection.getConnection());
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
            getDirList(ctx, dirList);
        } else if (cloudMessage instanceof DeleteFile delFile) {
            deleteFile(delFile);
        } else if (cloudMessage instanceof RenameFile renameFile) {
            renameFile(renameFile);
        } else if (cloudMessage instanceof CreatePathRequest path) {
            ctx.writeAndFlush(createPath(path.name()));
            ctx.writeAndFlush(new ListMessage(serverDir));
        } else if (cloudMessage instanceof CreateFileRequest file) {
            ctx.writeAndFlush(createFile(file.name()));
            ctx.writeAndFlush(new ListMessage(serverDir));
        } else if (cloudMessage instanceof AuthorizationRequest auth) {
            authUser(auth.username(), auth.password(), ctx);
        } else if (cloudMessage instanceof RegistrationRequest reg) {
            ResultMessage rm = regUser(reg.username(), reg.password());
            if (rm.type().equals(ResultType.AUTH_ERROR)) ctx.close();
            else ctx.writeAndFlush(rm);
        }
    }

    private ResultMessage regUser(String username, String password) {
        if (createPath(username).equals(ResultType.SUCCESS)) {
            user = dbUtils.registerUser(username, password, serverDir + File.separator + username);
            if (user != null) return new ResultMessage(ResultType.AUTH_SUCCESS, "Вы зарегестрированы");
        }
        return new ResultMessage(ResultType.AUTH_ERROR, "Регистрация не удалась");

    }

    private ResultMessage createFile(String file) {
        File createFile = new File(serverDir + File.separator + file);
        if (createFile.exists()) {
            return new ResultMessage(ResultType.MESSAGE, "Path " + file + " is exist! ");
        }
        try {
            if (!createFile.createNewFile()) {
                return new ResultMessage(ResultType.ERROR, "Path " + file + " is not created! ");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResultMessage(ResultType.SUCCESS, "File " + file + " is created!");
    }

    private ResultMessage createPath(String path) {
        File createPath = new File(serverDir + File.separator + path);
        if (createPath.exists()) {
            return new ResultMessage(ResultType.MESSAGE, "Path " + path + " is exist! ");
        }
        if (!createPath.mkdir()) {
            return new ResultMessage(ResultType.ERROR, "Path " + path + " is not created! ");
        }
        return new ResultMessage(ResultType.SUCCESS, "Path " + path + " is created");
    }

    private void renameFile(RenameFile renameFile) {
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
    }

    private void deleteFile(DeleteFile delFile) {
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
                assert entries != null;
                for (String s : entries) {
                    File currentFile = new File(toDelete.getPath(), s);
                    currentFile.delete();
                }
            }
        }
    }

    private void getDirList(ChannelHandlerContext ctx, DirFileListRequest dirList) throws IOException {
        if (dirList.getDirName() != null) {
            if (new File(serverDir.toString() + File.separator + dirList.getDirName()).isDirectory()) {
                serverDir = serverDir.resolve(dirList.getDirName()).normalize();
                log.debug("serverDir: {}", serverDir);
            }
        }
        ctx.writeAndFlush(new ListMessage(serverDir));
    }


    private void authUser(String username, String password, ChannelHandlerContext ctx) {
        user = dbUtils.getUser(username, password);
        if (user != null) {
            ctx.writeAndFlush(new ResultMessage(ResultType.AUTH_SUCCESS, "Авторизация прошла успешно"));
        } else {
            ctx.writeAndFlush(new ResultMessage(ResultType.AUTH_ERROR, "Неверный логин или пароль"));
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("CLIENT IS UNREGISTERED " + this.getClass().getName());
    }
}
