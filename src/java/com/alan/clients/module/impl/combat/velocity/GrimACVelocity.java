package com.alan.clients.module.impl.combat.velocity;

import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.module.impl.combat.Velocity;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.util.RayCastUtil;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.util.vector.Vector3d;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.SubMode;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.viamcp.ViaMCP;

public class GrimACVelocity extends Mode<Velocity> {
    public GrimACVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Block Spoof"))
            .add(new SubMode("Attack Reduce"))
            .add(new SubMode("1.17+"))
            .setDefault("Block Spoof");

    private final BooleanValue rayCast = new BooleanValue("Ray cast", this, false, () -> !mode.getValue().getName().equalsIgnoreCase("Attack Reduce"));
    private final BooleanValue s08debug = new BooleanValue("S08 Check Debug", this, true);
    private final BooleanValue debug = new BooleanValue("Debug",this,true);
    private final BooleanValue FireCheckValue = new BooleanValue("FireCheck",this, false);
    private final BooleanValue  WaterCheckValue = new BooleanValue("WaterCheck",this, false);
    private boolean can = false;

    private boolean should;

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdateEvent = event -> {
        if (mc.thePlayer.hurtTime > 0 && should) {
            mc.thePlayer.motionX *= 0.076;
            mc.thePlayer.motionZ *= 0.076;
            this.should = false;
        }
    };

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        Packet<?> packet = event.getPacket();
        if (isNull()) return;
        if (Velocity.mc.thePlayer == null) {
            return;
        }
        if (!getModule(KillAura.class).isEnabled()) {
            return;
        }
        if (Velocity.mc.thePlayer.isOnLadder()) {
            return;
        }
        if (Velocity.mc.thePlayer.isDead) {
            return;
        }
        if (Velocity.mc.currentScreen instanceof GuiGameOver) {
            return;
        }
        if (Velocity.mc.thePlayer.isBurning() && this.FireCheckValue.getValue()) {
            return;
        }
        if (Velocity.mc.thePlayer.isInWater() && this.WaterCheckValue.getValue()) {
            return;
        }
        if (packet instanceof S08PacketPlayerPosLook && s08debug.getValue()) {
            ChatUtil.display("S08 Flags");
        }
        if (packet instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapped = (S12PacketEntityVelocity) packet;
            if (mc.thePlayer != null) {
                if (wrapped.getEntityID() == mc.thePlayer.getEntityId()) {
                    double strength = (new Vector3d(wrapped.getMotionX(), wrapped.getMotionY(), wrapped.getMotionZ())).length();
                    switch (mode.getValue().getName().toLowerCase()) {
                        case "block spoof": {
                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer(mc.thePlayer.onGround));
                            mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer), EnumFacing.UP));
                            mc.timer.lastSyncSysClock += 1;
                            event.setCancelled();

                            break;
                        }
                        case "1.17+": {
                            mc.getNetHandler().addToSendQueueUnregistered(new C03PacketPlayer.C06PacketPlayerPosLook(
                                    mc.thePlayer.posX,
                                    mc.thePlayer.posY,
                                    mc.thePlayer.posZ,
                                    RotationComponent.rotations.x,
                                    RotationComponent.rotations.y,
                                    mc.thePlayer.onGround
                            ));
                            mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer), EnumFacing.UP));
                            event.setCancelled();

                            break;
                        }
                        case "attack reduce": {
                            Entity entity = null;
                            if (rayCast.getValue()) {
                                final MovingObjectPosition position = RayCastUtil.rayCast(RotationComponent.rotations, getModule(KillAura.class).range.getValue().doubleValue());

                                if (position != null && position.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
                                    entity = position.entityHit;
                            } else {
                                entity = getModule(KillAura.class).target;
                            }
                            if (this.debug.getValue()) {
                                if (entity != null) {
                                    ChatUtil.display("Strength: " + strength + ", Target: " + entity.getCommandSenderName());
                                }
                            }
                            if (entity != null) {


                                if (!mc.thePlayer.serverSprintState) {
                                    com.alan.clients.util.packet.PacketUtil.sendPacketC0F();
                                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                                    mc.thePlayer.serverSprintState = true;
                                }

                                for (int i = 0; i < 8; i++) {
                                    if (ViaMCP.getInstance().getVersion() <= 47)
                                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                    mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
                                    can= true;
                                    if (ViaMCP.getInstance().getVersion() >= 47) {
                                        PacketWrapper c0A = PacketWrapper.create(26, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                                        c0A.write(Type.VAR_INT, 0);
                                        PacketUtil.sendToServer(c0A, Protocol1_8To1_9.class, true, true);
                                    }
                                }
                                if (mc.thePlayer.serverSprintState && can) {
                                    com.alan.clients.util.packet.PacketUtil.sendPacketC0F();
                                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                                    mc.thePlayer.serverSprintState = false;
                                }

                                this.should = true;
                            }

                            break;
                        }
                    }
                }
            }
        }
    };
}
