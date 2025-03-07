package com.alan.clients.component.impl.patches;

import com.alan.clients.component.Component;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import net.minecraft.network.Packet;

public class S35Component extends Component {
    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceive = event ->{
        Packet packet = event.getPacket();

        if (packet == null)
        {
            event.setCancelled(true);
        }
    };
}
