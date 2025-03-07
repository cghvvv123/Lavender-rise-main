package com.alan.clients.module.impl.player;

import com.alan.clients.api.Rise;
import com.alan.clients.component.impl.render.SilentRender;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.newevent.impl.render.RenderContainerEvent;
import com.alan.clients.util.animations.Animation;
import com.alan.clients.util.animations.Easing;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.BoundsNumberValue;
import com.alan.clients.value.impl.ListValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

import static org.lwjgl.opengl.GL11.*;

@Rise
@ModuleInfo(name = "module.player.stealer.name", description = "module.player.stealer.description", category = Category.PLAYER)
public class Stealer extends Module {
    private int closeTick = -1;

    public final BooleanValue silent = new BooleanValue("SilentOpen", this, true);
    private final BooleanValue filter = new BooleanValue("Filter", this, true);
    private final BooleanValue autoClose = new BooleanValue("Autoclose", this, true);
    private final BooleanValue guiDetect = new BooleanValue("Gui detect", this, true);
    private final BooleanValue packet = new BooleanValue("Packet", this, true);
    private final ListValue<SilentRender> silentRender = new ListValue<>("SilentRender", this, () -> !silent.getValue());
    private final BoundsNumberValue delay = new BoundsNumberValue("Delay", this, 1, 0, 0, 10, 1);
    public final NumberValue ChestHudx = new NumberValue("Chest Hud x", this, 1, -500, 500, 1);
    public final NumberValue ChestHudy = new NumberValue("Chest Hud y", this, 1, -500, 500, 1);
    private int counter;
    private boolean taking = false;
    private final Animation scaleAnim = new Animation(Easing.EASE_IN_EXPO, 100L);
    private int ticks = 2;
    private GuiChest stored;
    private GuiFurnace Furnace;
    private BlockPos flag;
    private Manager invManager;
    public boolean stealer = false;
    private float startAngle = 0.0F;
    public Stealer() {
        for (SilentRender silentrender : SilentRender.values()) {
            silentRender.add(silentrender);
        }
        silentRender.setDefault(SilentRender.OFF);
    }
    @Override
    protected void onEnable() {
        ticks = 2;
        invManager = getModule(Manager.class);
        super.onEnable();
    }

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (isNull()) return;
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement && mc.theWorld.getBlockState(((C08PacketPlayerBlockPlacement) event.getPacket()).getPosition()).getBlock() instanceof BlockChest) {
            this.flag = ((C08PacketPlayerBlockPlacement) event.getPacket()).getPosition();
        }
    };

    @EventLink
    public final Listener<TickEvent> onTick = event -> {
        if (isNull()) return;
        closeTick--;

        if (closeTick == 0) {
            closeTick = -1;
            mc.thePlayer.closeScreen();
        }

    };

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (isNull()) return;
        if (mc.thePlayer.openContainer instanceof ContainerChest && (!isGUI() || !guiDetect.getValue())) {
            ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
            if (!packet.getValue()) {
                for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
                    taking = true;
                    ItemStack stack = container.getLowerChestInventory().getStackInSlot(i);
                    if (stack != null && !isUseless(stack)) {
                        taking = true;
                        if (++counter > delay.getValue().doubleValue()) {
                            taking = true;
                            mc.playerController.windowClick(container.windowId, i, 1, 1, mc.thePlayer);
                            counter = 0;
                            if (stack.stackSize >= stack.getMaxStackSize()) {
                                return;
                            }
                            if (container.getLowerChestInventory().getStackInSlot(i) == null) {
                                return;
                            }
                            boolean chestIsEmpty = true;
                            for (int j = 0; j < container.getLowerChestInventory().getSizeInventory(); j++) {
                                ItemStack remainingStack = container.getLowerChestInventory().getStackInSlot(j);
                                if (remainingStack != null) {
                                    chestIsEmpty = false;
                                    break;
                                }
                            }
                            if (chestIsEmpty) {
                                return;
                            }
                        }
                    }

                }
            }
            if (packet.getValue()) {
                if (ticks > 0) {
                    ticks--;
                    return;
                }
                ticks = 2;
                for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
                    stealer =true;
                    mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                }
                if (isChestEmpty(container)) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).thenRun(() -> {
                        mc.thePlayer.closeScreen();
                        stealer = false;
                    });
                }
            }
            if (autoClose.getValue() && isChestEmpty(container)) {
                closeTick = 2;
            }
        }
        if (mc.thePlayer.openContainer instanceof ContainerBrewingStand) {
            ContainerBrewingStand container = (ContainerBrewingStand) mc.thePlayer.openContainer;
            if (!packet.getValue()) {
                for (int i = 0; i < container.tileBrewingStand.getSizeInventory(); i++) {
                    ItemStack stack = container.tileBrewingStand.getStackInSlot(i);
                    if (stack != null && !isUseless(stack)) {
                        taking = true;
                        if (++counter > delay.getValue().doubleValue()) {
                            taking = true;
                            mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                            counter = 0;
                            if (isBrewingStandEmpty(container)) {
                                return;
                            }
                        }
                    }
                }
            }
            if (packet.getValue()) {
                if (ticks > 0) {
                    ticks--;
                    return;
                }
                ticks = 2;
                for (int i = 0; i < container.tileBrewingStand.getSizeInventory(); i++) {
                    stealer = true;
                    mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                }
                if (isBrewingStandEmpty(container)) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).thenRun(() -> {
                        mc.thePlayer.closeScreen();
                        stealer = false;
                    });
                }
                if (isBrewingStandEmpty(container) && closeTick != -1) {
                    closeTick = 2;
                }
            }
            if (autoClose.getValue() && isBrewingStandEmpty(container)) {
                closeTick = 2;
            }
        }
        if (mc.thePlayer.openContainer instanceof ContainerFurnace) {
            ContainerFurnace container = (ContainerFurnace) mc.thePlayer.openContainer;
            if (!packet.getValue()) {
                for (int i = 0; i < container.tileFurnace.getSizeInventory(); i++) {
                    ItemStack stack = container.tileFurnace.getStackInSlot(i);
                    if (stack != null && !isUseless(stack)) {
                        taking = true;
                        if (++counter > delay.getValue().doubleValue()) {
                            taking = true;
                            mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                            counter = 0;
                            if (isFurnaceEmpty(container)) {
                                return;
                            }
                        }
                    }
                }
            }
            if (packet.getValue()) {
                if (ticks > 0) {
                    ticks--;
                    return;
                }
                ticks = 2;
                for (int i = 0; i < container.tileFurnace.getSizeInventory(); i++) {
                    stealer = true;
                    mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                }
                if (isFurnaceEmpty(container)) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).thenRun(() -> {
                        mc.thePlayer.closeScreen();
                        stealer = false;
                    });
                }
                if (isFurnaceEmpty(container) && closeTick != -1) {
                    closeTick = 2;
                }
            }
            if (autoClose.getValue() && isFurnaceEmpty(container)) {
                closeTick = 2;
            }
        }
    };

    @EventLink
    public final Listener<RenderContainerEvent> onRenderContainer = event -> {
        if ((event.getContainer() instanceof GuiChest) && silent.getValue()) {
            event.setCancelled();
            stored = (GuiChest) event.getContainer();
        }
        if ((event.getContainer() instanceof GuiFurnace) && silent.getValue()) {
            event.setCancelled();
            Furnace = (GuiFurnace) event.getContainer();
        }
    };

    @EventLink
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (mc.currentScreen instanceof GuiFurnace && Furnace != null && flag != null) {
            scaleAnim.run(1f);
            drawChest3();
        }
        if (silent.getValue() && silentRender.getValue() == SilentRender.Lavender && flag != null) {
            if (mc.currentScreen instanceof GuiChest && stored != null) {
                scaleAnim.run(1f);
                drawChest(stored.lowerChestInventory);
            }
        }
        if (silent.getValue() && silentRender.getValue() == SilentRender.New && flag != null) {
            if ((mc.currentScreen instanceof GuiChest) && stored != null) {
                scaleAnim.run(1f);
                drawChest3();
            }
        }
    };

    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        if(!(silentRender.getValue() == SilentRender.Lavender2)) return;
        if (silent.getValue() && flag != null) {
            if ((mc.currentScreen instanceof GuiChest) && stored != null) {
                scaleAnim.run(1f);
                drawChest2(flag, stored.lowerChestInventory);
            } else {
                scaleAnim.run(0f);
                if (stored != null) {
                    drawChest2(flag, stored.lowerChestInventory);
                }
            }
        }
    };
    private boolean isBrewingStandEmpty(ContainerBrewingStand c) {
        for (int i = 0; i < c.tileBrewingStand.getSizeInventory(); ++i) {
            if (c.tileBrewingStand.getStackInSlot(i) != null) {
                return false;
            }
        }
        return true;
    }
    private boolean isFurnaceEmpty(ContainerFurnace c) {
        for (int i = 0; i < c.tileFurnace.getSizeInventory(); ++i) {
            if (c.tileFurnace.getStackInSlot(i) != null) {
                return false;
            }
        }
        return true;
    }

    public void drawChest(IInventory chest) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaled = new ScaledResolution(mc);
        int startX = scaled.getScaledWidth() / 2 - 80;
        int startY = (scaled.getScaledHeight() / 2 - chest.getSizeInventory() / 2 * 20) + 280;
        float width = 187;
        float height = 76f + chest.getSizeInventory() / 9f * 1.5f;
        float ChestHudX = ChestHudx.getValue().intValue();
        float ChestHudY = ChestHudy.getValue().intValue();
        float x = (startX - 6 + ChestHudX);
        float y = startY - 18 + ChestHudY;
        GL11.glScaled(scaleAnim.getValue(), scaleAnim.getValue(), scaleAnim.getValue());
        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            ScaledResolution scaledResolution = new ScaledResolution(mc);
            int screenWidth = scaledResolution.getScaledWidth();
            int screenHeight = scaledResolution.getScaledHeight();
            String text = "Stealing...";

            RenderUtil.roundedRectangle(x-3, y+33, width, height, getTheme().getRound(), getTheme().getBackgroundShade());
            nunitoNormal.drawStringWithShadow(text, x + 4, y + 40, getTheme().getFirstColor().getRGB());

            startAngle += 0.8;
            RenderUtil.drawPartialCircle(screenWidth / 2, (screenHeight / 2)+22,
                    10, startAngle, 170, 1.7F, new Color(255, 255, 255, 200));
            //nunitoLarge.drawStringWithShadow(text, screenWidth / 2F - nunitoLarge.width(text) / 2F + 2F, screenHeight / 2F + nunitoLarge.height() + 8F, new Color(255, 255, 255, 255).getRGB());
            //nunitoLarge.drawStringWithShadow(text, x + 48, y + 40, Color.GREEN.getRGB());


            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();
            for (int i = 0; i < chest.getSizeInventory(); i++) {
                ItemStack item = chest.getStackInSlot(i);
                if (item != null) {
                    int x2 = (int) ((startX + i % 9 * 20) + ChestHudX);
                    int y2 = (int) ((startY + i / 9 * 20) + 33.5f + ChestHudY);
                    mc.getRenderItem().renderItemIntoGUI(item, x2, y2);
                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, item, x2, y2, null);
                }
            }
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableDepth();
        });
        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(x-3, y+33, width, height, getTheme().getRound(), Color.BLACK));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(x-3, y+33, width, height, getTheme().getRound(), getTheme().getDropShadow()));
    }

    public void drawChest2(final BlockPos blockPos, IInventory chest) {
        Minecraft mc = Minecraft.getMinecraft();
        final RenderManager renderManager = mc.getRenderManager();
        final double posX = (blockPos.getX() + 0.5) - renderManager.renderPosX;
        final double posY = blockPos.getY() - renderManager.renderPosY;
        final double posZ = (blockPos.getZ() + 0.5) - renderManager.renderPosZ;
        GL11.glPushMatrix();
        GL11.glTranslated(posX, posY, posZ);
        GL11.glRotated(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
        GL11.glScaled(-0.1D, -0.1D, 0.1D);
        GL11.glScaled(scaleAnim.getValue(), scaleAnim.getValue(), scaleAnim.getValue());
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        GL11.glDepthMask(true);
        // Draw
        float width = 14;
        float height = 0.5f + chest.getSizeInventory() / 9f * 1.5f;
        float x = -7;
        float y = -10 - height;
        StencilUtil.initStencil();
        StencilUtil.bindWriteStencilBuffer();
        RenderUtil.roundedRectangle(x, y, width, height, 1.4, new Color(255, 255, 255, 180));
        StencilUtil.bindReadStencilBuffer(1);
        RenderUtil.rectangle(x, y, width, 0.6, new Color(180, 0, 0, 180));
        y += 0.5f;
        RenderUtil.rectangle(x, y, width, height - 0.5f, new Color(0
                , 0, 0));
        StencilUtil.uninitStencilBuffer();
        RenderItem renderItem = mc.getRenderItem();
        glEnable(GL_TEXTURE_2D);
        GL11.glPushMatrix();
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            ItemStack item = chest.getStackInSlot(i);

            if (item != null) {
                GL11.glPushMatrix();
                glTranslatef(x + 1f, y + 0.75f, 0);
                glScalef(14f / 9f, 14f / 9f, 0);
                renderItem.directRenderItemIntoGUI(item);
                GL11.glPopMatrix();
            }

            if ((i + 1) % 9 == 0) {
                x = -7;
                y += 1.5f;
            } else {
                x += 1.5f;
            }
        }
        GL11.glPopMatrix();


        // Stop render
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        GL11.glPopMatrix();
    }
    public void drawChest3() {
        Minecraft mc = Minecraft.getMinecraft();

        GL11.glScaled(scaleAnim.getValue(), scaleAnim.getValue(), scaleAnim.getValue());
        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            ScaledResolution scaledResolution = new ScaledResolution(mc);
            int screenWidth = scaledResolution.getScaledWidth();
            int screenHeight = scaledResolution.getScaledHeight();
            String text = "Stealing...";

            nunitoNormal.drawStringWithShadow(text, (screenWidth / 2)-20, (screenHeight / 2)+43, new Color(255, 255, 255, 255).getRGB());

            startAngle += 0.8;
            RenderUtil.drawPartialCircle(screenWidth / 2, (screenHeight / 2)+22,
                    10, startAngle, 360, 1.5F, new Color(40, 35, 35, 220));
            RenderUtil.drawPartialCircle(screenWidth / 2, (screenHeight / 2)+22,
                    10, startAngle, 170, 1.5F, new Color(255, 255, 255, 255));
        });
    }
    private boolean isChestEmpty(ContainerChest container) {
        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
            ItemStack stack = container.getLowerChestInventory().getStackInSlot(i);
            if (stack != null && !isUseless(stack)) {
                return false;
            }
        }

        return true;
    }

    private boolean isUseless(ItemStack stack) {
        if (stack == null) {
            return true;
        }
        if (invManager != null) {
            if (!filter.getValue()) {
                return false;
            }
            return invManager.isUseless(stack);
        } else {
            return false;
        }
    }

    private boolean isGUI() {
        for (double x = mc.thePlayer.posX - 5; x <= mc.thePlayer.posX + 5; x++) {
            for (double y = mc.thePlayer.posY - 5; y <= mc.thePlayer.posY + 5; y++) {
                for (double z = mc.thePlayer.posZ - 5; z <= mc.thePlayer.posZ + 5; z++) {

                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();

                    if (block instanceof BlockChest || block instanceof BlockEnderChest) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
