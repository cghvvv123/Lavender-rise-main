package com.alan.clients.module.impl.other;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.movement.Flight;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;

import java.util.concurrent.LinkedBlockingQueue;

@ModuleInfo(name = "SpectatorAbuse", description = "CNM", category = Category.OTHER)
public class SpectatorAbuse extends Module {
    private final BooleanValue autofly = new BooleanValue("AutoFly", this, true);
    private S12PacketEntityVelocity velocityPacket = null;
    private final LinkedBlockingQueue<S32PacketConfirmTransaction> transactionPackets = new LinkedBlockingQueue<>();
    private boolean hasSpecialEntityVelocity = false;

    @Override
    protected void onEnable() {
        this.hasSpecialEntityVelocity = false;
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        while (!transactionPackets.isEmpty()) {
            mc.getNetHandler().handleConfirmTransaction(transactionPackets.poll());
        }
        if (velocityPacket != null) {
            mc.getNetHandler().handleEntityVelocity(velocityPacket);
        }
        super.onDisable();
    }

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        final Packet<?> packet = event.getPacket();
        if (packet instanceof S39PacketPlayerAbilities) {
            hasSpecialEntityVelocity = true;
        }
        if (hasSpecialEntityVelocity) {
            if (packet instanceof S32PacketConfirmTransaction) {
                event.setCancelled(true);
                transactionPackets.add((S32PacketConfirmTransaction) packet);
            }
            if (packet instanceof S12PacketEntityVelocity && (((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId())) {
                event.setCancelled(true);
                this.velocityPacket = (S12PacketEntityVelocity) packet;
            }
        }
    };
    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {  
        if (hasSpecialEntityVelocity && autofly.getValue()) {
            getModule(Flight.class).setEnabled(true);
        }
    };
}
