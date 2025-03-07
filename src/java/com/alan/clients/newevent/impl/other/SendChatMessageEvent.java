package com.alan.clients.newevent.impl.other;

import com.alan.clients.newevent.CancellableEvent;

public class SendChatMessageEvent extends CancellableEvent {
    String msg;

    public SendChatMessageEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }
}
