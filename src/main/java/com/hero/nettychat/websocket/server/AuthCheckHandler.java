package com.hero.nettychat.websocket.server;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AuthCheckHandler extends ChannelInboundHandlerAdapter {

    public static final AttributeKey SECURITY_CHECK_COMPLETE_ATTRIBUTE_KEY =
            AttributeKey.valueOf("SECURITY_CHECK_COMPLETE_ATTRIBUTE_KEY");
/*

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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
        //other protocols
        super.channelRead(ctx, msg);
    }
*/

}

