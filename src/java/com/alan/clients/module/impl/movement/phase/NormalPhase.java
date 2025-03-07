package com.alan.clients.module.impl.movement.phase;


import com.alan.clients.module.impl.movement.Phase;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.other.BlockAABBEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.value.Mode;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;

public class NormalPhase extends Mode<Phase> {

    private boolean phasing;

    public NormalPhase(String name, Phase parent) {
        super(name, parent);
    }


    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        this.phasing = false;

        final double rotation = Math.toRadians(InstanceAccess.mc.thePlayer.rotationYaw);

        final double x = Math.sin(rotation);
        final double z = Math.cos(rotation);

        if (InstanceAccess.mc.thePlayer.isCollidedHorizontally) {
            InstanceAccess.mc.thePlayer.setPosition(InstanceAccess.mc.thePlayer.posX - x * 0.005, InstanceAccess.mc.thePlayer.posY, InstanceAccess.mc.thePlayer.posZ + z * 0.005);
            this.phasing = true;
        } else if (PlayerUtil.insideBlock()) {
            PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(InstanceAccess.mc.thePlayer.posX - x * 1.5, InstanceAccess.mc.thePlayer.posY, InstanceAccess.mc.thePlayer.posZ + z * 1.5, false));

            InstanceAccess.mc.thePlayer.motionX *= 0.3D;
            InstanceAccess.mc.thePlayer.motionZ *= 0.3D;

            this.phasing = true;
        }
    };


    @EventLink()
    public final Listener<BlockAABBEvent> onBlockAABB = event -> {
        // Sets The Bounding Box To The Players Y Position.
        if (event.getBlock() instanceof BlockAir && phasing) {
            final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

            if (y < InstanceAccess.mc.thePlayer.posY) {
                event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
            }
        }
    };
}
