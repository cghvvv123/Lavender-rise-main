package com.alan.clients.module.impl.player;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.other.MoveEvent;
import com.alan.clients.util.MSTimer;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer;
@ModuleInfo(name = "FastEat",category = Category.PLAYER,description = "CNM")
public class FastEat extends Module {
    private final BooleanValue noMoveValue = new BooleanValue("NoMove", this, false);
    private final MSTimer msTimer = new MSTimer();
    private boolean usedTimer;
    public boolean grimEat;
    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (FastEat.mc.thePlayer == null) {
            return;
        }
        if (this.usedTimer) {
            FastEat.mc.timer.timerSpeed = 1.0f;
            this.usedTimer = false;
        }
        if (!FastEat.mc.thePlayer.isUsingItem()) {
            this.msTimer.reset();
            return;
        }
        final Item usingItem = FastEat.mc.thePlayer.getItemInUse().getItem();
        if (usingItem instanceof ItemFood || usingItem instanceof ItemBucketMilk || (usingItem instanceof ItemPotion && !(FastEat.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword))) {
            this.usedTimer = true;
            this.grimEat = true;
            FastEat.mc.timer.timerSpeed = 0.3f;
            for (int i = 0; i < 2; ++i) {
                final EntityPlayerSP thePlayer = FastEat.mc.thePlayer;
                ++thePlayer.positionUpdateTicks;
                FastEat.mc.getNetHandler().addToSendQueue(new C03PacketPlayer(FastEat.mc.thePlayer.onGround));
            }
            this.grimEat = false;
        }
    };
    @EventLink()
    public final Listener<MoveEvent> onMove = event -> {
        if (FastEat.mc.thePlayer == null || event == null) {
            return;
        }
        if (!this.isEnabled() || !FastEat.mc.thePlayer.isUsingItem() || !this.noMoveValue.getValue()) {
            return;
        }
        final Item usingItem = FastEat.mc.thePlayer.getItemInUse().getItem();
        if (usingItem instanceof ItemFood || usingItem instanceof ItemBucketMilk || (usingItem instanceof ItemPotion && !(FastEat.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword))) {
            event.setCancelled(true);
        }
    };

    @Override
    protected void onDisable() {
        if (this.usedTimer) {
            FastEat.mc.timer.timerSpeed = 1.0f;
            this.usedTimer = false;
        }
        super.onDisable();
    }
}
