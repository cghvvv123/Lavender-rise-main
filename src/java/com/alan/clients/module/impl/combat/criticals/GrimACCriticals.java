package com.alan.clients.module.impl.combat.criticals;

import com.alan.clients.module.Module;
import com.alan.clients.module.impl.combat.Criticals;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.module.impl.player.Blink;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.other.AttackEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.NumberValue;

public class GrimACCriticals extends Mode<Criticals> {
    private final NumberValue timervalue = new NumberValue("TimerValue", this, 1F, 0.1F, 1F,0.1);
    private int blinktick = 0;
    private int jump = 0;
    private int airtick = 0;
    private boolean dotimercri = false;
    public GrimACCriticals(String name, Criticals parent) {super(name, parent);}
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (isNull()) return;
        Module blink = getModule(Blink.class);
        if (!InstanceAccess.mc.thePlayer.onGround) {
            airtick ++;
        } else {
            airtick = 0;
        }
        if (blink.isEnabled()) {
            blinktick = 100;
        } else blinktick --;
        if (dotimercri && getModule(KillAura.class).isEnabled()) {
            if (airtick == 1) {
                InstanceAccess.mc.gameSettings.keyBindJump.pressed = false;
            }
            if ((airtick >= 6 && airtick <= 10) && !InstanceAccess.mc.thePlayer.onGround) {
                InstanceAccess.mc.timer.timerSpeed = timervalue.getValue().floatValue();
            }
            if (airtick > 10 || airtick == 0 || InstanceAccess.mc.thePlayer.onGround) {
                InstanceAccess.mc.timer.timerSpeed = 1F;
                dotimercri = false;
            }
        }

        jump ++;
    };

    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        if (isNull()) return;
        Module blink = getModule(Blink.class);
        if (!blink.isEnabled() && blinktick <=0 && getModule(KillAura.class).isEnabled()) {
            if (InstanceAccess.mc.thePlayer.onGround && jump > 10 && airtick == 0 && !dotimercri && InstanceAccess.mc.thePlayer.hurtTime == 0) {
                InstanceAccess.mc.gameSettings.keyBindJump.pressed = true;
                jump = 0;
                airtick = 0;
                dotimercri = true;
                InstanceAccess.mc.timer.timerSpeed = 2f - timervalue.getValue().floatValue();
            }
        } else {
            if (InstanceAccess.mc.thePlayer.onGround && jump > 10) {
                InstanceAccess.mc.thePlayer.motionY = 0.41999998688698;
                jump = 0;
            }
        }
    };
}
