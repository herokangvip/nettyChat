package com.hero.nettychat.common.exception;

public class JsonException extends BaseBusinessException {
    private static final long serialVersionUID = -1L;

    public JsonException(int code, String desc) {
        super(code, desc);
    }

    public JsonException(int code, String desc, Throwable cause) {
        super(code, desc, cause);
    }
}
