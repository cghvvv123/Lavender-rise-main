package com.alan.clients.module.impl.render;

import com.alan.clients.Client;
import com.alan.clients.fontRender.FontManager;
import com.alan.clients.fontRender.RapeMasterFontManager;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.value.impl.DragValue;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ModuleInfo(name = "module.render.blockrate.name",category = Category.RENDER,description = "CNM")
public class BlockRate extends Module {
    private static final Map<UUID, BlockingData> playerBlockingData = new HashMap<>();
    private final RapeMasterFontManager font = FontManager.arial18;
    private EntityLivingBase target, lastTarget;
    float playerBlockRate = 0.0f, targetBlockRate = 0.0f;
    private final DragValue position = new DragValue("Position", this, new Vector2d(200, 200));
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        Vector2d position = this.position.position;
        target = getModule(KillAura.class).target == null ? lastTarget : (EntityLivingBase) getModule(KillAura.class).target;
        lastTarget = target;
        EntityPlayer player = mc.thePlayer;

        playerBlockRate = player != null ? calculate(player) : 0.0f;

        if (target instanceof EntityPlayer && ((EntityPlayer) target).isUsingItem()) {
            targetBlockRate = calculate((EntityPlayer) target);
        } else {
            targetBlockRate = 0.0f;
        }
        String targetName = target != null ? target.getCommandSenderName() : "None";
        String blockRateText = String.format("Your Block Rate: %.2f%% | Target - %s Block Rate: %.2f%%", playerBlockRate, targetName, targetBlockRate);
        float width = FontManager.arial18.getStringWidth(blockRateText);
        this.position.setScale(new Vector2d(width +5,20));
        final Interface interfaceModule = Client.INSTANCE.getModuleManager().get(Interface.class);
        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                RenderUtil.roundedRectangle(position.x, position.y, width + 5, 20, getTheme().getRound(), interfaceModule.getRENDER3BG2Color());
                font.drawStringWithShadow(blockRateText, position.x + 3, position.y + 7f, new Color(255, 255, 255, 255).getRGB());
        });
        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, position.y, width + 5, 20, getTheme().getRound(), Color.BLACK));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, position.y, width + 5, 20, getTheme().getRound() + 1, getTheme().getDropShadow()));
    };
    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        if (isNull()) return;

        this.target = null;
        this.playerBlockRate = 0.0f;
        this.targetBlockRate = 0.0f;
    };

    public float calculate(EntityPlayer player) {
        UUID playerId = player.getUniqueID();
        BlockingData data = playerBlockingData.computeIfAbsent(playerId, k -> new BlockingData());

        data.update(player.isBlocking());

        return data.calculate();
    }
    @Getter
    @Setter
    private static class BlockingData {
        private int blockToggleCount = 0;
        private long lastBlockTime = 0;
        private long totalBlockTime = 0;
        private boolean isBlocking = false;

        void update(boolean blockState) {
            long currentTime = System.currentTimeMillis();
            if (blockState != isBlocking) {
                isBlocking = blockState;
                blockToggleCount++;
                if (isBlocking) {
                    lastBlockTime = currentTime;
                } else {
                    totalBlockTime += currentTime - lastBlockTime;
                }
            }
        }

        float calculate() {
            if (blockToggleCount == 0) return 0.0f;
            float efficiencyFactor = (float) totalBlockTime / blockToggleCount;
            return Math.min(100.0f, efficiencyFactor);
        }
    }
}
