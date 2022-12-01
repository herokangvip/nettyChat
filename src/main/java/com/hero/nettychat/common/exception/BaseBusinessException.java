package com.hero.nettychat.common.exception;

/**
 * ParamException
 *
 * @author sandykang
 **/
public class BaseBusinessException extends RuntimeException {

    private static final long serialVersionUID = -1L;

    private int code;

    private String desc;

    public BaseBusinessException(int code, String desc) {
        super();
        this.code = code;
        this.desc = desc;
    }

    public BaseBusinessException(int code, String desc, Throwable cause) {
        super(cause);
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

