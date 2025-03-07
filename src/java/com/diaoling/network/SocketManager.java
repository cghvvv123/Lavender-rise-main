package com.diaoling.network;

import com.diaoling.network.client.SocketClient;
import com.diaoling.network.packet.Packet;
import com.diaoling.network.packet.impl.message.ChatMessagePacket;
import com.diaoling.network.packet.impl.operation.OperationPacket;
import com.diaoling.network.user.UserManager;
import com.diaoling.utils.misc.enums.ChannelType;
import com.diaoling.utils.misc.enums.Operation;

/**
 * @author DiaoLing
 * @since 4/8/2024
 */
public class SocketManager {
    private final SocketClient client = new SocketClient();

    private static String prefix = "!";

    public SocketClient getClient() {
        return client;
    }

    public String getPrefix() {
        return prefix;
    }

    public void send(Packet packet) {
        client.send(packet);
    }

    // 我去发你
    public void chat(String message) {
        this.send(new ChatMessagePacket(
                ChannelType.GLOBAL,
                message,
                System.currentTimeMillis()));
    }

    public void operation(Operation operation, String targetUsername, String message) {
        this.send(new OperationPacket(
                UserManager.getUser().getUsername(),
                targetUsername,
                message,
                operation
        ));
    }
}
