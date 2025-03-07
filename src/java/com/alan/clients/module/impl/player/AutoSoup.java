package com.alan.clients.module.impl.player;

import com.alan.clients.api.Rise;
import com.alan.clients.component.impl.player.SlotComponent;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.util.MSTimer;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.BowlList;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.ListValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import static com.alan.clients.util.interfaces.InstanceAccess.mc;

@Rise
@ModuleInfo(name = "module.player.autosoup.name", description = "module.player.autosoup.description", category = Category.PLAYER)
public class AutoSoup extends Module {
    private final NumberValue health = new NumberValue("Health", this, 15, 1, 20, 1);
    private final BooleanValue openInventoryValue = new BooleanValue("OpenInv", this, false);
    private final BooleanValue simulateInventoryValue = new BooleanValue("SimulateInventory", this, false);
    private final ListValue<BowlList> bowlValueDrop = new ListValue<>("Bowl", this);
    private final MSTimer timer = new MSTimer();

    public AutoSoup() {
        for (BowlList Bowl : BowlList.values()) {
            bowlValueDrop.add(Bowl);
        }
        bowlValueDrop.setDefault(BowlList.Drop);
    }
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (InstanceAccess.mc.thePlayer.getHealth() > health.getValue().floatValue())
            return;

        if (mc.thePlayer == null) return;

        int soupInHotbar = findItem(36, 45, Items.mushroom_stew);

        if (mc.thePlayer.getHealth() <= health.getValue().intValue() && soupInHotbar != -1) {

            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(soupInHotbar - 36));
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(soupInHotbar)));

            if (bowlValueDrop.getValue() == BowlList.Drop)
                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));

            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            return;
        }

        int bowlInHotbar = findItem(36, 45, Items.bowl);
        if (bowlValueDrop.getValue() == BowlList.Move && bowlInHotbar != -1) {
            if (openInventoryValue.getValue() && !(mc.currentScreen instanceof GuiInventory)) {
                return;
            }

            boolean bowlMovable = false;

            for (int i = 9; i <= 35; i++) {
                ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

                if (itemStack == null) {
                    bowlMovable = true;
                    break;
                } else if (itemStack.getItem() == Items.bowl && itemStack.stackSize < 64) {
                    bowlMovable = true;
                    break;
                }
            }

            if (bowlMovable) {
                boolean openInventory = !(mc.currentScreen instanceof GuiInventory) && simulateInventoryValue.getValue();

                if (openInventory) {
                    mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                }

                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, bowlInHotbar, 0, 1, mc.thePlayer);
            }
        }

        int soupInInventory = findItem(9, 36, Items.mushroom_stew);

        if (soupInInventory != -1 && hasSpaceHotbar()) {
            if (openInventoryValue.getValue() && !(mc.currentScreen instanceof GuiInventory)) {
                return;
            }

            boolean openInventory = !(mc.currentScreen instanceof GuiInventory) && simulateInventoryValue.getValue();

            if (openInventory) {
                mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            }

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, soupInInventory, 0, 1, mc.thePlayer);

            if (openInventory) {
                mc.getNetHandler().addToSendQueue(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            }
        }
        /*if (InstanceAccess.mc.thePlayer.getHealth() <= health.getValue().floatValue()) {
            for (int i = 0; i < 9; i++) {
                if (InstanceAccess.mc.thePlayer.getHealth() > health.getValue().floatValue())
                    break;

                final ItemStack stack = InstanceAccess.mc.thePlayer.inventory.getStackInSlot(i);

                if (stack == null)
                    continue;

                if (stack.getItem() instanceof ItemSoup) {
                    SlotComponent.setSlot(i);

                    PacketUtil.send(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
                }
            }
        }

         */
    };
    public static boolean hasSpaceHotbar() {
        for (int i = 36; i < 45; i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null)
                return true;
        }

        return false;
    }
    public static int findItem(final int startSlot, final int endSlot, final Item item) {
        for (int i = startSlot; i < endSlot; i++) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem().equals(item))
                return i;
        }

        return -1;
    }
}

