package com.alan.clients.module.impl.other;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import net.minecraft.client.settings.GameSettings;

@Rise
@ModuleInfo(name = "module.other.antiafk.name", description = "module.other.antiafk.description", category = Category.OTHER)
public final class AntiAFK extends Module {

    private int lastInput;

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        GameSettings gameSettings = InstanceAccess.mc.gameSettings;
        if (gameSettings.keyBindJump.isKeyDown() ||
                gameSettings.keyBindRight.isKeyDown() ||
                gameSettings.keyBindForward.isKeyDown() ||
                gameSettings.keyBindLeft.isKeyDown() ||
                gameSettings.keyBindBack.isKeyDown()) {
            lastInput = 0;
        }

        lastInput++;

        if (lastInput < 20 * 10) return;

        if (InstanceAccess.mc.thePlayer.ticksExisted % 5 == 0) {
            InstanceAccess.mc.gameSettings.keyBindRight.setPressed(false);
            InstanceAccess.mc.gameSettings.keyBindLeft.setPressed(false);
            InstanceAccess.mc.gameSettings.keyBindJump.setPressed(false);
        }

        if (InstanceAccess.mc.thePlayer.ticksExisted % 20 == 0) {
            if (InstanceAccess.mc.thePlayer.ticksExisted % 40 == 0) {
                InstanceAccess.mc.gameSettings.keyBindRight.setPressed(true);
            } else {
                InstanceAccess.mc.gameSettings.keyBindLeft.setPressed(true);
            }
        }

        if (InstanceAccess.mc.thePlayer.ticksExisted % 100 == 0) {
            InstanceAccess.mc.gameSettings.keyBindJump.setPressed(true);
        }
    };
}
