package com.hero.nettychat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
public class LoginMessage extends Message {

    private String userId;
    private String userName;
    private String password;

    @Override
    public int getMessageType() {
        return MessageType.LoginRequestMessage;
    }
}
