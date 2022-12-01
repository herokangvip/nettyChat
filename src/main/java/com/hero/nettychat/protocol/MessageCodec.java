package com.hero.nettychat.protocol;

import com.hero.nettychat.domain.LoginMessage;
import com.hero.nettychat.domain.Message;
import com.hero.nettychat.util.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 编解码
 */
public class MessageCodec extends ByteToMessageCodec<Message> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message msg, ByteBuf byteBuf) throws Exception {
        //4个字节 魔数
        byteBuf.writeBytes(new byte[]{6, 6, 6, 6});
        //4个字节 版本
        byteBuf.writeInt(1);
        //4个字节 序列化算法（0：json）
        byteBuf.writeInt(0);
        //4个字节 消息类型
        byteBuf.writeInt(msg.getMessageType());
        //8个字节 请求序号
        byteBuf.writeLong(msg.getSeqId());
        byte[] bytes = JsonUtils.toJsonString(msg).getBytes(StandardCharsets.UTF_8);
        //4个字节 长度
        byteBuf.writeInt(bytes.length);
        //内容
        byteBuf.writeBytes(bytes);

    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        // todo herokang 由于前一个处理器是LengthFieldBasedFrameDecoder，已经处理完粘包半包问题，in将是完整消息
        int magic = in.readInt();
        int version = in.readInt();
        int serialize = in.readInt();
        int messageType = in.readInt();
        long seqId = in.readLong();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);
        String s = new String(bytes, StandardCharsets.UTF_8);
        // todo herokang
        list.add(JsonUtils.parseObject(s, LoginMessage.class));
        System.out.println("========decode:"+s);
    }

}
