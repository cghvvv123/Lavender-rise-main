package com.alan.clients.module.impl.combat;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.AttackEvent;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0BPacketEntityAction;

@Rise
@ModuleInfo(name = "module.combat.superknockback.name", description = "module.combat.superknockback.description", category = Category.COMBAT)
public class SuperKnockback extends Module {
    private final BooleanValue onlymove = new BooleanValue("OnlyMove",this,false);
    private final BooleanValue onlyground = new BooleanValue("OnlyGround", this,false);
    public final BooleanValue bf = new BooleanValue("Bypass BadPacketsF",this,true);
    boolean lastSprinting;
    @EventLink()
    private final Listener<WorldChangeEvent> onWorld = event -> {
        this.lastSprinting = false;
    };
    @EventLink
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        if (isNull()) return;
        final Packet<?> packet = event.getPacket();
        if (bf.getValue() && packet instanceof C0BPacketEntityAction) {
            if (((C0BPacketEntityAction)packet).getAction() == C0BPacketEntityAction.Action.START_SPRINTING) {
                if (this.lastSprinting) {
                    event.setCancelled(true);
                }
                this.lastSprinting = true;
            }
            else if (((C0BPacketEntityAction)packet).getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                if (!this.lastSprinting) {
                    event.setCancelled(true);
                }
                this.lastSprinting = false;
            }
        }
    };
    @EventLink
    public final Listener<AttackEvent> onAttackEvent = event -> {
        if ((!MoveUtil.isMoving() && this.onlymove.getValue()) || (!SuperKnockback.mc.thePlayer.onGround && this.onlyground.getValue())) {
            return;
        }
        if (SuperKnockback.mc.thePlayer.isSprinting()) {
            SuperKnockback.mc.thePlayer.setSprinting(true);
        }
        SuperKnockback.mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(SuperKnockback.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
        SuperKnockback.mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(SuperKnockback.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
        SuperKnockback.mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(SuperKnockback.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
        SuperKnockback.mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(SuperKnockback.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
        SuperKnockback.mc.thePlayer.setSprinting(true);
        EntityPlayerSP.serverSprintState = true;
    };
    
}
