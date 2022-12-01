package com.hero.nettychat.websocket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;


/**
 * Netty 服务
 * Created by 尼恩 @ 疯狂创客圈
 */
//@Component
@Slf4j
public class WebSocketServer {


    //@Value("${tunnel.websocket.port}")
    private static int websocketPort;


    //@Value("${websocket.register.gateway}")
    private String websocketRegisterGateway;

    private static final EventLoopGroup group = new NioEventLoopGroup();
    private static Channel channel;


    public static void main(String[] args) {
        String getenv = System.getenv("ws.port");
        log.info("ws.port:" + getenv);
        websocketPort = Integer.parseInt(getenv);
        new Thread(() -> {
            startServer(websocketPort);
        }).start();

    }

    /**
     * 停止即时通讯服务器
     */
    public static void stopServer() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
    }

    /**
     * 启动即时通讯服务器
     */
    public static void startServer(int port) {


        ChannelFuture channelFuture = null;
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChatServerInitializer());
        InetSocketAddress address = new InetSocketAddress(port);


        channelFuture = serverBootstrap.bind(address);
//        channelFuture.syncUninterruptibly();

        channel = channelFuture.channel();
        // 返回与当前Java应用程序关联的运行时对象
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopServer();
            }
        });

        log.info("\n----------------------------------------------------------\n\t" +
                "Nett WebSocket 服务 is running! Access Port:{}\n\t", websocketPort);

        channelFuture.channel().closeFuture().syncUninterruptibly();
    }

    /**
     * 内部类
     */
    static class ChatServerInitializer extends ChannelInitializer<Channel> {
        private static final int READ_IDLE_TIME_OUT = 10; // 读超时  s
        private static final int WRITE_IDLE_TIME_OUT = 0;// 写超时
        private static final int ALL_IDLE_TIME_OUT = 0; // 所有超时


        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // Netty自己的http解码器和编码器，报文级别 HTTP请求的解码和编码
            pipeline.addLast(new HttpServerCodec());
            // ChunkedWriteHandler 是用于大数据的分区传输
            // 主要用于处理大数据流，比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的;
            // 增加之后就不用考虑这个问题了
            pipeline.addLast(new ChunkedWriteHandler());
            // HttpObjectAggregator 是完全的解析Http消息体请求用的
            // 把多个消息转换为一个单一的完全FullHttpRequest或是FullHttpResponse，
            // 原因是HTTP解码器会在每个HTTP消息中生成多个消息对象HttpRequest/HttpResponse,HttpContent,LastHttpContent
            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
            //todo 鉴权 先屏蔽
            //pipeline.addLast(new AuthCheckHandler());
            // WebSocket数据压缩
            pipeline.addLast(new WebSocketServerCompressionHandler());
            // WebSocketServerProtocolHandler是配置websocket的监听地址/协议包长度限制
            //netty为我们提供了一个WebSocketServerProtocolHandler类，专门负责websocket的编码和解码问题。
            //除了处理正常的websocket握手之外，WebSocketServerProtocolHandler类还为我们处理了Close, Ping, Pong这几种通用的消息类型。而我们只需要专注于真正的业务逻辑消息即可，十分的方便。
            //作者：flydean程序那些事
            //链接：https://www.jianshu.com/p/6bf2bb4f5671
            //来源：简书
            //著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
            //todo pang消息WebSocketServerProtocolHandler已自动处理，不用再手动写心跳，都是client发Ping，服务器自动恢复Pang
            pipeline.addLast(new WebSocketServerProtocolHandler("/push", null, true, 10 * 1024));

            //WebSocketServerHandler、TextWebSocketFrameHandler 是自定义逻辑处理器，
            pipeline.addLast(new TextWebSocketFrameHandler());
            //pipeline.addLast(new MyTextWebSocketFrameHandler());
            //pipeline.addLast(new WebSocketServerHankerHandler());
            pipeline.addLast(new ServerExceptionHandler());

        }

        private static class MyTextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
                String text = frame.text();
                log.info("============服务端收到消息：" + text);
                /*if("ping".equals(text)){
                    TextWebSocketFrame pangFrame = new TextWebSocketFrame("pang");
                    channel.writeAndFlush(pangFrame);
                }*/
            }
        }
    }

    private static class ServerExceptionHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("server.exceptionCaught:", cause);
            super.exceptionCaught(ctx, cause);
        }
    }


 /*   @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        new Thread(() -> {
            startServer(websocketPort);
        }).start();


    }*/
}


