package com.alan.clients.module.impl.movement.longjump;

import com.alan.clients.component.impl.player.BlinkComponent;
import com.alan.clients.component.impl.render.NotificationComponent;
import com.alan.clients.module.impl.movement.LongJump;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.DamageUtil;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author Auth
 * @since 18/11/2021
 */

public class WatchdogLongJump extends Mode<LongJump> {
    private int ticks = 0;

    public WatchdogLongJump(String name, LongJump parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        ticks = 0;
    }

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C03PacketPlayer && ticks <= 25) {
            event.setCancelled();
        }
    };

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (ticks <= 25)
            event.setCancelled();
    };

    @EventLink
    public final Listener<PreUpdateEvent> preUpdate = event -> {
        ticks++;
        if (ticks == 25) {
            DamageUtil.damagePlayer(0.5);
        } else if (ticks > 25 && ticks < 35 && InstanceAccess.mc.thePlayer.onGround) {
            InstanceAccess.mc.thePlayer.jump();
        } else if (ticks == 35) {
            BlinkComponent.blinking = true;
            MoveUtil.strafe(1);
            InstanceAccess.mc.thePlayer.jump();
        } else if (ticks > 35 && InstanceAccess.mc.thePlayer.onGround) {
            getParent().toggle();
            BlinkComponent.dispatch();
            BlinkComponent.blinking = false;
            NotificationComponent.post("Watchdog long jump", "After the jump.");
            this.ticks = 0;
        }
    };
}
