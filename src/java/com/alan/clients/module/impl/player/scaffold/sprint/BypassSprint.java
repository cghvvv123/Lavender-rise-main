package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;

public class BypassSprint extends Mode<Scaffold> {

    public BypassSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (MoveUtil.isMoving() && InstanceAccess.mc.thePlayer.isSprinting() && InstanceAccess.mc.thePlayer.onGround) {
            final double speed = MoveUtil.WALK_SPEED;
            final float yaw = (float) MoveUtil.direction();
            final double posX = MathHelper.sin(yaw) * speed + InstanceAccess.mc.thePlayer.posX;
            final double posZ = -MathHelper.cos(yaw) * speed + InstanceAccess.mc.thePlayer.posZ;
            PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(posX, event.getPosY(), posZ, false));
        }
//        mc.thePlayer.setSprinting(false);
    };
}
