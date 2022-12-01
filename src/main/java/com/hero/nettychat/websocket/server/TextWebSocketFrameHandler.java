package com.hero.nettychat.websocket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Created
 * <p>
 * WebSocket 帧：WebSocket 以帧的方式传输数据，每一帧代表消息的一部分。一个完整的消息可能会包含许多帧
 */
@Slf4j
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //增加消息的引用计数（保留消息），并将他写到 ChannelGroup 中所有已经连接的客户端
        /*ServerSession session = ServerSession.getSession(ctx);
        String result = RpcProcesser.inst().onMessage(msg.text(), session);

        if (result != null) {
            SessionMap.getSingleton().sendMsg(ctx, result);

        }*/
        //ctx.fireChannelRead(msg);
        String text = msg.text();
        log.info("============服务端收到消息：" + text);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //是否握手成功，升级为 Websocket 协议
        //if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            //包含 String requestUri;  HttpHeaders requestHeaders;
            WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete =
                    (WebSocketServerProtocolHandler.HandshakeComplete)evt;
            // 握手成功，移除 HttpRequestHandler，因此将不会接收到任何消息
            // 并把握手成功的 Channel 加入到 ChannelGroup 中
            //doAuth(....)
            // todo herokang 保存channel，保存用户机器关系
            log.info("=====握手成功====，client："+ctx.channel());
        } else if (evt instanceof IdleStateEvent) {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            if (stateEvent.state() == IdleState.READER_IDLE) {
                //ServerSession session = ServerSession.getSession(ctx);
                //String ack = RpcProcesser.inst().onIdleTooLong(session);
                //SessionMap.getSingleton().closeSessionAfterAck(session, ack);
            }
            ctx.fireUserEventTriggered(evt);
        }
    }
    /*public void doAuth(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpMessage) {
            //extracts token information  from headers
            HttpHeaders headers = ((FullHttpMessage) msg).headers();
            String token = Objects.requireNonNull(headers.get(SessionConstants.AUTHORIZATION_HEAD));
            //extracts account information  from headers
            String account = Objects.requireNonNull(headers.get(SessionConstants.APP_ACCOUNT));

            if (null == token || null == account) {
                // 参数校验、设置响应
                String content = "请登陆之后，再发起websocket连接！！！";
                closeUnauthChannelAfterWrite(ctx, content);
                return;

            }
            Payload<String> payload = null;
            // 在此处获取URL、Headers等信息并做校验，通过throw异常来中断链接。
            try {
                payload = AuthUtils.decodeRsaToken(token);
            } catch (Exception e) {
                // 解码异常、设置响应
                String content = "请登陆之后，再发起websocket连接！！！";
                closeUnauthChannelAfterWrite(ctx, content);
                return;
            }
            if (null == payload) {
                // 解码异常、设置响应
                String content = "请登陆之后，再发起websocket连接！！！";
                closeUnauthChannelAfterWrite(ctx, content);
                return;

            }
            String appId = payload.getId();
            SecurityCheckCompleteEvent complete = new SecurityCheckCompleteEvent(token,appId, account);
            ctx.channel().attr(SECURITY_CHECK_COMPLETE_ATTRIBUTE_KEY).set(complete);
            ctx.fireUserEventTriggered(complete);
        }
    }*/

}


