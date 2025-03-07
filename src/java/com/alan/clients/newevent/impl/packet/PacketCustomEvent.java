package com.alan.clients.newevent.impl.packet;

import com.alan.clients.newevent.Event;
import net.minecraft.network.Packet;

public class PacketCustomEvent implements Event {
    Packet packet;

    public PacketCustomEvent(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return this.packet;
    }
}
