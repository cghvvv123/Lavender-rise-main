package com.alan.clients.module.impl.movement.inventorymove;

import com.alan.clients.module.impl.movement.InventoryMove;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.Mode;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public final class NormalInventoryMove extends Mode<InventoryMove> {
    public NormalInventoryMove(String name, InventoryMove parent) {
        super(name, parent);
    }

    private final KeyBinding[] AFFECTED_BINDINGS = new KeyBinding[]{
            InstanceAccess.mc.gameSettings.keyBindForward,
            InstanceAccess.mc.gameSettings.keyBindBack,
            InstanceAccess.mc.gameSettings.keyBindRight,
            InstanceAccess.mc.gameSettings.keyBindLeft,
            InstanceAccess.mc.gameSettings.keyBindJump
    };


    @EventLink
    private final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        if (isNull()) return;
        if(InstanceAccess.mc.currentScreen == null || InstanceAccess.mc.currentScreen instanceof GuiChat || InstanceAccess.mc.currentScreen == this.getStandardClickGUI()) return;

        for (final KeyBinding bind : AFFECTED_BINDINGS) {
            bind.setPressed(GameSettings.isKeyDown(bind));
        }
    };
}
