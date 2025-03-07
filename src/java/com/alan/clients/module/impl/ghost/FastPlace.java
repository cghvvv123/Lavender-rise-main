package com.alan.clients.module.impl.ghost;

import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemSnowball;

/**
 * @author Alan
 * @since 29/01/2021
 */

@Rise
@ModuleInfo(name = "module.ghost.fastplace.name", description = "module.ghost.fastplace.description", category = Category.GHOST)
public class FastPlace extends Module {

    private final NumberValue delay = new NumberValue("Delay", this, 0, 0, 3, 1);
    private final BooleanValue projectiles = new BooleanValue("Projectiles", this, false);
    private final BooleanValue blocks = new BooleanValue("Blocks", this, true);
    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        Scaffold Scaffold = Client.INSTANCE.getModuleManager().get(Scaffold.class);
        if(Scaffold.isEnabled()) return;
        if (canFastPlace()) {
            InstanceAccess.mc.rightClickDelayTimer = Math.min(InstanceAccess.mc.rightClickDelayTimer, delay.getValue().intValue());
        }
    };
    private boolean canFastPlace() {
        if (InstanceAccess.mc.thePlayer == null || InstanceAccess.mc.thePlayer.getCurrentEquippedItem() == null || InstanceAccess.mc.thePlayer.getCurrentEquippedItem().getItem() == null)
            return false;
        Item heldItem = InstanceAccess.mc.thePlayer.getCurrentEquippedItem().getItem();
        return (blocks.getValue() && heldItem instanceof ItemBlock) || (projectiles.getValue() && (heldItem instanceof ItemSnowball || heldItem instanceof ItemEgg));
    }
}
