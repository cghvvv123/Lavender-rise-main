package com.alan.clients.module.impl.other;

import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.domcer.CustomPacket;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.Priorities;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.SubMode;
import com.google.common.base.Joiner;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Rise
@ModuleInfo(name = "module.other.domcerpatcher.name", category = Category.OTHER, description = "module.other.domcerpatcher.description")
public class DomcerPatcher extends Module {
    public final ModeValue screenShotMode = new ModeValue("ScreenShot", this)
            .add(new SubMode("Local Pic"))
            .add(new SubMode("Runtime Pic"))
            .setDefault("Runtime Pic");


    @EventLink(Priorities.VERY_HIGH)
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        Packet packet = event.getPacket();

        if (packet instanceof S3FPacketCustomPayload)
        {
            if (this.isEnabled())
            {
                S3FPacketCustomPayload packetIn = (S3FPacketCustomPayload) packet;

                if (packetIn.getChannelName().equalsIgnoreCase("REGISTER")) {

                    String salutation = Joiner.on('\0')
                            .join(Arrays.asList("FML|HS","FML","FML|MP", "Forge", "CustomSkinLoader", "UView"));
                    C17PacketCustomPayload proxy = new C17PacketCustomPayload("REGISTER",
                            new PacketBuffer(Unpooled.wrappedBuffer((salutation + "\0" + new String(packetIn.getBufferData().array())).getBytes(StandardCharsets.UTF_8))));

                    System.out.println("Server Have: " + Arrays.toString(new String(packetIn.getBufferData().array()).split("\0")));

                    InstanceAccess.mc.getNetHandler().addToSendQueue(proxy);

                }

                for (CustomPacket customPacket : Client.INSTANCE.getDomcerPacketManager().packets) {
                    if (customPacket.getChannel().equals(packetIn.getChannelName())) {
                        customPacket.process(packetIn.getBufferData());
                        return;
                    }
                }
            }

        }

    };







}
