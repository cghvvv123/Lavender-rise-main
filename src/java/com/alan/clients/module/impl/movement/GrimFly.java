package com.alan.clients.module.impl.movement;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PostMotionEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;

@ModuleInfo(name = "GrimFly",category = Category.MOVEMENT,description = "By Xiaoc")
public class GrimFly extends Module {

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
            mc.thePlayer.setPositionAndRotation(
                    mc.thePlayer.posX + 1000,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch
            );
    };
    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
            mc.thePlayer.setPositionAndRotation(
                    mc.thePlayer.posX - 1000,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch
            );
    };
}
