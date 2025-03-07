package com.alan.clients.module.impl.render;


import com.alan.clients.Client;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.Priorities;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.input.KeyboardInputEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.ui.click.other.HanabiClickGUI;
import com.alan.clients.ui.click.standard.RiseClickGUI;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.SubMode;
import org.lwjgl.input.Keyboard;

import java.awt.*;

/**
 * Displays a GUI which can display and do various things
 *
 * @author Alan
 * @since 04/11/2021
 */
@ModuleInfo(name = "module.render.clickgui.name", description = "module.render.clickgui.description", category = Category.RENDER, keyBind = Keyboard.KEY_RSHIFT)
public final class ClickGUI extends Module {


    public final ModeValue mode = new ModeValue("Click Gui Type", this)
            .add(new SubMode("Rise"))
            .add(new SubMode("Hanabi"))
            .setDefault("Rise");

    private final HanabiClickGUI hanabiClickGUI = new HanabiClickGUI();



    @Override
    public void onEnable() {

        switch(mode.getValue().getName()){
            case "Rise":
                Client.INSTANCE.getEventBus().register(Client.INSTANCE.getStandardClickGUI());
                InstanceAccess.mc.displayGuiScreen(Client.INSTANCE.getStandardClickGUI());
                break;
            case "Hanabi":
                InstanceAccess.mc.thePlayer.playSound("random.click", 1, 1);
                hanabiClickGUI.reset();
                InstanceAccess.mc.displayGuiScreen(hanabiClickGUI);
                break;
        }

    }




    @Override
    public void onDisable() {
        Keyboard.enableRepeatEvents(false);
        Client.INSTANCE.getEventBus().unregister(Client.INSTANCE.getStandardClickGUI());
        Client.INSTANCE.getExecutor().execute(() -> Client.INSTANCE.getConfigFile().write());
    }

    @EventLink(value = Priorities.HIGH)
    public final Listener<Render2DEvent> onRender2D = event -> {
        if(InstanceAccess.mc.currentScreen instanceof RiseClickGUI){
            double width = event.getScaledResolution().getScaledWidth();
            double height = event.getScaledResolution().getScaledHeight();
            InstanceAccess.UI_RENDER_RUNNABLES.add(() -> Client.INSTANCE.getStandardClickGUI().render());
            InstanceAccess.UI_BLOOM_RUNNABLES.add(() -> Client.INSTANCE.getStandardClickGUI().bloom());
            InstanceAccess.NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.rectangle(0, 0, width, height, Color.BLACK));
        }

        if (this.mc.currentScreen == null) {
            this.setEnabled(false);
        }
    };

    @EventLink()
    public final Listener<KeyboardInputEvent> onKey = event -> {

        if (event.getKeyCode() == this.getKeyCode()) {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
        }
    };
}
