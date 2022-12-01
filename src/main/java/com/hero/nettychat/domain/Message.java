package com.hero.nettychat.domain;

import java.io.Serializable;

public abstract class Message implements Serializable {


    private long seqId;
    private int messageType;

    public abstract int getMessageType();

    public long getSeqId() {
        return seqId;
    }

    public void setSeqId(long seqId) {
        this.seqId = seqId;
    }
}
