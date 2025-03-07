package com.alan.clients.module.impl.player;

import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@Rise
@ModuleInfo(name = "module.player.autotool.name", description = "module.player.autotool.description", category = Category.PLAYER)
public class AutoTool extends Module {
    private final BooleanValue spoof = new BooleanValue("ItemSpoof",this,true);
    private int oldSlot;

    private boolean wasDigging;

    @Override
    protected void onDisable() {
        if(wasDigging) {
            InstanceAccess.mc.thePlayer.inventory.currentItem = oldSlot;
            wasDigging = false;
        }

        Client.INSTANCE.getSlotSpoofHandler().stopSpoofing();
        super.onDisable();
    }
    @EventLink()
    public final Listener<TickEvent> onTick = event -> {
        if((Mouse.isButtonDown(0) || InstanceAccess.mc.gameSettings.keyBindAttack.isKeyDown()) && InstanceAccess.mc.objectMouseOver != null && InstanceAccess.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            Block block = InstanceAccess.mc.theWorld.getBlockState(InstanceAccess.mc.objectMouseOver.getBlockPos()).getBlock();

            float strength = 0;

            if(!wasDigging) {
                oldSlot = InstanceAccess.mc.thePlayer.inventory.currentItem;

                if(spoof.getValue()) {
                    Client.INSTANCE.getSlotSpoofHandler().startSpoofing(oldSlot);
                }
            }

            for(int i = 0; i <= 8; i++) {
                ItemStack stack = InstanceAccess.mc.thePlayer.inventory.getStackInSlot(i);

                if(stack != null) {
                    float slotStrength = stack.getStrVsBlock(block);

                    if(slotStrength > strength) {
                        InstanceAccess.mc.thePlayer.inventory.currentItem = i;
                        strength = slotStrength;
                    }
                }
            }

            wasDigging = true;
        } else {
            if(wasDigging) {
                InstanceAccess.mc.thePlayer.inventory.currentItem = oldSlot;

                Client.INSTANCE.getSlotSpoofHandler().stopSpoofing();

                wasDigging = false;
            } else {
                oldSlot = InstanceAccess.mc.thePlayer.inventory.currentItem;
            }
        }
    };
}
