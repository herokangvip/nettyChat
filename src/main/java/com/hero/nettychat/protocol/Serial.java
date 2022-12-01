package com.hero.nettychat.protocol;

public interface Serial {
    <T> T encode();

    <T> T decode();

    enum Ath implements Serial {
        Java {
            @Override
            public <T> T encode() {
                return null;
            }

            @Override
            public <T> T decode() {
                return null;
            }
        }
    }
}
