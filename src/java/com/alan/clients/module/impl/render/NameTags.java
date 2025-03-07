package com.alan.clients.module.impl.render;

import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.component.impl.render.ProjectionComponent;
import com.alan.clients.fontRender.FontManager;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.module.impl.combat.Teams;
import com.alan.clients.module.impl.other.AntiCheat;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.WeaponDetection;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.SubMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import javax.vecmath.Vector4d;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Alan
 * @since 29/04/2022
 */
@Rise
@ModuleInfo(name = "module.render.nametags.name", description = "module.render.nametags.description", category = Category.RENDER)
public final class NameTags extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Rise"))
            .add(new SubMode("Jello"))
            .add(new SubMode("New"))
            .setDefault("Rise");
    private final BooleanValue health = new BooleanValue("Show Health", this, true);
    // Show health option doesn't work until we come up with a design that looks good without the health
    // To be honest I don't care alan
    private static final NumberFormat df = new DecimalFormat("0.0");
    private final List<Entity> entityPosition = new CopyOnWriteArrayList<>();
    @EventLink()
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        entityPosition.clear();
        entityPosition.addAll(InstanceAccess.mc.theWorld.loadedEntityList);
    };

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (isNull()) return;
        final Interface interfaceModule = Client.INSTANCE.getModuleManager().get(Interface.class);
        for (Entity entity : entityPosition) {
            if (entity instanceof EntityLivingBase) {

                EntityLivingBase renderingEntity = (EntityLivingBase) entity;
                renderingEntity.renderNameTag = false;
                if (renderingEntity instanceof EntityAnimal || renderingEntity instanceof EntityArmorStand || renderingEntity instanceof EntityMob || renderingEntity instanceof EntityVillager || renderingEntity instanceof EntityBat || renderingEntity instanceof EntitySlime || renderingEntity instanceof EntityDragon || renderingEntity instanceof EntitySquid) {
                    break;
                }

                Vector4d position = ProjectionComponent.get(renderingEntity);

                if (position == null) {
                    continue;
                }
                if (entity == InstanceAccess.mc.thePlayer && InstanceAccess.mc.gameSettings.thirdPersonView == 0) {
                    continue;
                }
                switch (this.mode.getValue().getName()) {
                    case "Rise": {
                        final String text = entity.getCommandSenderName();
                        String str = String.format("%s %s", text, df.format(((EntityLivingBase) entity).getHealth()));

                        final double nameWidth = (this.health.getValue() ? FontManager.arial20.getStringWidth(str) : FontManager.arial20.getStringWidth(text));
                        final double posX = (position.x + (position.z - position.x) / 2);
                        final double posY = position.y - 2;
                        final double margin = 2;

                        final int multiplier = 2;
                        final double nH = FontManager.arial20.getHeight() + margin * multiplier;
                        final double nY = posY - nH;
                        final double nx = posX - margin - nameWidth / 2;
                        final double nw = nameWidth + margin * multiplier;

                        InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                            RenderUtil.roundedRectangle(nx, nY, nw, nH, getTheme().getRound(), getTheme().getDropShadow());
                        });

                        InstanceAccess.NORMAL_RENDER_RUNNABLES.add(() -> {
                            RenderUtil.roundedRectangle(nx, nY, nw, nH, getTheme().getRound(), getTheme().getBackgroundShade());


                            if (this.health.getValue()) {
                                FontManager.arial20.drawCenteredString(str, (float) posX, (float) (nY + margin * 2) + 1, getTheme().getFirstColor().getRGB());
                            } else {
                                FontManager.arial20.drawCenteredString(text, (float) posX, (float) (nY + margin * 2) + 1, getTheme().getFirstColor().getRGB());
                            }
                        });

                        InstanceAccess.NORMAL_BLUR_RUNNABLES.add(() -> {
                            RenderUtil.roundedRectangle(nx, nY, nw, nH, getTheme().getRound(), Color.BLACK);
                        });
                        break;
                    }
                    case "Jello": {
                        AntiCheat antiCheat = Client.INSTANCE.getModuleManager().get(AntiCheat.class);
                        String hackname = renderingEntity == InstanceAccess.mc.thePlayer ? EnumChatFormatting.AQUA + "[" + Client.name + "] " + EnumChatFormatting.GREEN + "[" + Client.location + "人] " + "§f" + renderingEntity.getCommandSenderName() : renderingEntity.getCommandSenderName();
                        boolean isCheater = antiCheat != null && antiCheat.isCheater(hackname);
                        if (isCheater) {
                            hackname = EnumChatFormatting.RED + "[Hacker] " + "§f" + hackname;
                        }
                        List<String> ranks = new ArrayList<>();
                        if (renderingEntity == Client.INSTANCE.getModuleManager().get(KillAura.class).target) {
                            ranks.add("§4[Target]");
                        }
                        if (WeaponDetection.isStrength((EntityPlayer) renderingEntity) > 0) {
                            ranks.add("§4[Strength]");
                        }
                        if (WeaponDetection.isRegen((EntityPlayer) renderingEntity) > 0) {
                            ranks.add("§4[Regen]");
                        }
                        if (WeaponDetection.isHoldingGodAxe((EntityPlayer) renderingEntity)) {
                            ranks.add("§4[GodAxe]");
                        }
                        if (WeaponDetection.isKBBall(renderingEntity.getHeldItem())) {
                            ranks.add("§4[KBBall]");
                        }
                        if (WeaponDetection.hasEatenGoldenApple((EntityPlayer) renderingEntity) > 0) {
                            ranks.add("§4[GApple]");
                        }
                        String rank = String.join(" ", ranks);
                        final String text = rank + "§f" + hackname;
                        String str = String.format("Health: " + "%s", df.format(renderingEntity.getHealth()));
                        double healthPercent = (double) ((EntityLivingBase) entity).getHealth() / renderingEntity.getMaxHealth();
                        double nameWidth = FontManager.arial19.getStringWidth(text);
                        double strWidth = FontManager.arial14.getStringWidth(str);
                        double margin = 2;
                        double totalWidth = Math.max(nameWidth, strWidth) + 2 * margin;
                        double posX = (position.x + (position.z - position.x) / 2) - totalWidth / 2;
                        double nY = position.y - FontManager.arial19.getHeight() - 4 - 2 * margin;
                        double nH = FontManager.arial20.getHeight() + FontManager.arial14.getHeight() - 1;
                        if (healthPercent > 1) {
                            healthPercent = 1F;
                        }
                        InstanceAccess.NORMAL_RENDER_RUNNABLES.add(() -> {
                            RenderUtil.rectangle(posX, nY, totalWidth, nH, new Color(0, 0, 0, 100));
                            FontManager.arial19.drawString(text, (float) (posX + margin), (float) (nY + margin) + 1.35f, Color.WHITE.getRGB());
                            FontManager.arial14.drawString(str, (float) (posX + margin), (float) (nY + nH - FontManager.arial14.getHeight()) + 2.66f, Color.WHITE.getRGB());
                        });
                        InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                            RenderUtil.rectangle(posX, nY, totalWidth, nH, getTheme().getDropShadow());
                        });
                        InstanceAccess.NORMAL_BLUR_RUNNABLES.add(() -> {
                            RenderUtil.rectangle(posX, nY, totalWidth, nH, Color.BLACK);
                        });
                        RenderUtil.rectangle(posX, nY + nH + 0.3, totalWidth, margin - 0.5f, new Color(20, 20, 20, 140));
                        RenderUtil.rectangle(posX, nY + nH + 0.3, totalWidth * healthPercent, margin - 0.5f, getModule(Teams.class).isInYourTeam(renderingEntity) ? new Color(179, 255, 179, 204) : new Color(255, 255, 255, 140));
                        break;
                    }
                    case "New": {
                        AntiCheat antiCheat = Client.INSTANCE.getModuleManager().get(AntiCheat.class);
                        float healthValue = ((EntityLivingBase) entity).getHealth() / renderingEntity.getMaxHealth();
                        String healthColor = healthValue > .75 ? "§a" : healthValue > .5 ? "§e" : healthValue > .35 ? "§c" : "§4";
                        String hackname = renderingEntity == InstanceAccess.mc.thePlayer ? EnumChatFormatting.AQUA + "[" + Client.name + "] " + EnumChatFormatting.GREEN + "[" + Client.location + "人] " + "§f" + renderingEntity.getCommandSenderName() : renderingEntity.getCommandSenderName();
                        boolean isCheater = antiCheat != null && antiCheat.isCheater(hackname);
                        if (isCheater) {
                            hackname = EnumChatFormatting.RED + "[Hacker] " + "§f" + hackname;
                        }
                        List<String> ranks = new ArrayList<>();
                        if (renderingEntity == Client.INSTANCE.getModuleManager().get(KillAura.class).target) {
                            ranks.add("§4[Target]");
                        }
                        if (WeaponDetection.isStrength((EntityPlayer) renderingEntity) > 0) {
                            ranks.add("§4[Strength]");
                        }
                        if (WeaponDetection.isRegen((EntityPlayer) renderingEntity) > 0) {
                            ranks.add("§4[Regen]");
                        }
                        if (WeaponDetection.isHoldingGodAxe((EntityPlayer) renderingEntity)) {
                            ranks.add("§4[GodAxe]");
                        }
                        if (WeaponDetection.isKBBall(renderingEntity.getHeldItem())) {
                            ranks.add("§4[KBBall]");
                        }
                        if (WeaponDetection.hasEatenGoldenApple((EntityPlayer) renderingEntity) > 0) {
                            ranks.add("§4[GApple]");
                        }
                        String rank = String.join(" ", ranks);
                        final String text = rank + "§f" + hackname + String.format(" §7["+healthColor+"%s HP§7]", df.format(renderingEntity.getHealth()));

                        double nameWidth = FontManager.arial19.getStringWidth(text);
                        double margin = 4;
                        double totalWidth = (nameWidth) + 2 * margin;
                        double posX = (position.x + (position.z - position.x) / 2) - totalWidth / 2;
                        double nY = (position.y - FontManager.arial19.getHeight() - 4 - 2 * margin);
                        double nH = FontManager.arial19.getHeight()+4;

                        InstanceAccess.NORMAL_RENDER_RUNNABLES.add(() -> {
                            RenderUtil.roundedRectangle(posX, nY+2, totalWidth, nH, getTheme().getRound(),interfaceModule.getRENDER3BG2Color());
                            FontManager.arial19.drawString(text, (float) (posX + margin), (float) (nY + margin) + 3.35f, Color.WHITE.getRGB());
                        });
                        InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                            RenderUtil.roundedRectangle(posX, nY+2, totalWidth, nH, getTheme().getRound(),getTheme().getDropShadow());
                        });
                        InstanceAccess.NORMAL_BLUR_RUNNABLES.add(() -> {
                            RenderUtil.roundedRectangle(posX, nY+2, totalWidth, nH, getTheme().getRound(),Color.BLACK);
                        });
                        break;
                    }
                }
            }
        }
    };
}
