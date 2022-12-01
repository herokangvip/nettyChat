package com.hero.nettychat.websocket.client;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;

/**
 * 负责监听启动时连接失败，重新连接功能
 *
 * @author yinjihuan
 */
public class ConnectionListener implements ChannelFutureListener {

    private WebSocketMockClient webSocketMockClient;

    public ConnectionListener(WebSocketMockClient webSocketMockClient) {
        this.webSocketMockClient = webSocketMockClient;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    System.err.println("服务端链接不上，开始重连操作...");
                    try {
                        webSocketMockClient.connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 10L, TimeUnit.SECONDS);
        } else {
            System.err.println("服务端 初始化 链接成功...");
        }
    }
}
