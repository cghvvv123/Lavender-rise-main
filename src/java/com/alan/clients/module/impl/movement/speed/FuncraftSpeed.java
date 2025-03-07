package com.alan.clients.module.impl.movement.speed;

import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.MoveEvent;
import com.alan.clients.newevent.impl.other.TeleportEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

/**
 * @author Wykt
 * @since 2/04/2023
 */

public final class FuncraftSpeed extends Mode<Speed> {
    public FuncraftSpeed(String name, Speed parent) {
        super(name, parent);
    }

    private double moveSpeed;
    private boolean boost;

    @Override
    public void onEnable() {
        moveSpeed = 0;
        boost = false;
    }

    @EventLink
    private final Listener<TeleportEvent> teleportEventListener = event -> {
        moveSpeed = getBaseMoveSpeed();
        boost = false;
    };

    @EventLink
    private final Listener<MoveEvent> moveEventListener = event -> {
        if(!MoveUtil.isMoving()) {
            moveSpeed = 0;
            MoveUtil.setMoveEvent(event, 0);
            return;
        }

        if (InstanceAccess.mc.thePlayer.onGround && !InstanceAccess.mc.thePlayer.isCollidedHorizontally) {
            final double yaw = MoveUtil.direction();

            for (int i = 0; i < 7; i++) {
                // crazy code
                double offset = InstanceAccess.mc.theWorld.getBlockState(offsetPlayerPosition(0.15 * i)).getBlock().isFullBlock() ||
                        InstanceAccess.mc.theWorld.getBlockState(offsetPlayerPosition(InstanceAccess.mc.thePlayer.getPosition().add(0, 1, 0), 0.15 * i)).getBlock().isFullBlock() ||
                        InstanceAccess.mc.theWorld.getBlockState(offsetPlayerPosition(InstanceAccess.mc.thePlayer.getPosition().add(0, -1, 0), 0.15 * i)).getBlock() instanceof BlockAir ? -0.15 : 0.15;

                final double x = Math.sin(yaw) * offset;
                final double z = Math.cos(yaw) * offset;

                InstanceAccess.mc.thePlayer.setPosition(InstanceAccess.mc.thePlayer.posX - x, InstanceAccess.mc.thePlayer.posY, InstanceAccess.mc.thePlayer.posZ + z);
                PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(InstanceAccess.mc.thePlayer.posX, InstanceAccess.mc.thePlayer.posY, InstanceAccess.mc.thePlayer.posZ, true));
            }

            event.setPosY(InstanceAccess.mc.thePlayer.motionY = 0.42F);
            moveSpeed = getBaseMoveSpeed(0.6);
            boost = true;
        } else {
            if (boost) {
                moveSpeed = 1.2;
                boost = false;
                MoveUtil.setMoveEvent(event, moveSpeed);
                return;
            }

            moveSpeed = Math.max(moveSpeed - moveSpeed / 154, getBaseMoveSpeed());
        }

        MoveUtil.setMoveEvent(event, moveSpeed);
    };

    private double getBaseMoveSpeed() {
        return getBaseMoveSpeed(0.2873);
    }

    private double getBaseMoveSpeed(double baseSpeed) {
        if (InstanceAccess.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            baseSpeed *= 1 + 0.2 * (InstanceAccess.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }

        return baseSpeed;
    }

    private BlockPos offsetPlayerPosition(final double offset) {
        double yaw = MoveUtil.direction();
        return new BlockPos(InstanceAccess.mc.thePlayer.posX + -Math.sin(yaw) * offset, InstanceAccess.mc.thePlayer.posY, InstanceAccess.mc.thePlayer.posZ + Math.cos(yaw) * offset);
    }

    private BlockPos offsetPlayerPosition(final BlockPos blockPos, final double offset) {
        double yaw = MoveUtil.direction();
        return new BlockPos(blockPos.getX() + -Math.sin(yaw) * offset, blockPos.getY(), blockPos.getZ() + Math.cos(yaw) * offset);
    }
}
