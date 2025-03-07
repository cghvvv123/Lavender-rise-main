package com.alan.clients.util.player;

import com.alan.clients.util.interfaces.InstanceAccess;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class KeyboardUtil implements InstanceAccess {

    public static boolean isPressed(KeyBinding key) {
        return Keyboard.isKeyDown(key.getKeyCode());
    }

    public static void resetKeybinding(KeyBinding key) {
        //if(mc.currentScreen instanceof GuiContainer) {
        if(mc.currentScreen != null) {
            key.pressed = false;
        } else {
            key.pressed = isPressed(key);
        }
    }

    public static void resetKeybindings(KeyBinding... keys) {
        for(KeyBinding key : keys) {
            resetKeybinding(key);
        }
    }

}
