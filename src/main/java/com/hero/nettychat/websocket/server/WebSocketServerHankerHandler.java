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
            // websocketè¿æ¥è¯·æ±
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            // websocketä¸å¡å¤ç
            handleWebSocketRequest(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * è·åWebSocketæå¡ä¿¡æ¯
     *
     * @param req
     * @return
     */
    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get("Host") + "/ws";
        return "ws://" + location;
    }

    /**
     * æ¥æ¶æ¡æè¯·æ±ï¼å¹¶ååº
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
            // ä¸æ¯æwebsocket
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // è·åè¯·æ±åæ°
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> parameters = decoder.parameters();

            //String userid = parameters.get("userid").get(0);
            // éè¿å®æé æ¡æååºæ¶æ¯è¿åç»å®¢æ·ç«¯
            ChannelFuture future = handshaker.handshake(ctx.channel(), request);
            if (future.isSuccess()) {
                String msg = "å®¢æ·ç«¯" + ctx.channel() + "å å¥èå¤©å®¤";
                ctx.channel().writeAndFlush(new TextWebSocketFrame(msg));
            }
        }
    }

    /**
     * æ¥æ¶WebSocketè¯·æ±
     *
     * @param ctx
     * @param req
     * @throws Exception
     */
    private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame req) throws Exception {
        if (req instanceof CloseWebSocketFrame) {//å³é­socketè¿æ¥è¯·æ±
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) req.retain());
            return;
        }
        if (req instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(req.content().retain()));
            return;
        }
        if (req instanceof BinaryWebSocketFrame) {
            throw new UnsupportedOperationException("å½ååªæ¯æææ¬æ¶æ¯ï¼ä¸æ¯æäºè¿å¶æ¶æ¯");
        }
        if (req instanceof TextWebSocketFrame) {
            ctx.channel().write(new TextWebSocketFrame(((TextWebSocketFrame) req).text()));
        }
        if (ctx == null || this.handshaker == null || ctx.isRemoved()) {
            throw new Exception("å°æªæ¡ææåï¼æ æ³åå®¢æ·ç«¯åéWebSocketæ¶æ¯");
        }
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // BAD_REQUEST(400) å®¢æ·ç«¯è¯·æ±éè¯¯è¿åçåºç­æ¶æ¯
        if (res.status().code() != 200) {
            // å°è¿åçç¶æç æ¾å¥ç¼å­ä¸­ï¼Unpooledæ²¡æä½¿ç¨ç¼å­æ± 
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // åéåºç­æ¶æ¯
        ChannelFuture cf = ctx.channel().writeAndFlush(res);
        // éæ³è¿æ¥ç´æ¥å³é­è¿æ¥
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            cf.addListener(ChannelFutureListener.CLOSE);
        }
    }
}

