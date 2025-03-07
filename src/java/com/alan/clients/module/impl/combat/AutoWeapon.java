package com.alan.clients.module.impl.combat;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.other.AttackEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

@Rise
@ModuleInfo(name = "module.combat.autoweapon.name", description = "auto tool in ItemSword", category = Category.COMBAT)
public final class AutoWeapon extends Module {
    private boolean attackEnemy = false;

    private int spoofedSlot = 0;

    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        if (isNull()) return;
        attackEnemy = true;
    };

    @EventLink()
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        if (isNull()) return;
        if (!(event.getPacket() instanceof C02PacketUseEntity)) {
            return;
        }
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null) return;
        C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
        if (packet.getAction() == C02PacketUseEntity.Action.ATTACK && attackEnemy) {
            attackEnemy = false;
            int bestSlot = -1;
            float maxDamage = -1;
            int axeSlot = -1;
            int toolSlot = -1;
            float maxToolDamage = -1;

            for (int slot = 0; slot < 9; slot++) {
                ItemStack itemStack = thePlayer.inventory.getStackInSlot(slot);
                if (itemStack != null && itemStack.getItem() instanceof ItemAxe) {//hyt秒人斧
                    float damage = ((ItemAxe) itemStack.getItem()).getDamageVsEntity();
                    int enchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack);
                    float totalDamage = damage + 1.25f * enchantLevel;
                    if (totalDamage > 500) {
                        axeSlot = slot;
                        break;
                    }
                } else if (itemStack != null && itemStack.getItem() instanceof ItemSword) {//物品栏伤害最高的剑
                    float damage = ((ItemSword) itemStack.getItem()).getDamageVsEntity();
                    int enchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack);
                    float totalDamage = damage + 1.25f * enchantLevel;
                    if (totalDamage > maxDamage) {
                        bestSlot = slot;
                        maxDamage = totalDamage;
                    }
                }
            }
            if (axeSlot != -1){
                thePlayer.inventory.currentItem = axeSlot;
            } else if (bestSlot != -1 && bestSlot != thePlayer.inventory.currentItem){
                thePlayer.inventory.currentItem = bestSlot;
            } else{
                return;
            }
            Minecraft.getMinecraft().playerController.updateController();

        }
    };

        @EventLink()
        public final Listener<PreUpdateEvent> onUpdateEvent = event -> {
            if (getModule(Scaffold.class).isEnabled()) {
                return;
            }
        if (spoofedSlot > 0) {
            if (spoofedSlot == 1)
                Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C09PacketHeldItemChange(Minecraft.getMinecraft().thePlayer.inventory.currentItem));
            spoofedSlot--;
        }
    };

}
