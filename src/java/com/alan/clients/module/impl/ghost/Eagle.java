package com.alan.clients.module.impl.ghost;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

/**
 * @author Auth
 * @since 27/4/2022
 */
@Rise
@ModuleInfo(name = "module.ghost.eagle.name", description = "module.ghost.eagle.description", category = Category.GHOST)
public class Eagle extends Module {

    public static Block getBlock(final BlockPos pos) {
        return Eagle.mc.theWorld.getBlockState(pos).getBlock();
    }
    public static Block getBlockUnderPlayer(final EntityPlayer player) {
        return getBlock(new BlockPos(player.posX, player.posY - 1.0, player.posZ));
    }
    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (getBlockUnderPlayer(Eagle.mc.thePlayer) instanceof BlockAir) {
            if (Eagle.mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(Eagle.mc.gameSettings.keyBindSneak.getKeyCode(), true);
            }
        }
        else if (Eagle.mc.thePlayer.onGround) {
            KeyBinding.setKeyBindState(Eagle.mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }
    };

    @Override
    public void onEnable() {
        if (Eagle.mc.thePlayer == null) {
            return;
        }
        Eagle.mc.thePlayer.setSneaking(false);
        super.onEnable();
    }
    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(Eagle.mc.gameSettings.keyBindSneak.getKeyCode(), false);
        super.onDisable();
    }
}
