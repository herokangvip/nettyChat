package com.hero.nettychat.service;

import java.util.concurrent.ConcurrentHashMap;

public class LoginService {

    private static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();

    static {
        map.put("zhangsan", "123");
        map.put("lisi", "123");
        map.put("wangwu", "123");
        map.put("zhaoliu", "123");
    }

    public boolean login(String name, String password) {
        return map.containsKey(name);
    }
}
