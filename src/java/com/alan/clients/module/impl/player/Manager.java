package com.alan.clients.module.impl.player;

import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.component.impl.player.SelectorDetectionComponent;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.module.impl.movement.InventoryMove;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PostMotionEvent;
import com.alan.clients.newevent.impl.other.AttackEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.ItemUtil;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import util.time.StopWatch;

@Rise
@ModuleInfo(name = "module.player.manager.name", description = "module.player.manager.description", category = Category.PLAYER)
public class Manager extends Module {
    private ItemStack helmet, chestplate, leggings, boots;

    private ItemStack weapon, pickaxe, axe, shovel;
    private final BooleanValue legit = new BooleanValue("Legit", this, false);
    private final BooleanValue Dis = new BooleanValue("AutoDis in Scaffold", this, false);
    private final NumberValue swordSlot = new NumberValue("Sword Slot", this, 1, 1, 9, 1);
    private final NumberValue pickaxeSlot = new NumberValue("Pickaxe Slot", this, 2, 1, 9, 1);
    private final NumberValue axeSlot = new NumberValue("Axe Slot", this, 3, 1, 9, 1);
    private final NumberValue shovelSlot = new NumberValue("Shovel Slot", this, 4, 1, 9, 1);
    private final NumberValue blockSlot = new NumberValue("Block Slot", this, 5, 1, 9, 1);
    private final NumberValue potionSlot = new NumberValue("Bow Slot", this, 6, 1, 9, 1);
    private final NumberValue foodSlot = new NumberValue("Apple Slot", this, 9, 1, 9, 1);
    private final NumberValue throwablesSlot = new NumberValue("Throwables Slot", this, 8, 1, 9, 1);
    private final NumberValue pearlSlot = new NumberValue("Pearl Slot", this,  7, 1, 9, 1);

    private final StopWatch stopwatch = new StopWatch();
    private int chestTicks, attackTicks, placeTicks;
    private boolean moved, open;
    private long nextClick;

    @EventLink()
    public final Listener<AttackEvent> onAttack = event -> this.attackTicks = 0;

    @Override
    protected void onDisable() {
        if (this.canOpenInventory()) {
            this.closeInventory();
        }
    }
    public boolean isGarbage(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.tnt_minecart)
            return false;

        if (item == Items.snowball || item == Items.egg || item == Items.fishing_rod || item == Items.experience_bottle || item == Items.skull || item == Items.flint || item == Items.lava_bucket || item == Items.flint_and_steel || item == Items.string || stack.getItem().getUnlocalizedName().equalsIgnoreCase("item.helmetChain") || stack.getItem().getUnlocalizedName().equalsIgnoreCase("item.leggingsChain")) {
            return true;
        } else if (item instanceof ItemHoe) {
            return true;
        } else if (item instanceof ItemFood && !(item == Items.golden_apple)) {
            return true;
        } else if (item instanceof ItemPotion) {
            ItemPotion potion = (ItemPotion) item;

            for (PotionEffect effect : potion.getEffects(stack)) {
                int id = effect.getPotionID();
                if (id == Potion.moveSlowdown.getId() || id == Potion.blindness.getId() || id == Potion.poison.getId() || id == Potion.digSlowdown.getId() || id == Potion.weakness.getId() || id == Potion.harm.getId()) {
                    return true;
                }
            }
        } else {
            String itemName = stack.getItem().getUnlocalizedName().toLowerCase();

            return itemName.contains("anvil") || itemName.contains("seed") || itemName.contains("table") || itemName.contains("string")
                    || itemName.contains("eye") || itemName.contains("mushroom") || (itemName.contains("chest") && !itemName.contains("plate")) || itemName.contains("pressure_plate");
        }

