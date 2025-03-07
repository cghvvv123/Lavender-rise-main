package com.alan.clients.module.impl.render;

import com.alan.clients.api.Rise;
import com.alan.clients.component.impl.render.ProjectionComponent;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.entity.player.EntityPlayer;

import javax.vecmath.Vector4d;
import java.awt.*;

/**
 * @author Hazsi, Alan
 * @since 10/11/2022
 */
@Rise
@ModuleInfo(name = "module.render.2desp.name", description = "module.render.projectionesp.description", category = Category.RENDER)
public class ProjectionESP extends Module {

    public BooleanValue glow = new BooleanValue("Glow", this, true);

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (isNull()) return;
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (mc.getRenderManager() == null  || (player.equals(mc.thePlayer) && mc.gameSettings.thirdPersonView == 0) ||
                    !RenderUtil.isInViewFrustrum(player) || player.isDead || player.isInvisible()) {
                continue;
            }
            Vector4d pos = ProjectionComponent.get(player);

            if (pos == null) {
                continue;
            }

            // Black outline
            RenderUtil.rectangle(pos.x, pos.y, pos.z - pos.x, 1.5, Color.BLACK); // Top
            RenderUtil.rectangle(pos.x, pos.y, 1.5, pos.w - pos.y + 1.5, Color.BLACK); // Left
            RenderUtil.rectangle(pos.z, pos.y, 1.5, pos.w - pos.y + 1.5, Color.BLACK); // Right
            RenderUtil.rectangle(pos.x, pos.w, pos.z - pos.x, 1.5, Color.BLACK); // Bottom

            RenderUtil.rectangle(pos.x-4, pos.y, 2, pos.w - pos.y + 1.5, Color.BLACK); // 血条Left
            // Main ESP
            Runnable runnable = () -> {

                final Vector2d first = new Vector2d(0, 0), second = new Vector2d(0, 500);
                float healthValue = player.getHealth() / player.getMaxHealth();
                Color healthColor = healthValue > .75 ? new Color(66, 246, 123) : healthValue > .5 ? new Color(228, 255, 105) : healthValue > .35 ? new Color(236, 100, 64) : new Color(255, 65, 68);
                RenderUtil.horizontalGradient(pos.x + 0.5, pos.y + 0.5, pos.z - pos.x, 0.5, // Top
                        new Color(189, 185, 185),  new Color(255,255,255));
                RenderUtil.verticalGradient(pos.x + 0.5, pos.y + 0.5, 0.5, pos.w - pos.y + 0.5, // Left
                        new Color(189, 185, 185),  new Color(255,255,255));
                RenderUtil.verticalGradient(pos.z + 0.5, pos.y + 0.5, 0.5, pos.w - pos.y + 0.5, // Right
                        new Color(189, 185, 185),  new Color(255,255,255));
                RenderUtil.horizontalGradient(pos.x + 0.5, pos.w + 0.5, pos.z - pos.x, 0.5, // Bottom
                        new Color(189, 185, 185),  new Color(255,255,255));


                RenderUtil.verticalGradient(pos.x + 0.5-4, (pos.y + 0.5)+((pos.w - pos.y + 0.5) - ((pos.w - pos.y + 0.5) * (healthValue))), 1,  (pos.w - pos.y + 0.5) *(healthValue), //血条Left
                        healthColor,  healthColor);

            };

            runnable.run();
            if (this.glow.getValue()) {
                NORMAL_POST_BLOOM_RUNNABLES.add(runnable);
            }
        }
    };
}
