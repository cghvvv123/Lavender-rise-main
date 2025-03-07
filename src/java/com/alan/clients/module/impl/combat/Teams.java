package com.alan.clients.module.impl.combat;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;

@Rise
@ModuleInfo(name = "Teams",description = "Show someone in your team",category = Category.COMBAT)
public class Teams extends Module {
    private final BooleanValue scoreboardValue = new BooleanValue("ScoreboardTeam",this, true);
    private final BooleanValue colorValue = new BooleanValue("Color",this, true);
    private final BooleanValue gommeSWValue = new BooleanValue("GommeSW",this, false);
    private final BooleanValue armorValue = new BooleanValue("ArmorColor",this, false);

    public boolean isInYourTeam(EntityLivingBase entity) {
        if (mc.thePlayer == null)
            return false;

        if (scoreboardValue.getValue() && mc.thePlayer.getTeam() != null && entity.getTeam() != null &&
                mc.thePlayer.getTeam().isSameTeam(entity.getTeam())) {
            return true;
        }

        if (gommeSWValue.getValue() && mc.thePlayer.getDisplayName() != null && entity.getDisplayName() != null) {
            String targetName = entity.getDisplayName().getFormattedText().replace("§r", "");
            String clientName = mc.thePlayer.getDisplayName().getFormattedText().replace("§r", "");
            if (targetName.startsWith("T") && clientName.startsWith("T")) {
                if (Character.isDigit(targetName.charAt(1)) && Character.isDigit(clientName.charAt(1))) {
                    return targetName.charAt(1) == clientName.charAt(1);
                }
            }
        }

        if (armorValue.getValue()) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            if (mc.thePlayer.inventory.armorInventory[3] != null && entityPlayer.inventory.armorInventory[3] != null) {
                ItemArmor myHead = (ItemArmor) mc.thePlayer.inventory.armorInventory[3].getItem();
                ItemArmor entityHead = (ItemArmor) entityPlayer.inventory.armorInventory[3].getItem();

                if (myHead.getColor(mc.thePlayer.inventory.armorInventory[3]) == entityHead.getColor(entityPlayer.inventory.armorInventory[3])) {
                    return true;
                }
            }
        }

        if (colorValue.getValue() && mc.thePlayer.getDisplayName() != null && entity.getDisplayName() != null) {
            String targetName = entity.getDisplayName().getFormattedText().replace("§r", "");
            String clientName = mc.thePlayer.getDisplayName().getFormattedText().replace("§r", "");
            return targetName.startsWith("§" + clientName.charAt(1));
        }

        return false;
    }
}