        return false;
    }

    private void openInventory() {
        if (!this.open) {
            PacketUtil.send(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            this.open = true;
        }
    }

    private void closeInventory() {
        if (this.open) {
            PacketUtil.send(new C0DPacketCloseWindow(InstanceAccess.mc.thePlayer.inventoryContainer.windowId));
            this.open = false;
        }
    }

    private boolean canOpenInventory() {
        return this.getModule(InventoryMove.class).isEnabled() && !(InstanceAccess.mc.currentScreen instanceof GuiInventory);
    }

    private void throwItem(final int slot) {
        if ((!this.moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot)) {
            if (this.canOpenInventory()) {
                this.openInventory();
            }
            InstanceAccess.mc.playerController.windowClick(InstanceAccess.mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 1, 4, InstanceAccess.mc.thePlayer);
            this.moved = true;
        }
    }


    private void moveItem(final int slot, final int destination) {
        if ((!this.moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot)) {
            if (this.canOpenInventory()) {
                this.openInventory();
            }
            InstanceAccess.mc.playerController.windowClick(InstanceAccess.mc.thePlayer.inventoryContainer.windowId, this.slot(slot), this.slot(destination), 2, InstanceAccess.mc.thePlayer);
            this.moved = true;
        }
    }

    private void equipItem(final int slot) {
        if ((!this.moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot)) {
            if (this.canOpenInventory()) {
                this.openInventory();
            }
            InstanceAccess.mc.playerController.windowClick(InstanceAccess.mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 0, 1, InstanceAccess.mc.thePlayer);
            this.moved = true;
        }
    }

    private float damage(final ItemStack stack) {
        final ItemSword sword = (ItemSword) stack.getItem();
        final int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
        return (float) (sword.getDamageVsEntity() + level * 1.25);
    }
    private float bowdamage(final ItemStack stack) {
        final ItemBow bow = (ItemBow) stack.getItem();
        final int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
        return (float) (bow.getMaxDamage() + level * 1.25);
    }
    private float mineSpeed(final ItemStack stack) {
        final Item item = stack.getItem();
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);

        switch (level) {
            case 1:
                level = 30;
                break;

            case 2:
                level = 69;
                break;

            case 3:
                level = 120;
                break;

            case 4:
                level = 186;
                break;

            case 5:
                level = 271;
                break;

            default:
                level = 0;
                break;
        }

        if (item instanceof ItemPickaxe) {
            final ItemPickaxe pickaxe = (ItemPickaxe) item;
            return pickaxe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemSpade) {
            final ItemSpade shovel = (ItemSpade) item;
            return shovel.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemAxe) {
            final ItemAxe axe = (ItemAxe) item;
            return axe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        }

        return 0;
    }
    public boolean isUseless(ItemStack stack) {
        if(!this.isEnabled()) {
            return isGarbage(stack);
        }

        if(isGarbage(stack)) {
            return true;
        } else if(stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).armorType == 0 && isBetterArmor(stack, helmet, ArmorType.HELMET)) {
            return true;
        } else if(stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).armorType == 1 && isBetterArmor(stack, chestplate, ArmorType.CHESTPLATE)) {
            return true;
        } else if(stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).armorType == 2 && isBetterArmor(stack, leggings, ArmorType.LEGGINGS)) {
            return true;
        } else if(stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).armorType == 3 && isBetterArmor(stack, boots, ArmorType.BOOTS)) {
            return true;
        } else if(stack.getItem() instanceof ItemSword && weapon != null && !isBetterWeapon(stack, weapon)) {
            return true;
        } else if(stack.getItem() instanceof ItemAxe && axe != null && isBetterTool(stack, axe)) {
            return true;
        } else if(stack.getItem() instanceof ItemPickaxe && pickaxe != null && isBetterTool(stack, pickaxe)) {
            return true;
        } else return stack.getItem().getUnlocalizedName().toLowerCase().contains("shovel") && shovel != null && isBetterTool(stack, shovel);
    }

    private boolean isBetterWeapon(ItemStack newWeapon, ItemStack oldWeapon) {
        Item item = newWeapon.getItem();

        if(item instanceof ItemSword || item instanceof ItemTool) {
            if(oldWeapon != null) {
                return getAttackDamage(newWeapon) > getAttackDamage(oldWeapon);
            } else {
                return true;
            }
        }

        return false;
    }


    private boolean isBetterTool(ItemStack newTool, ItemStack oldTool) {
        Item item = newTool.getItem();

        if(item instanceof ItemTool) {
            if(oldTool != null) {
                return !(getToolUsefulness(newTool) > getToolUsefulness(oldTool));
            } else {
                return false;
            }
        }

        return true;
    }
    private float getAttackDamage(ItemStack stack) {
        if(stack == null) return 0F;

        Item item = stack.getItem();

        float baseDamage = 0F;

        if (item instanceof ItemSword) {
            ItemSword sword = (ItemSword) item;
            baseDamage += sword.getAttackDamage();
        } else if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool) item;
            baseDamage += tool.getAttackDamage();
        }

        float enchantsDamage = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.3F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack) * 0.15F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1F;

        //LogUtil.addChatMessage("Damage : " + baseDamage + " Enchants damage : " + enchantsDamage);

        return baseDamage + enchantsDamage;
    }

    private float getToolUsefulness(ItemStack stack) {
        if(stack == null) return 0F;

        Item item = stack.getItem();

        float baseUsefulness = 0F;

        if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool) item;

            switch (tool.getToolMaterial()) {
                case WOOD:
                case GOLD:
                    baseUsefulness = 1F;
                    break;
                case STONE:
                    baseUsefulness = 2F;
                    break;
                case IRON:
                    baseUsefulness = 3F;
                    break;
                case EMERALD:
                    baseUsefulness = 4F;
                    break;
            }
        }

        float enchantsUsefulness = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack) * 1.25F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.3F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) * 0.5F
                + 0F;

        return baseUsefulness + enchantsUsefulness;
    }
    private boolean isBetterArmor(ItemStack newArmor, ItemStack oldArmor, ArmorType type) {
        if(oldArmor == null) return false;

        Item oldItem = oldArmor.getItem();

        if(oldItem instanceof ItemArmor) {
            ItemArmor oldItemArmor = (ItemArmor) oldItem;

            if(oldItemArmor.armorType == type.ordinal()) {
                return !(getArmorProtection(newArmor) > getArmorProtection(oldArmor));
            } else {
                return false;
            }
        }

        return true;
    }
    private float getArmorProtection(ItemStack stack) {
        if(stack == null) return 0F;

        Item item = stack.getItem();

        float baseProtection = 0F;

        if (item instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) item;
            baseProtection += armor.damageReduceAmount;
        }

        float enchantsProtection = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 1.25F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack) * 0.15F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack) * 0.15F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack) * 0.15F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack) * 0.1F
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1F;

        return baseProtection + enchantsProtection;
    }
    private double armorReduction(final ItemStack stack) {
        final ItemArmor armor = (ItemArmor) stack.getItem();
        return armor.damageReduceAmount + EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[]{stack}, DamageSource.generic)*0.2;
    }

    private int slot(final int slot) {
        if (slot >= 36) {
            return 8 - (slot - 36);
        }

        if (slot < 9) {
            return slot + 36;
        }

        return slot;
    }

    @EventLink()
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (isNull()) return;
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            this.placeTicks = 0;
        }
    };
    @EventLink()
    public final Listener<PostMotionEvent> onPostMotionEvent = event -> {
        if (isNull()) return;
        if (InstanceAccess.mc.thePlayer.ticksExisted <= 40) {
            return;
        }
        Container container = InstanceAccess.mc.thePlayer.inventoryContainer;
        int helmet_slot = 5;
        helmet = container.getSlot(helmet_slot).getStack();
        int chestplate_slot = 6;
        chestplate = container.getSlot(chestplate_slot).getStack();
        int leggings_slot = 7;
        leggings = container.getSlot(leggings_slot).getStack();
        int boots_slot = 8;
        boots = container.getSlot(boots_slot).getStack();
        weapon = container.getSlot(swordSlot.getValue().intValue() + 36).getStack();
        axe = container.getSlot(axeSlot.getValue().intValue() + 36).getStack();
        pickaxe = container.getSlot(pickaxeSlot.getValue().intValue() + 36).getStack();
        shovel = container.getSlot(shovelSlot.getValue().intValue() + 36).getStack();
        Scaffold Scaffold = Client.INSTANCE.getModuleManager().get(Scaffold.class);
        if(Scaffold.isEnabled() && Dis.getValue()) return;
        KillAura KillAura = Client.INSTANCE.getModuleManager().get(KillAura.class);
        if(KillAura.isEnabled() && KillAura.target != null && Dis.getValue()) return;
        if (InstanceAccess.mc.thePlayer.ticksExisted <= 40) {
            return;
        }

        if (InstanceAccess.mc.currentScreen instanceof GuiChest) {
            this.chestTicks = 0;
        } else {
            this.chestTicks++;
        }

        this.attackTicks++;
        this.placeTicks++;

        // Calls stopwatch.reset() to simulate opening an inventory, checks for an open inventory to be legit.
        if (legit.getValue() && !(InstanceAccess.mc.currentScreen instanceof GuiInventory)) {
            this.stopwatch.reset();
            return;
        }

        if (!this.stopwatch.finished(this.nextClick) || this.chestTicks < 10 || this.attackTicks < 10 || this.placeTicks < 10) {
            this.closeInventory();
            return;
        }

        if (!this.getModule(InventoryMove.class).isEnabled() && !(InstanceAccess.mc.currentScreen instanceof GuiInventory)) {
            return;
        }

        this.moved = false;

        int helmet = -1;
        int chestplate = -1;
        int leggings = -1;
        int boots = -1;

        int sword = -1;
        int pickaxe = -1;
        int axe = -1;
        int shovel = -1;
        int block = -1;
        int potion = -1;
        int food = -1;
        int pearl = -1;
        int throwables = -1;
        int INVENTORY_COLUMNS = 9;
        int ARMOR_SLOTS = 4;
        int INVENTORY_ROWS = 4;
        int INVENTORY_SLOTS = (INVENTORY_ROWS * INVENTORY_COLUMNS) + ARMOR_SLOTS;
        for (int i = 0; i < INVENTORY_SLOTS; i++) {
            final ItemStack stack = InstanceAccess.mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null) {
                continue;
            }
            final Item item = stack.getItem();



            if (item instanceof ItemArmor) {
                final ItemArmor armor = (ItemArmor) item;
                final double reduction = this.armorReduction(stack);

                switch (armor.armorType) {
                    case 0:
                        if (helmet == -1 || reduction > armorReduction(InstanceAccess.mc.thePlayer.inventory.getStackInSlot(helmet))) {
                            helmet = i;
                        }
                        break;

                    case 1:
                        if (chestplate == -1 || reduction > armorReduction(InstanceAccess.mc.thePlayer.inventory.getStackInSlot(chestplate))) {
                            chestplate = i;
                        }
                        break;

                    case 2:
                        if (leggings == -1 || reduction > armorReduction(InstanceAccess.mc.thePlayer.inventory.getStackInSlot(leggings))) {
                            leggings = i;
                        }
                        break;

                    case 3:
                        if (boots == -1 || reduction > armorReduction(InstanceAccess.mc.thePlayer.inventory.getStackInSlot(boots))) {
                            boots = i;
                        }
                        break;
                }
            }
            if (item instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) item;
                if (block == -1) {
                    block = i;
                } else if (itemBlock.getBlock() != Blocks.tnt)  {
                    continue;
                }  else {
                    final ItemStack currentStack = InstanceAccess.mc.thePlayer.inventory.getStackInSlot(block);

                    if (currentStack != null && stack.stackSize > currentStack.stackSize) {
                        block = i;
                    }
                }
            }
            if (item instanceof ItemAppleGold) {
                if (food == -1) {
                    food = i;
                } else {
                    final ItemStack currentStack = InstanceAccess.mc.thePlayer.inventory.getStackInSlot(food);

                    if (currentStack == null) {
                        continue;
                    }

                    final ItemAppleGold currentItemFood = (ItemAppleGold) currentStack.getItem();
                    final ItemAppleGold itemFood = (ItemAppleGold) item;

                    if (itemFood.getSaturationModifier(stack) > currentItemFood.getSaturationModifier(currentStack)) {
                        food = i;
                    }
                }
            }
            if (item instanceof ItemEnderPearl) {
                if (pearl == -1) {
                    pearl = i;
                } else {
                    final ItemStack currentStack = InstanceAccess.mc.thePlayer.inventory.getStackInSlot(pearl);

                    if (currentStack != null && stack.stackSize > currentStack.stackSize) {
                        pearl = i;
                    }
                }
            }
            if (item instanceof ItemSnowball) {
                if (throwables == -1) {
                    throwables = i;
                } else {
                    final ItemStack currentStack = InstanceAccess.mc.thePlayer.inventory.getStackInSlot(throwables);

                    if (currentStack != null && stack.stackSize > currentStack.stackSize) {
                        throwables = i;
                    }
                }
            }
            if (item instanceof ItemBow) {
                if (potion == -1 || bowdamage(stack) > bowdamage(InstanceAccess.mc.thePlayer.inventory.getStackInSlot(potion))) {
                    potion = i;
                }

                if (i != potion) {
                    this.throwItem(i);
                }
            }
            if (!ItemUtil.useful(stack)) {
                this.throwItem(i);
            }

            if (item instanceof ItemSword) {
                if (sword == -1 || damage(stack) > damage(InstanceAccess.mc.thePlayer.inventory.getStackInSlot(sword))) {
                    sword = i;
                }

                if (i != sword) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemPickaxe) {
                if (pickaxe == -1 || mineSpeed(stack) > mineSpeed(InstanceAccess.mc.thePlayer.inventory.getStackInSlot(pickaxe))) {
                    pickaxe = i;
                }

                if (i != pickaxe) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemAxe) {
                if (axe == -1 || mineSpeed(stack) > mineSpeed(InstanceAccess.mc.thePlayer.inventory.getStackInSlot(axe))) {
                    axe = i;
                }

                if (i != axe) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemSpade) {
                if (shovel == -1 || mineSpeed(stack) > mineSpeed(InstanceAccess.mc.thePlayer.inventory.getStackInSlot(shovel))) {
                    shovel = i;
                }

                if (i != shovel) {
                    this.throwItem(i);
                }
            }
        }
        for (int i = 0; i < INVENTORY_SLOTS; i++) {
            final ItemStack stack = InstanceAccess.mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null) {
                continue;
            }

            final Item item = stack.getItem();

            if (item instanceof ItemArmor) {
                final ItemArmor armor = (ItemArmor) item;

                switch (armor.armorType) {
                    case 0:
                        if (i != helmet) {
                            this.throwItem(i);
                        }
                        break;

                    case 1:
                        if (i != chestplate) {
                            this.throwItem(i);
                        }
                        break;

                    case 2:
                        if (i != leggings) {
                            this.throwItem(i);
                        }
                        break;

                    case 3:
                        if (i != boots) {
                            this.throwItem(i);
                        }
                        break;
                }
            }
        }

        if (helmet != -1 && helmet != 39) {
            this.equipItem(helmet);
        }

        if (chestplate != -1 && chestplate != 38) {
            this.equipItem(chestplate);
        }

        if (leggings != -1 && leggings != 37) {
            this.equipItem(leggings);
        }

        if (boots != -1 && boots != 36) {
            this.equipItem(boots);
        }

        if (sword != -1 && sword != this.swordSlot.getValue().intValue() - 1) {
            this.moveItem(sword, this.swordSlot.getValue().intValue() - 37);
        }

        if (pickaxe != -1 && pickaxe != this.pickaxeSlot.getValue().intValue() - 1) {
            this.moveItem(pickaxe, this.pickaxeSlot.getValue().intValue() - 37);
        }

        if (axe != -1 && axe != this.axeSlot.getValue().intValue() - 1) {
            this.moveItem(axe, this.axeSlot.getValue().intValue() - 37);
        }

        if (shovel != -1 && shovel != this.shovelSlot.getValue().intValue() - 1) {
            this.moveItem(shovel, this.shovelSlot.getValue().intValue() - 37);
        }

        if (block != -1 && block != this.blockSlot.getValue().intValue() - 1 && !this.getModule(Scaffold.class).isEnabled()) {
            this.moveItem(block, this.blockSlot.getValue().intValue() - 37);
        }

        if (potion != -1 && potion != this.potionSlot.getValue().intValue() - 1) {
            this.moveItem(potion, this.potionSlot.getValue().intValue() - 37);
        }

        if (food != -1 && food != this.foodSlot.getValue().intValue() - 1) {
            this.moveItem(food, this.foodSlot.getValue().intValue() - 37);
        }
        if (throwables != -1 && throwables != this.throwablesSlot.getValue().intValue() - 1){
            this.moveItem(throwables, this.throwablesSlot.getValue().intValue() - 37);
        }
        if (pearl != -1 && pearl != this.pearlSlot.getValue().intValue() - 1){
            this.moveItem(pearl, this.pearlSlot.getValue().intValue() - 37);
        }
        if (this.canOpenInventory() && !this.moved) {
            this.closeInventory();
        }
    };
}

enum ArmorType {
    HELMET, CHESTPLATE, LEGGINGS, BOOTS

}
