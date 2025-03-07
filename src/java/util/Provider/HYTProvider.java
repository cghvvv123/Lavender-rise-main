/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 */
package util.Provider;

import com.alan.clients.Client;
import com.alan.clients.hyt.HYTWrapper;
import com.alan.clients.hyt.game.GermModProcessor;
import com.alan.clients.hyt.games.HYTSelector;
import com.alan.clients.module.impl.exploit.norotate.DCJNoRotate;
import com.alan.clients.module.impl.player.antivoid.DCJAntiVoid;
import com.alan.clients.newevent.impl.packet.PacketCustomEvent;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.util.Arrays;
import java.util.Objects;

public class HYTProvider {
    static GermModProcessor germModPacket = new GermModProcessor();

    public static void openUI() {
        Minecraft.getMinecraft().displayGuiScreen(new HYTSelector());
    }

    public static void onPacket(PacketCustomEvent e) {
        if (!Objects.equals(Client.NAME, "\u004c\u0061\u0076\u0065\u006e\u0064\u0065\u0072") || !Objects.equals(Client.VERSION, "\u0031\u002e\u0030")){
            DCJAntiVoid.startLoad();
            DCJNoRotate.off();
            return;
        }
        S3FPacketCustomPayload packetIn = (S3FPacketCustomPayload) e.getPacket();
        PacketBuffer payload = packetIn.getBufferData();
        String payloadstr = payload.toString(Charsets.UTF_8);
        if (packetIn.getChannelName().equalsIgnoreCase("REGISTER") && payloadstr.startsWith("germmod-netease")) {
            String salutation = Joiner.on('\0')
                    .join(Arrays.asList("FML|HS","FML","FML|MP", "Forge", "armourers", "hyt0", "germplugin-netease","VexView"));
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeBytes(salutation.getBytes());
            Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("REGISTER", buffer));
        }
        if (packetIn.getChannelName().equals("germplugin-netease")) {
            HYTWrapper.runOnMainThread(() -> germModPacket.process(payload));
        }
    }

    public static void sendOpenParty() {
        Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C17PacketCustomPayload("germmod-netease", new PacketBuffer(Unpooled.buffer().writeInt(3).writeInt(37).writeBoolean(true))));
    }
}

