package com.alan.clients.module.impl.player;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.BlockUtil;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Alan
 * @since 23/10/2021
 */

@Rise
@ModuleInfo(name = "module.player.noweb.name", description = "module.player.noweb.description", category = Category.PLAYER)
public class NoWeb extends Module {
    public final NumberValue range = new NumberValue("range", this, 3, 3, 20, 0.1);
    private final BooleanValue water = new BooleanValue("Water", this, true);
    private final BooleanValue web = new BooleanValue("Web", this, true);
    private final BooleanValue magma = new BooleanValue("Magma", this, true);
    private final BooleanValue ladder = new BooleanValue("Ladder",this,false);
    private final NumberValue yMotionValue = new NumberValue("YMotion",this,0.15, 0.1, 0.2, 0.01);
    Block magmaBlock = Blocks.lava;

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        final KillAura aura =getModule(KillAura.class);
        if (isNull()) return;
        if (ladder.getValue()) {
            if (aura.target != null) return;
            final Block block = BlockUtil.getBlock(new BlockPos(InstanceAccess.mc.thePlayer.posX, InstanceAccess.mc.thePlayer.posY + 1.0, InstanceAccess.mc.thePlayer.posZ));
            if ((block instanceof BlockLadder && InstanceAccess.mc.thePlayer.isCollidedHorizontally) || block instanceof BlockVine || BlockUtil.getBlock(new BlockPos(InstanceAccess.mc.thePlayer.posX, InstanceAccess.mc.thePlayer.posY, InstanceAccess.mc.thePlayer.posZ)) instanceof BlockVine) {
                InstanceAccess.mc.thePlayer.motionY = this.yMotionValue.getValue().doubleValue();
                InstanceAccess.mc.thePlayer.motionX = 0.0;
                InstanceAccess.mc.thePlayer.motionZ = 0.0;
            }
        }
        if (web.getValue() || water.getValue() || magma.getValue()) {
            if (InstanceAccess.mc.playerController.curBlockDamageMP != 0f) return;
            World world = InstanceAccess.mc.theWorld;
            final Map<BlockPos, Block> searchBlock = BlockUtil.searchBlocks(range.getValue().intValue());
            ArrayList<Packet<?>> packetsToSend = new ArrayList<>();

            for (final Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
                BlockPos pos = block.getKey();
                Block currentBlock = world.getBlockState(pos).getBlock();
                if (currentBlock instanceof BlockWeb && web.getValue() || currentBlock instanceof BlockLiquid && water.getValue()
                        || (currentBlock == Blocks.lava || currentBlock == magmaBlock) && magma.getValue()) {

                    packetsToSend.add(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, EnumFacing.DOWN));
                    packetsToSend.add(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.DOWN));

                    if (currentBlock instanceof BlockLiquid || currentBlock == magmaBlock) {
                        world.setBlockToAir(pos);
                    }
                }
            }
            packetsToSend.forEach(packet -> InstanceAccess.mc.getNetHandler().addToSendQueue(packet));
            if (InstanceAccess.mc.thePlayer.isOnLadder() && InstanceAccess.mc.gameSettings.keyBindJump.isKeyDown()) { // 快速上梯子
                if (InstanceAccess.mc.thePlayer.motionY >= 0.0) {
                    InstanceAccess.mc.thePlayer.motionY = 0.1786;
                }
            }
            if (web.getValue()) InstanceAccess.mc.thePlayer.isInWeb = false;
        }
    };
}
