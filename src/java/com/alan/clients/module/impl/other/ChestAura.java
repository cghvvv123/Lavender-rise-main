package com.alan.clients.module.impl.other;

import com.alan.clients.api.Rise;
import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.util.render.GLUtil;
import com.alan.clients.util.rotation.RotationUtil;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.impl.ListValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author Lynn
 * @since 04/28/2024
 */
@Rise
@ModuleInfo(name = "ChestAura", description = "Opens nearby chests for you", category = Category.PLAYER)
public final class ChestAura extends Module {
    private final NumberValue rangeValue = new NumberValue("Range",this,  5F,1F, 4, 0.1F);
    private final ListValue<MovementFix> movementCorrection = new ListValue<>("Movement correction", this);
    public static List<BlockPos> openedChests = new ArrayList<>();
    public static List<BlockPos> ignoredChests = new ArrayList<>();
    private final Map<BlockPos, EnumFacing> openSideCache = new HashMap<>();
    private float targetYaw, targetPitch;

    public ChestAura() {
        for (MovementFix movementFix : MovementFix.values()) {
            movementCorrection.add(movementFix);
        }
        movementCorrection.setDefault(MovementFix.OFF);
    }
    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.currentScreen instanceof GuiContainer ||mc.thePlayer.isEating() || isAllowed())
            return;
        int range = rangeValue.getValue().intValue();
        Vec3 playerPosition = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    Vec3 currentPos = playerPosition.addVector(dx, dy, dz);
                    BlockPos blockPos = new BlockPos(currentPos);
                    Block targetBlock = mc.theWorld.getBlockState(blockPos).getBlock();
                    final Vector2f rotations = RotationUtil.calculate(new com.alan.clients.util.vector.Vector3d(blockPos.getX(),
                            blockPos.getY(), blockPos.getZ()),EnumFacing.UP);
                    if (targetBlock == Blocks.chest) {
                        final float[] rot = RotationUtil.getRotationsNeededBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        MovingObjectPosition rayTraceResult = mc.theWorld.rayTraceBlocks(playerPosition, currentPos);
                        if (rayTraceResult != null && rayTraceResult.getBlockPos().equals(blockPos) && !openedChests.contains(blockPos)) {
                            if(isAccessibleChestOrFurnace(blockPos)){
                                targetYaw = rotations.x;
                                targetPitch = rotations.y;
                                openChest(blockPos);
                                openedChests.add(blockPos);
                            }else{
                                ignoredChests.add(blockPos);
                            }
                        }
                    }
                }
            }
        }
    };

    public void openChest(BlockPos chestPos) {
        if(isAccessibleChestOrFurnace(chestPos)) {
            EnumFacing openSide = openSideCache.get(chestPos);
            if (openSide == null) {
                List<EnumFacing> possibleDirections = Arrays.asList(EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.UP);
                for(EnumFacing facing : possibleDirections){
                    BlockPos adjBlockPos = chestPos.offset(facing);
                    if(mc.theWorld.getBlockState(adjBlockPos).getBlock() != Blocks.air){
                        continue;
                    }
                    if(!mc.theWorld.getBlockState(adjBlockPos.offset(facing.getOpposite())).getBlock().isFullCube()){
                        openSide = facing;
                        openSideCache.put(chestPos, openSide);
                        break;
                    }
                }
            }
            Vec3 openPos = new Vec3(chestPos.getX() + 0.5D + 0.5D * Objects.requireNonNull(openSide).getFrontOffsetX(),
                    chestPos.getY() + 0.5D + 0.5D * openSide.getFrontOffsetY(),
                    chestPos.getZ() + 0.5D + 0.5D * openSide.getFrontOffsetZ());

            RotationComponent.setRotations(new Vector2f(targetYaw, targetPitch), 10, movementCorrection.getValue());

            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), chestPos, openSide, openPos)) {
                mc.thePlayer.swingItem();
            }
        }
    }


    public boolean isAccessibleChestOrFurnace(BlockPos blockPos){
        boolean isAccessible = true;
        Block block = mc.theWorld.getBlockState(blockPos.up()).getBlock();
        if(!(block == Blocks.air)){
            isAccessible = false;
        }
        if(!isAccessible){
            for (EnumFacing facing : new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST}) {
                BlockPos adjBlockPos = blockPos.offset(facing);
                if(mc.theWorld.getBlockState(adjBlockPos).getBlock() == Blocks.air){
                    isAccessible = true;
                    break;
                }
            }
        }
        return isAccessible;
    }

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        openedChests.clear();
        ignoredChests.clear();
    };

    @Override
    protected void onDisable() {
        openedChests.clear();
        ignoredChests.clear();
    }

    public boolean isAllowed() {
        return getModule(KillAura.class).isEnabled() || getModule(KillAura.class).target != null;
    }


    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {
        if(isNull()) return;
        mc.theWorld.loadedTileEntityList.forEach(entity -> {
            if (entity instanceof TileEntityChest || entity instanceof TileEntityEnderChest) {
                if (ChestAura.mc.thePlayer.getDistance(entity.getPos()) < 20.0) {
                    int color;
                    color = new Color(255, 0, 0).getRGB();
                    if (openedChests.contains(entity.getPos())) {
                        color = new Color(0, 255, 0).getRGB();
                    }
                    final BlockPos blockpos = entity.getPos();
                    GL11.glPushMatrix();
                    GL11.glDisable(2929);
                    GLUtil.startBlend();
                    GL11.glDepthMask(false);
                    GL11.glDisable(3553);
                    GL11.glColor4ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF), (byte) 51);
                    GL11.glTranslated(-ChestAura.mc.getRenderManager().renderPosX, -ChestAura.mc.getRenderManager().renderPosY, -ChestAura.mc.getRenderManager().renderPosZ);
                    RenderGlobal.drawSelectionBoundingBox(entity.getBlockType().getCollisionBoundingBox(ChestAura.mc.theWorld, blockpos, entity.getBlockType().getStateFromMeta(entity.getBlockMetadata())), false, true);
                    GL11.glEnable(2929);
                    GLUtil.endBlend();
                    GL11.glDepthMask(true);
                    GL11.glEnable(3553);

                    GL11.glPopMatrix();
                }
            }
        });
    };

}