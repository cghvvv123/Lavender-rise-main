package net.minecraft.block;

import com.alan.clients.Client;
import com.alan.clients.module.impl.render.XRay;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumWorldBlockLayer;

import java.util.Random;

public class BlockGlass extends BlockBreakable {
    public BlockGlass(final Material materialIn, final boolean ignoreSimilarity) {
        super(materialIn, ignoreSimilarity);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(final Random random) {
        return 0;
    }

    public EnumWorldBlockLayer getBlockLayer() {
        XRay xRay = Client.INSTANCE.getModuleManager().get(XRay.class);
        return xRay.isEnabled() ? EnumWorldBlockLayer.TRANSLUCENT : EnumWorldBlockLayer.CUTOUT;
        //return EnumWorldBlockLayer.CUTOUT;
    }

    public boolean isFullCube() {
        return false;
    }

    protected boolean canSilkHarvest() {
        return true;
    }
}
