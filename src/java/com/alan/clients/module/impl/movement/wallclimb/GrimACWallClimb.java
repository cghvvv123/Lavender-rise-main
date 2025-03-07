package com.alan.clients.module.impl.movement.wallclimb;

import com.alan.clients.module.impl.movement.WallClimb;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.input.MoveInputEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.other.BlockAABBEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLadder;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class GrimACWallClimb extends Mode<WallClimb> {
    public GrimACWallClimb(String name, WallClimb parent) {
        super(name, parent);
    }
    private final BooleanValue sneak = new BooleanValue("Legit", this, false);
    private boolean inWall;
    private BlockPos targetPos;
    private BlockPos blockpos2 = new BlockPos(0,0,0);

    @Override
    public void onEnable() {
        reset();
    }

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (isNull()) return;
        if (inWall && targetPos != null && (Math.abs(targetPos.getX() - InstanceAccess.mc.thePlayer.posX) >= 1.5 || Math.abs(targetPos.getZ() - InstanceAccess.mc.thePlayer.posZ) >= 1.5))
            reset();

        if (inWall) {
            if (targetPos == null) return;

            targetPos = new BlockPos(targetPos.getX(), Math.floor(InstanceAccess.mc.thePlayer.posY), targetPos.getZ());
            blockpos2 = targetPos;
            final BlockPos blockNeedBreak = targetPos.up(2);

            if (cantBreak(InstanceAccess.mc.theWorld.getBlockState(blockNeedBreak).getBlock())) {
                reset();

                return;
            }

            InstanceAccess.mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockNeedBreak, EnumFacing.DOWN));
            for (int i = 0;i <= 1;i++) {
                final BlockPos offsetPos = targetPos.up(i);
                if (cantBreak(InstanceAccess.mc.theWorld.getBlockState(offsetPos).getBlock())) continue;
                InstanceAccess.mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, offsetPos, EnumFacing.DOWN));
                InstanceAccess.mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, offsetPos, EnumFacing.DOWN));
            }
        } else {
            final float yaw = (float) Math.toRadians(InstanceAccess.mc.thePlayer.rotationYaw);
            targetPos = new BlockPos(InstanceAccess.mc.thePlayer.posX - MathHelper.sin(yaw), InstanceAccess.mc.thePlayer.posY, InstanceAccess.mc.thePlayer.posZ + MathHelper.cos(yaw));
            for (int i = 0;i <= 1;i++) {
                final BlockPos offsetPos = targetPos.add(0, i, 0);
                if (cantBreak(InstanceAccess.mc.theWorld.getBlockState(offsetPos).getBlock())) continue;
                InstanceAccess.mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, offsetPos, EnumFacing.DOWN));
                InstanceAccess.mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, offsetPos, EnumFacing.DOWN));
            }
        }
    };

    @EventLink
    private final Listener<BlockAABBEvent> onBlockAABB = event -> {
        if (isNull()) return;
        final BlockPos blockPos = event.getBlockPos();

        if (targetPos == null || event.getBlock() instanceof BlockLadder) return;

        if (blockPos.equals(targetPos) || blockPos.equals(targetPos.up()) || (blockPos.equals(targetPos.up(2)) && inWall))
            event.setBoundingBox(null);
    };

    @EventLink
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (isNull()) return;
        final Packet<?> packet = event.getPacket();

        if (targetPos == null || inWall) return;

        if (packet instanceof S08PacketPlayerPosLook) {
            inWall = true;
        }
    };

    @EventLink
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        if (isNull()) return;
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C07PacketPlayerDigging) {
            final C07PacketPlayerDigging wrapped = (C07PacketPlayerDigging) packet;

            if (wrapped.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK && targetPos != null)
                event.setCancelled();
        }
    };

    @EventLink
    private final Listener<MoveInputEvent> onMoveInput = event -> {
        if (sneak.getValue() && targetPos != null)
            event.setSneak(true);
    };
    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {
if (blockpos2 != new BlockPos(0,0,0)) {
    RenderUtil.drawBlockBox(blockpos2, Color.RED,true);
}
    };
    private boolean cantBreak(Block block) {
        return block instanceof BlockAir || block instanceof BlockLadder;
    }

    private void reset() {
        inWall = false;
        targetPos = null;
    }
}
