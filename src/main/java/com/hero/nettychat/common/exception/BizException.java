package com.hero.nettychat.common.exception;

/**
 * ParamException
 *
 * @author sandykang
 **/
public class BizException extends BaseBusinessException {

    private static final long serialVersionUID = -1L;

    public BizException(int code, String desc) {
        super(code, desc);
    }

    public BizException(int code, String desc, Throwable cause) {
        super(code, desc, cause);
    }
}

