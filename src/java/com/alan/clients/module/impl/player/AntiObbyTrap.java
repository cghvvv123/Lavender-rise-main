package com.alan.clients.module.impl.player;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.packet.PacketUtil;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.concurrent.ThreadLocalRandom;
@ModuleInfo(name = "AntiObbyTrap",category = Category.PLAYER,description = "Stop being a bot and falling for obby traps")
public class AntiObbyTrap extends Module {
    private float currentDamage;
    @Getter
    private boolean digging;
    @EventLink()
    public final Listener<PreMotionEvent> onPreUpdate = event -> {
        if (this.mc.theWorld.getBlockState(new BlockPos(event.getPosX(), event.getPosY() + 1, event.getPosZ()))
                .getBlock() == Blocks.obsidian || this.mc.theWorld.getBlockState(new BlockPos(event.getPosX(), event.getPosY() + 1, event.getPosZ()))
                .getBlock() == Blocks.cobblestone || this.mc.theWorld.getBlockState(new BlockPos(event.getPosX(), event.getPosY() + 2, event.getPosZ()))
                .getBlock() instanceof BlockFurnace) {
            event.setPitch(89 + ThreadLocalRandom.current().nextFloat());
            Block currentBlock = this.mc.theWorld.getBlockState(new BlockPos(event.getPosX(), event.getPosY() - 1, event.getPosZ())).getBlock();
            BlockPos pos = new BlockPos(event.getPosX(), event.getPosY() - 1, event.getPosZ());

            if (this.currentDamage == 0.0F) {
                this.digging = true;
                PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
            }

            mc.thePlayer.updateTool(pos);
            PacketUtil.send(new C0APacketAnimation());
            this.currentDamage += currentBlock.getPlayerRelativeBlockHardness(this.mc.thePlayer, this.mc.theWorld, pos);
            this.mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), pos, (int) (this.currentDamage * 10.0F) - 1);

            if (this.currentDamage >= 1.0F) {
                PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));
                this.mc.playerController.onPlayerDestroyBlock(pos, EnumFacing.UP);
                this.currentDamage = 0.0F;
                this.digging = false;
            }
        } else {
            this.currentDamage = 0.0F;
            this.digging = false;
        }
    };
}
