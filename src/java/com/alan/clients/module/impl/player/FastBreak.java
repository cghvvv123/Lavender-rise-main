package com.alan.clients.module.impl.player;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.NumberValue;
import com.alan.clients.value.impl.SubMode;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;

@Rise
@ModuleInfo(name = "module.player.fastbreak.name", description = "module.player.fastbreak.description", category = Category.PLAYER)
public final class FastBreak extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Percentage"))
            .add(new SubMode("Ticks"))
            .add(new SubMode("GrimAC"))
            .setDefault("Ticks");

    private boolean boost = false;
    private float damage = 0;
    private BlockPos pos;
    private EnumFacing facing;

    public final NumberValue speed2 = new NumberValue("Speed", this, 1.1, 1.0, 3.0, 0.1,  () -> !mode.getValue().getName().equals("GrimAC"));
    private final NumberValue speed = new NumberValue("Speed", this, 50, 0, 100, 1,  () -> mode.getValue().getName().equals("Ticks") || mode.getValue().getName().equals("GrimAC"));
    private final NumberValue ticks = new NumberValue("Ticks", this, 1, 1, 100, 1, () -> !mode.getValue().getName().equals("Ticks"));


    @Override
    protected void onDisable() {

        if(InstanceAccess.mc.thePlayer!=null) InstanceAccess.mc.thePlayer.removePotionEffect(Potion.digSpeed.id);
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (isNull()) return;
        InstanceAccess.mc.playerController.blockHitDelay = 0;

        double percentageFaster = 0;

        switch (mode.getValue().getName()) {
            case "Percentage":
                percentageFaster = speed.getValue().doubleValue() / 100f;
                break;

            case "Ticks":
                if (InstanceAccess.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    BlockPos blockPos = InstanceAccess.mc.objectMouseOver.getBlockPos();
                    Block block = PlayerUtil.block(blockPos);

                    float blockHardness = block.getPlayerRelativeBlockHardness(InstanceAccess.mc.thePlayer, InstanceAccess.mc.theWorld, blockPos);
                    percentageFaster = blockHardness * ticks.getValue().intValue();
                }
                break;
            case "GrimAC":
                if (InstanceAccess.mc.playerController.extendedReach()) {
                    InstanceAccess.mc.playerController.blockHitDelay = 0;
                }
                else if (this.pos != null && this.boost) {
                    final IBlockState blockState = InstanceAccess.mc.theWorld.getBlockState(this.pos);
                    this.damage += (float)(blockState.getBlock().getPlayerRelativeBlockHardness(InstanceAccess.mc.thePlayer, InstanceAccess.mc.theWorld, this.pos) * this.speed2.getValue().doubleValue());
                    if (this.damage >= 1.0f) {
                        InstanceAccess.mc.theWorld.setBlockState(this.pos, Blocks.air.getDefaultState(), 11);
                        PacketUtil.sendNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.pos, this.facing));
                        PacketUtil.sendNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.pos, this.facing));
                        this.damage = 0.0f;
                        this.boost = false;
                    }
                }
                break;
        }

        if (InstanceAccess.mc.playerController.curBlockDamageMP > 1 - percentageFaster && !mode.getValue().getName().equals("GrimAC")) {
            InstanceAccess.mc.playerController.curBlockDamageMP = 1;
        }


    };


    @EventLink()
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        if (isNull()) return;
        if (event.getPacket() instanceof C07PacketPlayerDigging && mode.getValue().getName().equals("GrimAC")) {
            if (((C07PacketPlayerDigging)event.getPacket()).getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                this.boost = true;
                this.pos = ((C07PacketPlayerDigging)event.getPacket()).getPosition();
                this.facing = ((C07PacketPlayerDigging)event.getPacket()).getFacing();
                this.damage = 0.0f;
            }
            else if (((C07PacketPlayerDigging)event.getPacket()).getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK || ((C07PacketPlayerDigging)event.getPacket()).getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                this.boost = false;
                this.pos = null;
                this.facing = null;
            }
        }
    };
}
