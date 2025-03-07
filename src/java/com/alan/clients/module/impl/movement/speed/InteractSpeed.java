package com.alan.clients.module.impl.movement.speed;

import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

/**
 * @author Auth
 * @since 4/07/2022
 */

public class InteractSpeed extends Mode<Speed> {

    private final NumberValue speed = new NumberValue("Speed", this, 1, 1, 10, 1);

    public InteractSpeed(String name, Speed parent) {
        super(name, parent);
    }


    @EventLink()
    public final Listener<TickEvent> onTick = event -> {

        final int speed = this.speed.getValue().intValue();
        if (MoveUtil.isMoving()) {
            for (int i = 0; i < speed; i++) {
                PacketUtil.send(new C08PacketPlayerBlockPlacement(InstanceAccess.mc.thePlayer.inventory.getStackInSlot(InstanceAccess.mc.thePlayer.inventory.currentItem)));
                PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(InstanceAccess.mc.thePlayer.posX, InstanceAccess.mc.thePlayer.posY, InstanceAccess.mc.thePlayer.posZ, InstanceAccess.mc.thePlayer.onGround));

                final double posX = InstanceAccess.mc.thePlayer.posX;
                final double posY = InstanceAccess.mc.thePlayer.posY;
                final double posZ = InstanceAccess.mc.thePlayer.posZ;

                InstanceAccess.mc.thePlayer.onLivingUpdate();

                InstanceAccess.mc.thePlayer.posX = posX;
                InstanceAccess.mc.thePlayer.posY = posY;
                InstanceAccess.mc.thePlayer.posZ = posZ;
            }
        }
    };
}
