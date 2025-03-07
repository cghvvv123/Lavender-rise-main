package com.alan.clients.module.impl.combat.velocity;

import com.alan.clients.module.impl.combat.Velocity;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.other.AttackEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.Mode;
import net.minecraft.util.MovingObjectPosition;

public final class IntaveVelocity extends Mode<Velocity> {

    private boolean attacked;

    public IntaveVelocity(String name, Velocity parent) {
        super(name, parent);
    }


    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (getParent().onSwing.getValue() || getParent().onSprint.getValue() && !InstanceAccess.mc.thePlayer.isSwingInProgress) return;

        if (InstanceAccess.mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY) && InstanceAccess.mc.thePlayer.hurtTime > 0 && !attacked) {
            InstanceAccess.mc.thePlayer.motionX *= 0.6D;
            InstanceAccess.mc.thePlayer.motionZ *= 0.6D;
            InstanceAccess.mc.thePlayer.setSprinting(false);
        }

        attacked = false;
    };

    @EventLink()
    public final Listener<AttackEvent> onAttack = event -> {
        attacked = true;
    };
}
