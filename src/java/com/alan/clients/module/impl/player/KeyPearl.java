package com.alan.clients.module.impl.player;

import com.alan.clients.component.impl.player.SlotComponent;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.player.SlotUtil;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.SubMode;
import net.minecraft.init.Items;

@ModuleInfo(name = "KeyPearl", description = "CNM", category = Category.PLAYER)
public class KeyPearl extends Module {
    private final ModeValue mode = new ModeValue("Modes", this)
            .add(new SubMode("Middle"))
            .add(new SubMode("Key"))
            .setDefault("Middle");
    private boolean flag;

    @Override
    protected void onEnable() {
        if (mode.getValue().getName().equalsIgnoreCase("Key")) {
            throwPearl();
            this.setEnabled(false);
        }
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        flag = false;
        super.onDisable();
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (isNull() || mode.getValue().getName().equalsIgnoreCase("Key")) return;
        if (mc.gameSettings.keyBindPickBlock.isKeyDown()) {
            flag = true;
        }
        if (flag && !mc.gameSettings.keyBindPickBlock.isKeyDown()) {
            throwPearl();
            flag = false;
        }
    };

    public void throwPearl() {
        SlotComponent.setSlot(SlotUtil.findItem(Items.ender_pearl), false);
        mc.rightClickMouse();
    }
}
