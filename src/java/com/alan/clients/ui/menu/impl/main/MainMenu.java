package com.alan.clients.ui.menu.impl.main;

import com.alan.clients.Client;
import com.alan.clients.fontRender.FontManager;
import com.alan.clients.fontRender.RapeMasterFontManager;
import com.alan.clients.ui.MysteriousGui;
import com.alan.clients.ui.menu.Menu;
import com.alan.clients.ui.menu.component.button.MenuButton;
import com.alan.clients.ui.menu.component.button.impl.MenuTextButton;
import com.alan.clients.util.MouseUtil;
import com.alan.clients.util.animations.Animation;
import com.alan.clients.util.animations.Easing;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.shader.RiseShaders;
import com.alan.clients.util.shader.base.ShaderRenderType;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiSelectWorld;

import java.awt.*;
import java.io.IOException;

public final class MainMenu extends Menu {
    private Animation animation = new Animation(Easing.EASE_OUT_QUINT, 600);
    private MenuTextButton singlePlayerButton;
    private MenuTextButton multiPlayerButton;
    private MenuTextButton altManagerButton;
    private MenuTextButton ililliii;
    private MenuTextButton exitButton;

    private MenuButton[] menuButtons;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.singlePlayerButton == null || this.multiPlayerButton == null || this.altManagerButton == null) {
            return;
        }

        // Renders the background
        RiseShaders.MAIN_MENU_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, null);

        // Run blur
        RiseShaders.GAUSSIAN_BLUR_SHADER.update();
        RiseShaders.GAUSSIAN_BLUR_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, InstanceAccess.NORMAL_BLUR_RUNNABLES);

        // Run bloom
        RiseShaders.POST_BLOOM_SHADER.update();
        RiseShaders.POST_BLOOM_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES);

        // Run post shader things
        InstanceAccess.clearRunnables();

        // Renders the buttons
        this.singlePlayerButton.draw(mouseX, mouseY, partialTicks);
        this.multiPlayerButton.draw(mouseX, mouseY, partialTicks);
        this.altManagerButton.draw(mouseX, mouseY, partialTicks);
        this.exitButton.draw(mouseX,mouseY,partialTicks);
        this.ililliii.draw(mouseX, mouseY, partialTicks);
        RapeMasterFontManager changelog = FontManager.arial14;
        changelog.drawStringWithShadow("LavenderTeams",6,6, Color.WHITE.getRGB());
        changelog.drawStringWithShadow("   (ThanK U)",6,6 + changelog.getHeight(), Color.WHITE.getRGB());

        // TODO: Add the small "6.0"
        UI_BLOOM_RUNNABLES.forEach(Runnable::run);
        UI_BLOOM_RUNNABLES.clear();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.menuButtons == null) return;

        // If doing a left click and the mouse is hovered over a button, execute the buttons action (runnable)
        if (mouseButton == 0) {
            for (MenuButton menuButton : this.menuButtons) {
                if (MouseUtil.isHovered(menuButton.getX(), menuButton.getY(), menuButton.getWidth(), menuButton.getHeight(), mouseX, mouseY)) {
                    menuButton.runAction();
                    break;
                }
            }
        }
    }

    @Override
    public void initGui() {
        InstanceAccess.clearRunnables();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonWidth = 180;
        int buttonHeight = 24;
        int buttonSpacing = 6;
        int buttonX = centerX - buttonWidth / 2;
        int buttonY = centerY - buttonHeight / 2 - buttonSpacing / 2 - buttonHeight / 2;

        // Re-creates the buttons for not having to care about the animation reset
        this.singlePlayerButton = new MenuTextButton(buttonX, buttonY, buttonWidth, buttonHeight, () -> mc.displayGuiScreen(new GuiSelectWorld(this)), "Singleplayer");
        this.multiPlayerButton = new MenuTextButton(buttonX, buttonY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight, () -> mc.displayGuiScreen(new GuiMultiplayer(this)), "Multiplayer");
        this.altManagerButton = new MenuTextButton(buttonX, buttonY + buttonHeight * 2 + buttonSpacing * 2, buttonWidth, buttonHeight, () -> mc.displayGuiScreen(Client.INSTANCE.getAltManagerMenu()), "Alts");
        this.exitButton = new MenuTextButton(buttonX, buttonY + buttonHeight * 3 + buttonSpacing * 3, buttonWidth, buttonHeight, () -> System.exit(0), "EXIT");
        this.ililliii = new MenuTextButton(buttonX, buttonY + buttonHeight * 4 + buttonSpacing * 4, buttonWidth, buttonHeight, () -> mc.displayGuiScreen(new MysteriousGui()), "Don't Click");

        // Re-create the logo animation for not having to care about its reset
        this.animation = new Animation(Easing.EASE_OUT_QUINT, 600);

        // Putting all buttons in an array for handling mouse clicks
        this.menuButtons = new MenuButton[]{this.singlePlayerButton, this.multiPlayerButton, this.altManagerButton,this.exitButton,this.ililliii};
    }
}

