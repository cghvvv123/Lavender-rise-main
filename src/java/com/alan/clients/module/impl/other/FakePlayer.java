package com.alan.clients.module.impl.other;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import net.minecraft.client.entity.EntityOtherPlayerMP;

import java.util.LinkedList;
@ModuleInfo(name = "module.other.fakeplayer.name", category = Category.OTHER,description = "For Test")
public class FakePlayer extends Module {
    private EntityOtherPlayerMP fakePlayer = null;
    private final LinkedList<double[]> positions = new LinkedList<>();

    @Override
    protected void onEnable() {
        if (FakePlayer.mc.thePlayer == null) {
            return;
        }
        (this.fakePlayer = new EntityOtherPlayerMP(FakePlayer.mc.theWorld, FakePlayer.mc.thePlayer.getGameProfile())).clonePlayer(FakePlayer.mc.thePlayer, true);
        this.fakePlayer.copyLocationAndAnglesFrom(FakePlayer.mc.thePlayer);
        this.fakePlayer.rotationYawHead = FakePlayer.mc.thePlayer.rotationYawHead;
        FakePlayer.mc.theWorld.addEntityToWorld(-1337, this.fakePlayer);
        synchronized (this.positions) {
            this.positions.add(new double[] { FakePlayer.mc.thePlayer.posX, FakePlayer.mc.thePlayer.getEntityBoundingBox().minY + FakePlayer.mc.thePlayer.getEyeHeight() / 2.0f, FakePlayer.mc.thePlayer.posZ });
            this.positions.add(new double[] { FakePlayer.mc.thePlayer.posX, FakePlayer.mc.thePlayer.getEntityBoundingBox().minY, FakePlayer.mc.thePlayer.posZ });
        }
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        if (FakePlayer.mc.thePlayer == null) {
            return;
        }
        if (this.fakePlayer != null) {
            FakePlayer.mc.theWorld.removeEntityFromWorld(this.fakePlayer.getEntityId());
            this.fakePlayer = null;
        }
        super.onDisable();
    }
}
