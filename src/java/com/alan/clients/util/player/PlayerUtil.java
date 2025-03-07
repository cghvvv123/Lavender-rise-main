package com.alan.clients.util.player;

import com.alan.clients.util.BlockInfo;
import com.alan.clients.util.interfaces.InstanceAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAnvilBlock;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is a player util which can be used to do various things related to the player
 *
 * @author Auth
 * @since 21/10/2021
 */
@UtilityClass
public class PlayerUtil implements InstanceAccess {
    public static MovingObjectPosition raytrace(float yaw, float pitch) {
        float partialTicks = mc.timer.renderPartialTicks;
        float blockReachDistance = mc.playerController.getBlockReachDistance();

        Vec3 vec3 = mc.thePlayer.getPositionEyes(partialTicks);

        Vec3 vec31 = mc.thePlayer.getVectorForRotation(pitch, yaw);

        Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);

        return mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
    }
    public static EnumFacing getHorizontalFacing(float yaw) {
        return EnumFacing.getHorizontal(MathHelper.floor_double((double)(yaw * 4.0F / 360.0F) + 0.5D) & 3);
    }
    public static boolean isEntityNearby(Entity entity, double range) {
        if (mc.thePlayer != null && entity != null) {
            return (double)mc.thePlayer.getDistanceToEntity(entity) <= range;
        } else {
            return false;
        }
    }
    public static BlockInfo getBlockUnder(double y, int maxRange) {
        return getBlockInfo(mc.thePlayer.posX, y - 1, mc.thePlayer.posZ, maxRange);
    }
    public static Block getBlockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + offsetX, mc.thePlayer.posY + offsetY, mc.thePlayer.posZ + offsetZ)).getBlock();
    }

    public static BlockPos getBlockCorner(BlockPos start, BlockPos end) {
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                for (int z = 0; z <= 1; ++z) {
                    BlockPos pos = new BlockPos(end.getX() + x, end.getY() + y, end.getZ() + z);
                    if (PlayerUtil.isBlockBetween(start, pos)) continue;
                    return pos;
                }
            }
        }
        return null;
    }
    public static boolean isBlockBetween(BlockPos start, BlockPos end) {
        int startX = start.getX();
        int startY = start.getY();
        int startZ = start.getZ();
        int endX = end.getX();
        int endY = end.getY();
        int endZ = end.getZ();
        double diffX = endX - startX;
        double diffY = endY - startY;
        double diffZ = endZ - startZ;
        double x = startX;
        double y = startY;
        double z = startZ;
        double STEP = 0.1;
        int STEPS = (int)Math.max(Math.abs(diffX), Math.max(Math.abs(diffY), Math.abs(diffZ))) * 4;
        for (int i = 0; i < STEPS - 1; ++i) {
            BlockPos pos;
            Block block;
            if ((x += diffX / (double)STEPS) == (double)endX && (y += diffY / (double)STEPS) == (double)endY && (z += diffZ / (double)STEPS) == (double)endZ || (block = mc.theWorld.getBlockState(pos = new BlockPos(x, y, z)).getBlock()).getMaterial() == Material.air || block.getMaterial() == Material.water || block instanceof BlockVine || block instanceof BlockLadder) continue;
            return true;
        }
        return false;
    }
    public static BlockInfo getBlockInfo(double x, double y, double z, int maxRange) {
        BlockPos pos = new BlockPos(x, y, z);

        // To add the facing of the opposite direction of the player as a priority
        EnumFacing playerDirectionFacing = getHorizontalFacing(MoveUtil.getPlayerDirection()).getOpposite();

        ArrayList<EnumFacing> facingValues = new ArrayList<>();
        facingValues.add(playerDirectionFacing);

        for(EnumFacing facing : EnumFacing.values()) {
            if(facing != playerDirectionFacing && facing != EnumFacing.UP) {
                facingValues.add(facing);
            }
        }

        CopyOnWriteArrayList<BlockPos> aaa = new CopyOnWriteArrayList<>();

        aaa.add(pos);

        int i = 0;

        while(i < maxRange) {
            ArrayList<BlockPos> ccc = new ArrayList<>(aaa);

            if(!aaa.isEmpty()) {
                for(BlockPos bbbb : aaa) {
                    for(EnumFacing facing : facingValues) {
                        BlockPos n = bbbb.offset(facing);

                        if(isAirOrLiquid(n)) {
                            aaa.add(n);
                        } else {
                            return new BlockInfo(n, facing.getOpposite());
                        }
                    }
                }
            }

            //LogUtil.addChatMessage("" + aaa.size());

            for(BlockPos dddd : ccc) {
                aaa.remove(dddd);
            }

            ccc.clear();

            i++;
        }

        return null;
    }
    public static boolean isAirOrLiquid(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();

        return block instanceof BlockAir || block instanceof BlockLiquid;
    }
    public static boolean isBlockBlacklisted(Item item) {
        return item instanceof ItemAnvilBlock || item.getUnlocalizedName().contains("sand") || item.getUnlocalizedName().contains("gravel") || item.getUnlocalizedName().contains("ladder") || item.getUnlocalizedName().contains("tnt") || item.getUnlocalizedName().contains("chest") || item.getUnlocalizedName().contains("web");
    }
    private final HashMap<Integer, Integer> GOOD_POTIONS = new HashMap<Integer, Integer>() {{
        put(6, 1); // Instant Health
        put(10, 2); // Regeneration
        put(11, 3); // Resistance
        put(21, 4); // Health Boost
        put(22, 5); // Absorption
        put(23, 6); // Saturation
        put(5, 7); // Strength
        put(1, 8); // Speed
        put(12, 9); // Fire Resistance
        put(14, 10); // Invisibility
        put(3, 11); // Haste
        put(13, 12); // Water Breathing
    }};

    /**
     * Gets the block at a position
     *
     * @return block
     */
    public Block block(final double x, final double y, final double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    /**
     * Gets the block at a position
     *
     * @return block
     */
    public Block block(final BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }

    /**
     * Gets the distance between 2 positions
     *
     * @return distance
     */
    public double distance(final BlockPos pos1, final BlockPos pos2) {
        final double x = pos1.getX() - pos2.getX();
        final double y = pos1.getY() - pos2.getY();
        final double z = pos1.getZ() - pos2.getZ();
        return x * x + y * y + z * z;
    }

    /**
     * Gets the block relative to the player from the offset
     *
     * @return block relative to the player
     */
    public Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(offsetX, offsetY, offsetZ)).getBlock();
    }

    public Block blockAheadOfPlayer(final double offsetXZ, final double offsetY) {
        return blockRelativeToPlayer(-Math.sin(MoveUtil.direction()) * offsetXZ, offsetY, Math.cos(MoveUtil.direction()) * offsetXZ);
    }

    /**
     * Gets another players' username without any formatting
     *
     * @return players username
     */
    public String name(final EntityPlayer player) {
        return player.getCommandSenderName();
    }

    /**
     * Gets the players' username without any formatting
     *
     * @return players username
     */
    public String name() {
        return mc.thePlayer.getCommandSenderName();
    }

    /**
     * Checks if another players' team is the same as the players' team
     *
     * @return same team
     */
    public boolean sameTeam(final EntityLivingBase player) {
        if (player.getTeam() != null && mc.thePlayer.getTeam() != null) {
            final char c1 = player.getDisplayName().getFormattedText().charAt(1);
            final char c2 = mc.thePlayer.getDisplayName().getFormattedText().charAt(1);
            return c1 == c2;
        }
        return false;
    }

    /**
     * Checks if there is a block under the player
     *
     * @return block under
     */
    public boolean isBlockUnder(final double height) {
        return isBlockUnder(height, true);
    }

    public boolean isBlockUnder(final double height, final boolean boundingBox) {
        if (boundingBox) {
            for (int offset = 0; offset < height; offset += 2) {
                final AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);

                if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                    return true;
                }
            }
        } else {
            for (int offset = 0; offset < height; offset++) {
                if (PlayerUtil.blockRelativeToPlayer(0, -offset, 0).isFullBlock()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBlockUnder() {
        return isBlockUnder(mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
    }

    /**
     * Checks if a potion is good
     *
     * @return good potion
     */
    public boolean goodPotion(final int id) {
        return GOOD_POTIONS.containsKey(id);
    }

    /**
     * Gets a potions ranking
     *
     * @return potion ranking
     */
    public int potionRanking(final int id) {
        return GOOD_POTIONS.getOrDefault(id, -1);
    }

    /**
     * Checks if the player is in a liquid
     *
     * @return in liquid
     */
    public boolean inLiquid() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava();
    }

    /**
     * Fake damages the player
     */
    public void fakeDamage() {
        mc.thePlayer.handleHealthUpdate((byte) 2);
        mc.ingameGUI.healthUpdateCounter = mc.ingameGUI.updateCounter + 20;
    }

    /**
     * Checks if the player is near a block
     *
     * @return block near
     */
    public boolean blockNear(final int range) {
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = blockRelativeToPlayer(x, y, z);

                    if (!(block instanceof BlockAir)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if the player is inside a block
     *
     * @return inside block
     */
    public boolean insideBlock() {
        if (mc.thePlayer.ticksExisted < 5) {
            return false;
        }

        final EntityPlayerSP player = PlayerUtil.mc.thePlayer;
        final WorldClient world = PlayerUtil.mc.theWorld;
        final AxisAlignedBB bb = player.getEntityBoundingBox();
        for (int x = MathHelper.floor_double(bb.minX); x < MathHelper.floor_double(bb.maxX) + 1; ++x) {
            for (int y = MathHelper.floor_double(bb.minY); y < MathHelper.floor_double(bb.maxY) + 1; ++y) {
                for (int z = MathHelper.floor_double(bb.minZ); z < MathHelper.floor_double(bb.maxZ) + 1; ++z) {
                    final Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    final AxisAlignedBB boundingBox;
                    if (block != null && !(block instanceof BlockAir) && (boundingBox = block.getCollisionBoundingBox(world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)))) != null && player.getEntityBoundingBox().intersectsWith(boundingBox)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sends a click to Minecraft legitimately
     */
    public void sendClick(final int button, final boolean state) {
        final int keyBind = button == 0 ? mc.gameSettings.keyBindAttack.getKeyCode() : mc.gameSettings.keyBindUseItem.getKeyCode();

        KeyBinding.setKeyBindState(keyBind, state);

        if (state) {
            KeyBinding.onTick(keyBind);
        }
    }

    public static boolean onLiquid() {
        boolean onLiquid = false;
        final AxisAlignedBB playerBB = PlayerUtil.mc.thePlayer.getEntityBoundingBox();
        final WorldClient world = PlayerUtil.mc.theWorld;
        final int y = (int) playerBB.offset(0.0, -0.01, 0.0).minY;
        for (int x = MathHelper.floor_double(playerBB.minX); x < MathHelper.floor_double(playerBB.maxX) + 1; ++x) {
            for (int z = MathHelper.floor_double(playerBB.minZ); z < MathHelper.floor_double(playerBB.maxZ) + 1; ++z) {
                final Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block != null && !(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLiquid)) {
                        return false;
                    }
                    onLiquid = true;
                }
            }
        }
        return onLiquid;
    }

    public static EnumFacingOffset getEnumFacing(Vec3 position) {
        if (mc.thePlayer == null && mc.theWorld == null) return null;
        for (int x2 = -1; x2 <= 1; x2 += 2) {

            if (!(PlayerUtil.block(position.xCoord + (double)x2, position.yCoord, position.zCoord) instanceof BlockAir)) {
                if (x2 > 0) {
                    return new EnumFacingOffset(EnumFacing.WEST, new Vec3(x2, 0.0, 0.0));
                } else {
                    return new EnumFacingOffset(EnumFacing.EAST, new Vec3(x2, 0.0, 0.0));
                }
            }
        }

        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (!(PlayerUtil.block(position.xCoord, position.yCoord + (double)y2, position.zCoord) instanceof BlockAir)) {
                if (y2 < 0) {
                    return new EnumFacingOffset(EnumFacing.UP, new Vec3(0.0, y2, 0.0));
                }
            }
        }

        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (!(PlayerUtil.block(position.xCoord, position.yCoord, position.zCoord + (double)z2) instanceof BlockAir)) {
                if (z2 < 0) {
                    return new EnumFacingOffset(EnumFacing.SOUTH, new Vec3(0.0, 0.0, z2));
                } else {
                    return new EnumFacingOffset(EnumFacing.NORTH, new Vec3(0.0, 0.0, z2));
                }
            }
        }

        return null;
    }


    // This methods purpose is to get block placement possibilities, blocks are 1 unit thick so please don't change it to 0.5 it causes bugs.
    public Vec3 getPlacePossibility(double offsetX, double offsetY, double offsetZ, int blockRange) {
        final List<Vec3> possibilities = new ArrayList<>();
        final int range = (int) (blockRange + (Math.abs(offsetX) + Math.abs(offsetZ)));

        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = PlayerUtil.blockRelativeToPlayer(x, y, z);

                    if (!(block instanceof BlockAir)) {
                        for (int x2 = -1; x2 <= 1; x2 += 1)
                            possibilities.add(new Vec3(mc.thePlayer.posX + x + x2, mc.thePlayer.posY + y, mc.thePlayer.posZ + z));

                        for (int y2 = -1; y2 <= 1; y2 += 1)
                            possibilities.add(new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y + y2, mc.thePlayer.posZ + z));

                        for (int z2 = -1; z2 <= 1; z2 += 1)
                            possibilities.add(new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z + z2));
                    }
                }
            }
        }

        possibilities.removeIf(vec3 -> mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) > blockRange || !(PlayerUtil.block(vec3.xCoord, vec3.yCoord, vec3.zCoord) instanceof BlockAir));

        if (possibilities.isEmpty()) return null;

        possibilities.sort(Comparator.comparingDouble(vec3 -> {

            final double d0 = (mc.thePlayer.posX + offsetX) - vec3.xCoord;
            final double d1 = (mc.thePlayer.posY - 1 + offsetY) - vec3.yCoord;
            final double d2 = (mc.thePlayer.posZ + offsetZ) - vec3.zCoord;
            return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);

        }));

        return possibilities.get(0);
    }

    public Vec3 getPlacePossibility() {
        return getPlacePossibility(0, 0, 0, 5);
    }

}
