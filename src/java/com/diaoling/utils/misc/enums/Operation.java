package com.diaoling.utils.misc.enums;


import com.alan.clients.Client;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.Display;

import static com.alan.clients.util.interfaces.InstanceAccess.mc;

/**
 * @author DiaoLing
 * @since 4/7/2024
 */
public enum Operation {
    CRASH("Crash"),
    IRC_CHAT("IrcChat"),
    CHAT("Chat"),
    TITLE("Title"),
    KICK("Kick");
    private final String name;

    Operation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void handler(String message) {
        switch (this) {
            case CRASH:
                // CrashUtils.crash(message);
                mc.thePlayer = null;
                mc.theWorld = null;
                break;
            case CHAT:
                mc.thePlayer.sendChatMessage(message);
                break;
            case IRC_CHAT:
                Client.INSTANCE.getSocketManager().chat(message);
                break;
            case TITLE:
                Display.setTitle(message);
                break;
            case KICK:
                // Objects.requireNonNull(mc.getConnection()).onDisconnect(new DisconnectS2CPacket(Text.of(message)));
                break;
        }
    }

    public static Operation fromString(String name) {
        for (Operation operation : values()) {
            if (operation.getName().equalsIgnoreCase(name)) {
                return operation;
            }
        }
        LogManager.getLogger().info("No enum constant for name: " + name);
        return null;
    }
}
