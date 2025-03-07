package com.alan.clients.module.impl.render;


import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashSet;


@Rise
@ModuleInfo(name = "module.render.xray.name", description = "Xray", category = Category.RENDER)
public class XRay extends Module {

    public final NumberValue opacity = new NumberValue("Opacity",this, 60, 0, 100, 1);
    public final BooleanValue bypass = new BooleanValue("Anti-xray bypass",this, false);
    public final HashSet<BlockPos> checkedOres = new HashSet<>(), queuedOres = new HashSet<>(), oresToRender = new HashSet<>();
    private final BooleanValue redstone = new BooleanValue("Redstone",this,true);
    private final BooleanValue diamond = new BooleanValue("Diamond",this,true);
    private final BooleanValue emerald = new BooleanValue("Emerald",this,true);
    private final BooleanValue lapis = new BooleanValue("Lapis",this,true);
    private final BooleanValue iron = new BooleanValue("Iron",this,true);
    private final BooleanValue coal = new BooleanValue("Coal",this,true);
    private final BooleanValue gold = new BooleanValue("Gold",this,true);

    private final HashSet<Block> blocks = new HashSet<>(Arrays.asList(Blocks.obsidian, Blocks.clay, Blocks.mossy_cobblestone, Blocks.diamond_ore, Blocks.redstone_ore, Blocks.iron_ore, Blocks.coal_ore, Blocks.gold_ore, Blocks.emerald_ore, Blocks.lapis_ore));
    private World lastWorld;

    public boolean isWhitelisted(Block block) {
        if (this.redstone.getValue() && ( block == Blocks.redstone_ore || block == Blocks.redstone_block))
            return true;
        if (this.diamond.getValue() && ( block == Blocks.diamond_ore || block == Blocks.diamond_block))
            return true;
        if (this.emerald.getValue() && ( block == Blocks.emerald_ore || block == Blocks.emerald_block))
            return true;
        if (this.lapis.getValue() && ( block == Blocks.lapis_ore || block == Blocks.lapis_block))
            return true;
        if (this.iron.getValue() && ( block == Blocks.iron_ore || block == Blocks.iron_block))
            return true;
        if (this.coal.getValue() && ( block == Blocks.coal_ore || block == Blocks.coal_block))
            return true;
        return this.gold.getValue() && (block == Blocks.gold_ore || block == Blocks.gold_block);
    }


    public static boolean isExposed(IBlockAccess worldIn, BlockPos pos, EnumFacing side, double minY, double maxY, double minZ, double maxZ, double minX, double maxX) {
        return side == EnumFacing.DOWN && minY > 0.0D || (side == EnumFacing.UP && maxY < 1.0D || (side == EnumFacing.NORTH && minZ > 0.0D || (side == EnumFacing.SOUTH && maxZ < 1.0D || (side == EnumFacing.WEST && minX > 0.0D || (side == EnumFacing.EAST && maxX < 1.0D || !worldIn.getBlockState(pos).getBlock().isOpaqueCube())))));
    }

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event ->{
        this.setEnabled(false);
    };

    @Override
    public void onEnable() {
        InstanceAccess.mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        InstanceAccess.mc.renderGlobal.loadRenderers();
    }


    public boolean shouldAdd(Block block, BlockPos pos) {
        for (EnumFacing si : EnumFacing.VALUES) {
            if (isExposed(InstanceAccess.mc.theWorld, pos.offset(si), si, block.getBlockBoundsMinY(), block.getBlockBoundsMaxY(), block.getBlockBoundsMinZ(), block.getBlockBoundsMaxZ(), block.getBlockBoundsMinX(), block.getBlockBoundsMaxX())) {
                return false;
            }
        }
        return true;
    }



}
