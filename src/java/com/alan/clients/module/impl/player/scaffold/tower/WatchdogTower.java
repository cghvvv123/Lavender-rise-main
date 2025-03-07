package com.alan.clients.module.impl.player.scaffold.tower;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;

public class WatchdogTower extends Mode<Scaffold> {
    int towerTick = 0;
    public WatchdogTower(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (InstanceAccess.mc.gameSettings.keyBindJump.isKeyDown()) {
            if (MoveUtil.isMoving()) {
                towerTick++;
                if (InstanceAccess.mc.thePlayer.onGround) {
                    towerTick = 0;
                }
                InstanceAccess.mc.thePlayer.motionY = 0.41965;
                InstanceAccess.mc.thePlayer.motionX = Math.min(InstanceAccess.mc.thePlayer.motionX, 0.265);
                InstanceAccess.mc.thePlayer.motionZ = Math.min(InstanceAccess.mc.thePlayer.motionZ, 0.265);
                if (towerTick == 1) {
                    InstanceAccess.mc.thePlayer.motionY = 0.33;
                } else if (towerTick == 2) {
                    InstanceAccess.mc.thePlayer.motionY = 1 - InstanceAccess.mc.thePlayer.posY % 1;
                } else if (towerTick >= 3) {
                    towerTick = 0;
                }
            }
        } else {
            towerTick = 0;
        }
    };
}
