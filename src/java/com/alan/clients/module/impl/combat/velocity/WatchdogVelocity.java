package com.alan.clients.module.impl.combat.velocity;

import com.alan.clients.module.impl.combat.Velocity;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public final class WatchdogVelocity extends Mode<Velocity> {

    public WatchdogVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (getParent().onSwing.getValue() || getParent().onSprint.getValue() && !InstanceAccess.mc.thePlayer.isSwingInProgress)
            return;

        final Packet<?> p = event.getPacket();

        if (p instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapper = (S12PacketEntityVelocity) p;
            if(InstanceAccess.mc.thePlayer!=null) {
                if (wrapper.getEntityID() == InstanceAccess.mc.thePlayer.getEntityId()) {
                    event.setCancelled(true);
                    InstanceAccess.mc.thePlayer.motionY = ((S12PacketEntityVelocity) p).motionY / 8000.0;
                }
            }
        } else if (p instanceof S27PacketExplosion) {
            event.setCancelled(true);
        }
    };
}
