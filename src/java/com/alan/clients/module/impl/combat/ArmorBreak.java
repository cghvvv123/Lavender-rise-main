package com.alan.clients.module.impl.combat;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

@Rise
@ModuleInfo(name = "module.combat.armorbreak.name", description = "ArmorBreak", category = Category.COMBAT)
public final class ArmorBreak extends Module {
    private EntityLivingBase target = null;
    public BooleanValue switchValue = new BooleanValue("Burst", this, true);

    private boolean doCritArmorBreaker = false;

    @EventLink()
    public final Listener<TickEvent> onTick = event -> {
        KillAura aura = getModule(KillAura.class);
        EntityPlayer thePlayer = mc.thePlayer;
        if (target==null || isNull() || thePlayer == null || mc.thePlayer.isDead || mc.thePlayer.getHealth() <= 3) {
            return;
        }

        if (!aura.isEnabled()) return;

        target = (EntityLivingBase) aura.target;
        doCritArmorBreaker = (target instanceof EntityPlayer && ((EntityPlayer) target).isUsingItem() && !mc.gameSettings.keyBindUseItem.isPressed());

        if (switchValue.getValue()) {
            int weaponSlot = findBestWeaponSlot();

            if (target instanceof EntityPlayer && ((EntityPlayer) target).isUsingItem()) {
                if (target.hurtResistantTime <= 1) {
                    mc.thePlayer.inventory.currentItem = 8;
                    mc.playerController.updateController();
                }
                if (target.hurtResistantTime == 9) {
                    int weaponSlot2 = findWorstWeaponSlot();
                    mc.thePlayer.inventory.currentItem = weaponSlot2;
                    mc.playerController.updateController();
                }
                if (target.hurtResistantTime >= 2 && target.hurtResistantTime <= 8) {
                    mc.thePlayer.inventory.currentItem = weaponSlot;
                    mc.playerController.updateController();
                }
            } else {
                if (aura.target != null) {
                    thePlayer.inventory.currentItem = weaponSlot;
                    mc.playerController.updateController();
                }
            }
        } else {
            int weaponSlot = findBestWeaponSlot();
            if (aura.target != null) {
                thePlayer.inventory.currentItem = weaponSlot;
                mc.playerController.updateController();
            }
        }
    };

    private int findBestWeaponSlot() {
        int bestSlot = -1;
        float bestDamage = 0;

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemSword) {
                float damage = EnchantmentHelper.getEnchantmentLevel(16, stack) + ((ItemSword) stack.getItem()).getDamageVsEntity();
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }

    private int findWorstWeaponSlot() {
        int worstSlot = -1;
        float worstDamage = Float.MAX_VALUE;

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemSword) {
                float damage = EnchantmentHelper.getEnchantmentLevel(16, stack) + ((ItemSword) stack.getItem()).getDamageVsEntity();
                if (damage < worstDamage) {
                    worstDamage = damage;
                    worstSlot = i;
                }
            }
        }

        return worstSlot;
    }
}
