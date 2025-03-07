package com.alan.clients.module.impl.other;


import com.alan.clients.api.Rise;
import com.alan.clients.hyt.games.HYTSelector;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.Priorities;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.SendChatMessageEvent;
import com.alan.clients.newevent.impl.packet.PacketCustomEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.SubMode;
import util.Provider.HYTProvider;

import java.awt.*;

@Rise
@ModuleInfo(name = "module.other.protocol.name", description = "Include com.alan.clients.hyt vexview and germ", category = Category.OTHER)
public class Protocol extends Module {
    public final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("QuickMacro"))
            .setDefault("QuickMacro");
    @EventLink()
    public final Listener<PacketCustomEvent> onPacketCustom = HYTProvider::onPacket;
    @EventLink()
    public final Listener<SendChatMessageEvent> onSendChatMessage = e -> {
        if (isNull()) return;
        if (e.getMsg().contains("/kh")) {
            ChatUtil.info("\u6253\u5f00\u7ec4\u961f\u9875\u9762");
            HYTProvider.sendOpenParty();
            e.setCancelled();
        }
    };
    @EventLink(value = Priorities.HIGH)
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (mc.currentScreen instanceof HYTSelector) {
            double width = event.getScaledResolution().getScaledWidth();
            double height = event.getScaledResolution().getScaledHeight();
            NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.rectangle(0, 0, width, height, Color.BLACK));
        }
    };
}

