package com.alan.clients.module.impl.movement;

import com.alan.clients.Client;
import com.alan.clients.Type;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.Priorities;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.impl.BooleanValue;

/**
 * @author Auth
 * @since 20/10/2021
 */
@ModuleInfo(name = "module.movement.sprint.name", description = "module.movement.sprint.description", category = Category.MOVEMENT)
public class Sprint extends Module {
    private final BooleanValue legit = new BooleanValue("Legit", this, true, () -> Client.CLIENT_TYPE != Type.RISE);

    private final BooleanValue huayutingBypass = new BooleanValue("HuayutingBypass", this, false);


    @EventLink(value = Priorities.LOW)
    public final Listener<StrafeEvent> onStrafe = event -> {


        InstanceAccess.mc.gameSettings.keyBindSprint.setPressed(!huayutingBypass.getValue() || (InstanceAccess.mc.gameSettings.keyBindForward.isKeyDown() && !InstanceAccess.mc.thePlayer.isCollidedHorizontally));

        if (Client.CLIENT_TYPE != Type.RISE) return;
        if (InstanceAccess.mc.thePlayer.omniSprint && MoveUtil.isMoving() && !legit.getValue()) {
            InstanceAccess.mc.thePlayer.setSprinting(true);
        }

        InstanceAccess.mc.thePlayer.omniSprint = !legit.getValue() && MoveUtil.isMoving() && !InstanceAccess.mc.thePlayer.isCollidedHorizontally &&
                !InstanceAccess.mc.thePlayer.isSneaking() && !InstanceAccess.mc.thePlayer.isUsingItem();
    };

    @Override
    public void onDisable() {
        InstanceAccess.mc.thePlayer.setSprinting(InstanceAccess.mc.gameSettings.keyBindSprint.isKeyDown());
        InstanceAccess.mc.thePlayer.omniSprint = false;
    }
}
