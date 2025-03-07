package com.alan.clients.newevent.impl.input;

import com.alan.clients.newevent.Event;

public class KeyEvent implements Event {
    private int key;

    public KeyEvent(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
