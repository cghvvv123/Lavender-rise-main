package com.alan.clients.module.impl.render;

import com.alan.clients.fontRender.FontManager;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemFood;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import java.awt.*;

@ModuleInfo(name = "module.render.progessbar.name", category = Category.RENDER, description = "CNM")
public class ProgessBar extends Module {
    private long eatStartTime = 0;
    private boolean isEating = false;

    private boolean hasEaten = false;
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (isNull()) return;
        ScaledResolution scaledResolution = new ScaledResolution(InstanceAccess.mc);
        int screenWidth = scaledResolution.getScaledWidth();
        int screenHeight = scaledResolution.getScaledHeight();
        int x = screenWidth / 2 - 80;
        int y = (int) (screenHeight / 2.06 - 3.25f);
        float width = 160;
        float height = 6.5F;
        float radius = 2;
        if (InstanceAccess.mc.thePlayer.getHeldItem() != null) {
            if (InstanceAccess.mc.thePlayer.isEating() && InstanceAccess.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
                if (!isEating) {
                    isEating = true;
                    eatStartTime = System.currentTimeMillis();
                    hasEaten = false;
                }

                float timerSpeed = InstanceAccess.mc.timer.timerSpeed;
                long duration = (long) (1500 / timerSpeed);
                long elapsedTime = System.currentTimeMillis() - eatStartTime;
                float progress = Math.min(1.0f, elapsedTime / (float) duration);
                InstanceAccess.NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                    RenderUtil.roundedRectangle(x, y, width, height, radius, new Color(0, 0, 0, 120));
                    float fillWidth = width * progress;
                    RenderUtil.roundedRectangle(x, y, fillWidth, height, radius, new Color(240, 10, 10, 199));
                });
                InstanceAccess.NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(x, y, width, height, radius, Color.BLACK));
                InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(x, y, width, height, radius + 1, getTheme().getDropShadow()));

                String progressText = String.format("%.0f%%", progress * 100);
                int progressTextY = y + (int) height + 5;
                FontManager.arial18.drawStringWithShadow(progressText, x + width / 2 - (float) FontManager.arial18.getStringWidth(progressText) / 2, progressTextY - FontManager.arial18.getHeight() * 2, 0xFFFFFFFF);

                if (elapsedTime >= duration && !hasEaten) {
                    hasEaten = true;
                }
            } else {
                isEating = false;
                hasEaten = false;
            }
        }
        if (InstanceAccess.mc.objectMouseOver != null && InstanceAccess.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos blockPos = InstanceAccess.mc.objectMouseOver.getBlockPos();
            IBlockState blockState = InstanceAccess.mc.theWorld.getBlockState(blockPos);
            Block block = blockState.getBlock();

            if (block.getBlockHardness(InstanceAccess.mc.theWorld, blockPos) >= 0.0f) {
                float progress = InstanceAccess.mc.playerController.curBlockDamageMP;
                if (progress > 0.0f) {
                    drawPercentage(progress);
                }
            }
        }
    };

    private void drawPercentage(float progress) {
        ScaledResolution scaledRes = new ScaledResolution(InstanceAccess.mc);
        int screenWidth = scaledRes.getScaledWidth();
        int screenHeight = scaledRes.getScaledHeight();

        int barWidth = 100;
        int barHeight = (int) 6.5f;

        int posX = screenWidth / 2 - barWidth / 2;
        int posY = (int) (screenHeight / 2.06 - barHeight / 2);
        int percentage = (int) (progress * 100);
        InstanceAccess.NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            RenderUtil.roundedRectangle(posX, posY, barWidth, barHeight, 3.0, new Color(0, 0, 0, 150));
            int fillWidth = (int) (progress * barWidth);
            RenderUtil.roundedRectangle(posX, posY, fillWidth, barHeight, 3.0, new Color(255, 0, 0, 220));
        });
        InstanceAccess.NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(posX, posY, 100, barHeight, 3.0, Color.BLACK));
        InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(posX, posY, 100, barHeight, 3.0 + 1, getTheme().getDropShadow()));
        String text = percentage + "%";
        FontManager.arial18.drawStringWithShadow(text, posX + (float) barWidth / 2 - (float) FontManager.arial18.getStringWidth(text) / 2, posY - FontManager.arial18.getHeight(), 0xFFFFFFFF);
    }

}
