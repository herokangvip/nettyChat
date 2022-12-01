package com.hero.nettychat.websocket.client;


import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 基于websocket的netty客户端
 */
@Slf4j
public class WebSocketMockClient {


    private static String account = "1860000000";
    //static String uriString = "ws://127.0.0.1:80/websocket/1212";
    static String uriString = "ws://127.0.0.1:80/push";

    private static final int READ_IDLE_TIME_OUT = 0; // 读超时  s
    private static final int WRITE_IDLE_TIME_OUT = 7;// 写超时
    private static final int ALL_IDLE_TIME_OUT = 0; // 所有超时


    private Bootstrap bootstrap;
    private EventLoopGroup group;

    public static void main(String[] args) throws Exception {
        WebSocketMockClient nettyClient = new WebSocketMockClient();
        nettyClient.connect();
    }



    public WebSocketMockClient() {
        init();
    }

    private void init() {
        //netty基本操作，线程组
        group = new NioEventLoopGroup();
        //netty基本操作，启动类
        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .group(group)
                .handler(new LoggingHandler(LogLevel.INFO))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("http-codec", new HttpClientCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 10));

                        // WebSocket数据压缩
                        pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
                        //7s没写就超时
                        pipeline.addLast(new IdleStateHandler(READ_IDLE_TIME_OUT, WRITE_IDLE_TIME_OUT,
                                ALL_IDLE_TIME_OUT, TimeUnit.SECONDS));
                        pipeline.addLast("SimpleUserEventChannelHandler",new MySimpleUserEventChannelHandler());

                        pipeline.addLast("ws-handler", new WebSocketClientHandler(WebSocketMockClient.this));
                    }
                });
    }

    public void connect() throws Exception {
        //websocke连接的地址，/hello是因为在服务端的websockethandler设置的
        URI websocketURI = new URI(uriString);
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        //todo 业务自己实现的鉴权用的，暂时屏蔽
        //httpHeaders.set(SessionConstants.AUTHORIZATION_HEAD, token);
        //httpHeaders.set(SessionConstants.APP_ACCOUNT, account);
        //进行握手
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory
                .newHandshaker(websocketURI, WebSocketVersion.V13, (String) null, true, httpHeaders);
        //客户端与服务端连接的通道，final修饰表示只会有一个
        //final Channel channel = bootstrap.connect(websocketURI.getHost(), websocketURI.getPort())
        //        .addListener(new ConnectionListener(WebSocketMockClient.this)).sync().channel();
        final Channel channel = bootstrap.connect(websocketURI.getHost(), websocketURI.getPort()).sync().channel();
        WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("ws-handler");
        handler.setHandshaker(handshaker);
        handshaker.handshake(channel);

//        MySimpleUserEventChannelHandler eventChannelHandler = (MySimpleUserEventChannelHandler)
//        channel.pipeline().get("SimpleUserEventChannelHandler");
//        eventChannelHandler.setHandshaker(handshaker);
//        handshaker.handshake(channel);
        //阻塞等待是否握手成功
        handler.handshakeFuture().sync();
        System.out.println("握手成功");
        //给服务端发送的内容，如果客户端与服务端连接成功后，可以多次掉用这个方法发送消息
        sengMessage(channel, "你好，我是client");
    }

    public static void sengMessage(Channel channel, String s) {
        TextWebSocketFrame frame = new TextWebSocketFrame(s);
        channel.writeAndFlush(frame).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    System.out.println("消息发送成功，发送的消息是：" + s);
                } else {
                    System.out.println("消息发送失败 " + channelFuture.cause().getMessage());
                }
            }
        });
    }

    public static void sengPingMessage(Channel channel) {
        PingWebSocketFrame frame = new PingWebSocketFrame();
        channel.writeAndFlush(frame);
    }









    //====================end



    public void main2(String[] args) throws Exception {
        //netty基本操作，线程组
        EventLoopGroup group = new NioEventLoopGroup();
        //netty基本操作，启动类
        Bootstrap boot = new Bootstrap();
        boot.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .group(group)
                .handler(new LoggingHandler(LogLevel.INFO))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("http-codec", new HttpClientCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 10));
                        //7s没写就超时
                        pipeline.addLast(new IdleStateHandler(READ_IDLE_TIME_OUT, WRITE_IDLE_TIME_OUT,
                                ALL_IDLE_TIME_OUT, TimeUnit.SECONDS));
                        pipeline.addLast(new SimpleUserEventChannelHandler<IdleStateEvent>() {
                            @Override
                            protected void eventReceived(ChannelHandlerContext ctx,
                                                         IdleStateEvent idleStateEvent) throws Exception {
                                if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                                    log.info("已经 7 秒没有发送信息！发送 ping");
                                    //向服务端发送Ping消息
                                    sengPingMessage(ctx.channel());
                                } else {
                                    ctx.fireUserEventTriggered(idleStateEvent);
                                }
                            }
                        });

                        pipeline.addLast("ws-handler", new WebSocketClientHandler(WebSocketMockClient.this));
                    }
                });
        //websocke连接的地址，/hello是因为在服务端的websockethandler设置的
        URI websocketURI = new URI(uriString);
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        //todo 业务自己实现的鉴权用的，暂时屏蔽
        //httpHeaders.set(SessionConstants.AUTHORIZATION_HEAD, token);
        //httpHeaders.set(SessionConstants.APP_ACCOUNT, account);
        //进行握手
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory
                .newHandshaker(websocketURI, WebSocketVersion.V13, (String) null, true, httpHeaders);
        //客户端与服务端连接的通道，final修饰表示只会有一个
        final Channel channel = boot.connect(websocketURI.getHost(), websocketURI.getPort()).sync().channel();
        WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("ws-handler");
        handler.setHandshaker(handshaker);
        handshaker.handshake(channel);
        //阻塞等待是否握手成功
        handler.handshakeFuture().sync();
        System.out.println("握手成功");
        //给服务端发送的内容，如果客户端与服务端连接成功后，可以多次掉用这个方法发送消息
        sengMessage(channel, "你好，我是client");
//        System.out.println("===================分隔符====================");
//        Thread.sleep(2000);
//        sengMessage(channel, "你好，我是client");
//
//        System.out.println("===================分隔符====================");
//        Thread.sleep(2000);
//        sengMessage(channel, "你好，我是client");
//
//        System.out.println("===================分隔符====================");
//        Thread.sleep(2000);
//        sengMessage(channel, "你好，我是client");


    }



}

