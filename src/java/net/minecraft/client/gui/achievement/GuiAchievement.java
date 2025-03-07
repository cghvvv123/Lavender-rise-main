package net.minecraft.client.gui.achievement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ResourceLocation;

public class GuiAchievement extends Gui {
    private static final ResourceLocation achievementBg = new ResourceLocation("textures/gui/achievement/achievement_background.png");
    private final Minecraft mc;
    private int width;
    private int height;
    private String achievementTitle;
    private String achievementDescription;
    private Achievement theAchievement;
    private long notificationTime;
    private final RenderItem renderItem;
    private boolean permanentNotification;

    public GuiAchievement(final Minecraft mc) {
        this.mc = mc;
        this.renderItem = mc.getRenderItem();
    }

    public void displayAchievement(final Achievement ach) {
        this.achievementTitle = I18n.format("achievement.get");
        this.achievementDescription = ach.getStatName().getUnformattedText();
        this.notificationTime = Minecraft.getSystemTime();
        this.theAchievement = ach;
        this.permanentNotification = false;
    }

    public void displayUnformattedAchievement(final Achievement achievementIn) {
        this.achievementTitle = achievementIn.getStatName().getUnformattedText();
        this.achievementDescription = achievementIn.getDescription();
        this.notificationTime = Minecraft.getSystemTime() + 2500L;
        this.theAchievement = achievementIn;
        this.permanentNotification = true;
    }

    private void updateAchievementWindowScale() {
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        this.width = this.mc.displayWidth;
        this.height = this.mc.displayHeight;
        final ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        this.width = scaledresolution.getScaledWidth();
        this.height = scaledresolution.getScaledHeight();
        GlStateManager.clear(256);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, this.width, this.height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    public void updateAchievementWindow() {
        return ;
    }

    public void clearAchievements() {
        this.theAchievement = null;
        this.notificationTime = 0L;
    }
}
