package com.alan.clients.module.impl.player.scaffold.tower;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.value.Mode;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public class    NCPTower extends Mode<Scaffold> {

    public NCPTower(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (InstanceAccess.mc.gameSettings.keyBindJump.isKeyDown() && PlayerUtil.blockNear(2)) {
            PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(null));

//            mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;

            if (InstanceAccess.mc.thePlayer.posY % 1 <= 0.00153598) {
                InstanceAccess.mc.thePlayer.setPosition(InstanceAccess.mc.thePlayer.posX, Math.floor(InstanceAccess.mc.thePlayer.posY), InstanceAccess.mc.thePlayer.posZ);
                InstanceAccess.mc.thePlayer.motionY = 0.42F;
            } else if (InstanceAccess.mc.thePlayer.posY % 1 < 0.1 && InstanceAccess.mc.thePlayer.offGroundTicks != 0) {
                InstanceAccess.mc.thePlayer.motionY = 0;
                InstanceAccess.mc.thePlayer.setPosition(InstanceAccess.mc.thePlayer.posX, Math.floor(InstanceAccess.mc.thePlayer.posY), InstanceAccess.mc.thePlayer.posZ);
            }
        }
    };
}
