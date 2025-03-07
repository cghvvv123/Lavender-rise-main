package com.alan.clients.module.impl.movement;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.JumpEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.util.rotation.RotationUtil;
import com.alan.clients.util.vector.Vector3d;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/**
 * @author Alan
 * @since 20/10/2021
 */
@Rise
@ModuleInfo(name = "module.movement.targetstrafe.name", description = "module.movement.targetstrafe.description", category = Category.MOVEMENT)
public class TargetStrafe extends Module {

    private final NumberValue range = new NumberValue("Range", this, 1, 0.2, 6, 0.1);

    public final BooleanValue holdJump = new BooleanValue("Hold Jump", this, true);
    private float yaw;
    private Entity target;
    private boolean left, colliding;
    private boolean active;

    @EventLink()
    public final Listener<JumpEvent> onJump = event -> {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    };

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    };
    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        // Disable if scaffold is enabled
        KillAura killAura = getModule(KillAura.class);
        target = getModule(KillAura.class).target;
        if (killAura == null || !killAura.isEnabled()) {
            active = false;
            return;
        }

        active = true;
        
        /*
         * Getting targets and selecting the nearest one
         */


        if (holdJump.getValue() && !InstanceAccess.mc.gameSettings.keyBindJump.isKeyDown() || !(InstanceAccess.mc.gameSettings.keyBindForward.isKeyDown())) {
            target = null;
            return;
        }


        if (InstanceAccess.mc.thePlayer.isCollidedHorizontally || !PlayerUtil.isBlockUnder(5, false)) {
            if (!colliding) {
                MoveUtil.strafe();
                left = !left;
            }
            colliding = true;
        } else {
            colliding = false;
        }

        if (target == null) {
            return;
        }
        if(target.getDistanceToEntity(InstanceAccess.mc.thePlayer) <= range.getValue().intValue()) {

            float yaw = RotationUtil.calculate(target).getX() + (90 + 45) * (left ? -1 : 1);

            final double range = this.range.getValue().doubleValue();
            final double posX = -MathHelper.sin((float) Math.toRadians(yaw)) * range + target.posX;
            final double posZ = MathHelper.cos((float) Math.toRadians(yaw)) * range + target.posZ;

            yaw = RotationUtil.calculate(new Vector3d(posX, target.posY, posZ)).getX();

            this.yaw = yaw;
            InstanceAccess.mc.thePlayer.movementYaw = this.yaw;
        }
    };
}
