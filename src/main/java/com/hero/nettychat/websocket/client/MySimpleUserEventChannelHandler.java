package com.hero.nettychat.websocket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleUserEventChannelHandler;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import static com.hero.nettychat.websocket.client.WebSocketMockClient.sengMessage;
import static com.hero.nettychat.websocket.client.WebSocketMockClient.sengPingMessage;

@Slf4j
public class MySimpleUserEventChannelHandler extends SimpleUserEventChannelHandler<IdleStateEvent> {
    //WebSocketClientHandshaker handshakeFuture;

    @Override
    protected void eventReceived(ChannelHandlerContext ctx,
                                 IdleStateEvent idleStateEvent) throws Exception {
        if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
            log.info("已经 7 秒没有发送信息！发送 ping");
            //向服务端发送Ping消息
            sengPingMessage(ctx.channel());
            sengMessage(ctx.channel(), "你好，我是client");
        } else {
            ctx.fireUserEventTriggered(idleStateEvent);
        }
    }



}
