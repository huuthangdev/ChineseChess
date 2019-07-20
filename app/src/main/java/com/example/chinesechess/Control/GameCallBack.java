package com.example.chinesechess.Control;


public interface GameCallBack {
    void postPlaySound(int soundIndex);

    void postShowMessage(String message);

    void postShowMessage(int messageId);

    void postStartThink();

    void postEndThink();
}
