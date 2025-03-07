package com.alan.clients.network;

import com.alan.clients.util.interfaces.InstanceAccess;
import lombok.Getter;
import lombok.Setter;

@Getter

public final class NetworkManager implements InstanceAccess {
    @Setter
    public String username;
    public String message;

    public void init(String username) {
        this.username = username;
    }

    public static boolean a() {
        return true;
    }
}
