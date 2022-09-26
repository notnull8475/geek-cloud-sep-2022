package com.geekbrains.netty.serial;

import com.geekbrains.model.CloudMessage;
import com.geekbrains.model.FileMessage;
import com.geekbrains.model.FileRequest;
import com.geekbrains.model.ListMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverDir = Path.of("server_files");
        ctx.writeAndFlush(new ListMessage(serverDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        log.debug("Received: {}", cloudMessage.getType());
        if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(serverDir.resolve(fileMessage.getFileName()), fileMessage.getBytes());
            ctx.writeAndFlush(new ListMessage(serverDir));
        } else if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(serverDir.resolve(fileRequest.getFileName())));
        }
    }
}
