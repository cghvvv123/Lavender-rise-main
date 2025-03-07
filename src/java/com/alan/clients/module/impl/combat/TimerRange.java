package com.alan.clients.module.impl.combat;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.other.AttackEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.noti.NotificationManager;
import com.alan.clients.noti.NotificationType;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.NumberValue;
import com.alan.clients.value.impl.SubMode;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
@ModuleInfo(name = "TimerRange",category = Category.COMBAT,description = "CNM")
public class TimerRange extends Module {
    private int playerTicks = 0;
    private int smartCounter = 0;
    private boolean confirmAttack = false;
    private boolean confirmLagBack = false;
    private final ModeValue timerBoostMode = new ModeValue("TimerMod",this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Smart"))
            .setDefault("Smart");
    private final NumberValue ticksValue = new NumberValue("Ticks", this, 10.0, 1.0, 20.0, 1.0);
    private final NumberValue timerBoostValue = new NumberValue("TimerBoost", this, 1.5, 0.01, 35.0, 0.01);
    private final NumberValue timerChargedValue = new NumberValue("TimerCharged", this, 0.45, 0.05, 5.0, 0.01);
    private final NumberValue rangeValue = new NumberValue("Range", this, 3.5, 1.0, 5.0, 0.1, () -> timerBoostMode.getValue().getName().equals("Normal"));
    private final NumberValue minRange = new NumberValue("MinRange", this, 1.0, 1.0, 5.0, 0.1, () -> timerBoostMode.getValue().getName().equals("Smart"));
    private final NumberValue maxRange = new NumberValue("MaxRange", this, 5.0, 1.0, 5.0, 0.1, () -> timerBoostMode.getValue().getName().equals("Smart"));
    private final NumberValue minTickDelay = new NumberValue("MinTickDelay", this, 5.0, 1.0, 100.0, 1.0, () -> timerBoostMode.getValue().getName().equals("Smart"));
    private final NumberValue maxTickDelay = new NumberValue("MaxTickDelay", this, 100.0, 1.0, 100.0, 1.0, () -> timerBoostMode.getValue().getName().equals("Smart"));
    private final BooleanValue resetlagBack = new BooleanValue("ResetOnLagback", this, false);

    @Override
    protected void onEnable() {
        this.timerReset();
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        this.timerReset();
        this.smartCounter = 0;
        this.playerTicks = 0;
        super.onDisable();
    }
    @EventLink
    private final Listener<AttackEvent> onAttack = event -> {
        if (!(event.getTarget() instanceof EntityLivingBase) || this.shouldResetTimer()) {
            this.timerReset();
            return;
        }
        this.confirmAttack = true;
        final EntityLivingBase targetEntity = (EntityLivingBase)event.getTarget();
        final double entityDistance = TimerRange.mc.thePlayer.getClosestDistanceToEntity((Entity)targetEntity);
        final int randomCounter = MathUtil.getRandomNumberUsingNextInt(this.minTickDelay.getValue().intValue(), this.maxTickDelay.getValue().intValue());
        final double randomRange = MathUtil.getRandomInRange(this.minRange.getValue().doubleValue(), this.maxRange.getValue().doubleValue());
        ++this.smartCounter;
        boolean shouldSlowed = false;
        switch (timerBoostMode.getValue().getName()) {
            case "Normal": {
                shouldSlowed = (entityDistance <= this.rangeValue.getValue().doubleValue());
                break;
            }
            case "Smart": {
                shouldSlowed = (this.smartCounter >= randomCounter && entityDistance <= randomRange);
                break;
            }
            default: {
                shouldSlowed = false;
                break;
            }
        }
        if (shouldSlowed && this.confirmAttack) {
            this.confirmAttack = false;
            this.playerTicks = this.ticksValue.getValue().intValue();
            if (this.resetlagBack.getValue()) {
                this.confirmLagBack = true;
            }
            this.smartCounter = 0;
        }
        else {
            this.timerReset();
        }
    };
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        final double timerboost = MathUtil.getRandomInRange(0.5, 0.56);
        final double charged = MathUtil.getRandomInRange(0.75, 0.91);
        if (this.playerTicks <= 0) {
            this.timerReset();
            return;
        }
        final double tickProgress = this.playerTicks / this.ticksValue.getValue().doubleValue();
        final float playerSpeed = (float)((tickProgress < timerboost) ? this.timerBoostValue.getValue() : ((tickProgress < charged) ? this.timerChargedValue.getValue() : 1.0));
        final float speedAdjustment = (playerSpeed >= 0.0f) ? playerSpeed : ((float)(1.0 + this.ticksValue.getValue().doubleValue() - this.playerTicks));
        final float adjustedTimerSpeed = Math.max(speedAdjustment, 0.0f);
        TimerRange.mc.timer.timerSpeed = adjustedTimerSpeed;
        --this.playerTicks;
    };
    private void timerReset() {
        TimerRange.mc.timer.timerSpeed = 1.0f;
    }

    private boolean shouldResetTimer() {
        final EntityPlayerSP player = TimerRange.mc.thePlayer;
        return this.playerTicks >= 1 || player.isSpectator() || player.isDead || player.isInWater() || player.isInLava() || player.isInWeb || player.isOnLadder() || player.isRiding();
    }
    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && this.resetlagBack.getValue() && this.confirmLagBack && !this.shouldResetTimer()) {
            this.confirmLagBack = false;
            this.timerReset();
            NotificationManager.post(NotificationType.WARNING, "TimerRange", "Lagback Detected | Timer Reset");
        }
    };
}
