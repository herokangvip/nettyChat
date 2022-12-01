package com.hero.nettychat.common.exception;

/**
 * ParamException
 *
 * @author sandykang
 **/
public class ParamException extends BaseBusinessException {


    private static final long serialVersionUID = -1L;

    public ParamException(int code, String desc) {
        super(code, desc);
    }

    public ParamException(int code, String desc, Throwable cause) {
        super(code, desc, cause);
    }
}

