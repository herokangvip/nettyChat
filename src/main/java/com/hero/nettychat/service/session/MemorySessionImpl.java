package com.hero.nettychat.service.session;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class MemorySessionImpl implements Session {
    private static ConcurrentHashMap<Channel, String> channelMap = new ConcurrentHashMap<Channel, String>();
    private static ConcurrentHashMap<String, Channel> userMap = new ConcurrentHashMap<String, Channel>();


    @Override
    public void bind(Channel channel, String userName) {
        channelMap.put(channel, userName);
        userMap.put(userName, channel);
    }

    @Override
    public void unbind(Channel channel) {
        String userName = channelMap.remove(channel);
        userMap.remove(userName);
    }

    @Override
    public Channel getChannel(String userName) {
        return userMap.get(userName);
    }
}
