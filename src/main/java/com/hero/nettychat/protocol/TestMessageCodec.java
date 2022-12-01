package com.hero.nettychat.protocol;

import com.hero.nettychat.domain.LoginMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                //int maxFrameLength, int lengthFieldOffset, int lengthFieldLength
                new LengthFieldBasedFrameDecoder(1024, 24, 4),
                new LoggingHandler(),//打印调试日志
                new MessageCodec()
        );
        LoginMessage loginMessage = new LoginMessage();
        loginMessage.setUserId("111");
        loginMessage.setUserName("jim");
        loginMessage.setPassword("pass");
        loginMessage.setSeqId(1L);

        //测试 encode
        //channel.writeOutbound(loginMessage);


        //测试有消息进来 解码decode
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, loginMessage, buffer);
        channel.writeInbound(buffer);
    }
}
