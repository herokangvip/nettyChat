package com.hero.nettychat.websocket.server;

import com.hero.nettychat.websocket.client.WebSocketMockClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import org.thymeleaf.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebSocketServerHankerHandler extends ChannelInboundHandlerAdapter {
    private WebSocketServerHandshaker handshaker;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            // websocket连接请求
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            // websocket业务处理
            handleWebSocketRequest(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 获取WebSocket服务信息
     *
     * @param req
     * @return
     */
    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get("Host") + "/ws";
        return "ws://" + location;
    }

    /**
     * 接收握手请求，并响应
     *
     * @param ctx
     * @param request
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess() || !"websocket".equals(request.headers().get("Upgrade"))) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, false);
        handshaker = handshakerFactory.newHandshaker(request);
        if (handshaker == null) {
            // 不支持websocket
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // 获取请求参数
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> parameters = decoder.parameters();

            //String userid = parameters.get("userid").get(0);
            // 通过它构造握手响应消息返回给客户端
            ChannelFuture future = handshaker.handshake(ctx.channel(), request);
            if (future.isSuccess()) {
                String msg = "客户端" + ctx.channel() + "加入聊天室";
                ctx.channel().writeAndFlush(new TextWebSocketFrame(msg));
            }
        }
    }

    /**
     * 接收WebSocket请求
     *
     * @param ctx
     * @param req
     * @throws Exception
     */
    private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame req) throws Exception {
        if (req instanceof CloseWebSocketFrame) {//关闭socket连接请求
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) req.retain());
            return;
        }
        if (req instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(req.content().retain()));
            return;
        }
        if (req instanceof BinaryWebSocketFrame) {
            throw new UnsupportedOperationException("当前只支持文本消息，不支持二进制消息");
        }
        if (req instanceof TextWebSocketFrame) {
            ctx.channel().write(new TextWebSocketFrame(((TextWebSocketFrame) req).text()));
        }
        if (ctx == null || this.handshaker == null || ctx.isRemoved()) {
            throw new Exception("尚未握手成功，无法向客户端发送WebSocket消息");
        }
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // BAD_REQUEST(400) 客户端请求错误返回的应答消息
        if (res.status().code() != 200) {
            // 将返回的状态码放入缓存中，Unpooled没有使用缓存池
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // 发送应答消息
        ChannelFuture cf = ctx.channel().writeAndFlush(res);
        // 非法连接直接关闭连接
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            cf.addListener(ChannelFutureListener.CLOSE);
        }
    }
}

