package com.hero.nettychat.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSignerUtil;

/**
 * @author
 */
public class JwtUtils {

    private static final String APP_SECRET = "ERcoohpGCIrDY";
    private static final Integer EXPIRE = 7;

    /**
     * 获取默认过期时间的jwt签名器
     * @return
     */
    public static JWT getJwt(){
        return getJwt(EXPIRE);
    }

    /**
     * 获取自定义过期时间的jwt签名器
     * @param expire
     * @return
     */
    public static JWT getJwt(Integer expire){
        return getJWT().setExpiresAt(DateUtil.offsetDay(DateUtil.date(), expire));
    }

    /**
     * 生成jwt签名器
     * @return
     */
    private static JWT getJWT(){
        return JWT.create().setSigner(JWTSignerUtil.hs256(APP_SECRET.getBytes()));
    }

    /**
     * 根据token生成jwt
     * @param token
     * @return
     */
    public static JWT parse(String token){
        return getJWT().parse(token);
    }
}
