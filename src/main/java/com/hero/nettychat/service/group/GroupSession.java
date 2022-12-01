package com.hero.nettychat.service.group;

import java.nio.channels.Channel;
import java.util.List;
import java.util.Set;

public interface GroupSession {

    /**
     * 创建聊天组
     *
     * @param groupName
     * @param members
     * @return
     */
    Group createGroup(String groupName, Set<String> members);

    /**
     * 加入聊天组
     *
     * @param groupName
     * @param member
     * @return
     */
    Group JoinMember(String groupName, String member);

    /**
     * 移除 组成员
     *
     * @param groupName
     * @param member
     * @return
     */
    Group removeMember(String groupName, String member);

    /**
     * 移除聊天组
     *
     * @param groupName
     * @return
     */
    Group removeGroup(String groupName);

    /**
     * 获取组成员
     *
     * @param groupName
     * @return
     */
    Set<String> getMembers(String groupName);

    /**
     * 获取聊天组成员的channel 在线的
     *
     * @param groupName
     * @return
     */
    List<Channel> getMembersChannel(String groupName);
}
