package com.alan.clients.module.impl.combat;

import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.other.Stuck;
import com.alan.clients.module.impl.player.Blink;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.util.RandomUtil;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.rotation.RotationUtil;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.impl.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import util.time.StopWatch;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Rise
@ModuleInfo(name = "ProjectileAura", description = "ProjectileAura", category = Category.COMBAT)
public final class ProjectileAura extends Module {
    private final ModeValue mode = new ModeValue("Attack Mode", this)
            .add(new SubMode("Single"))
            .add(new SubMode("Switch"))
            .setDefault("Single");
    private final BoundsNumberValue switchTicks = new BoundsNumberValue("Switch Ticks", this, 100, 1000, 0, 2000, 1, () -> !mode.getName().equalsIgnoreCase("Single"));

    public final NumberValue range = new NumberValue("Range", this, 8, 5, 15, 1);
    public final NumberValue minRange = new NumberValue("Min Range", this, 0, 0, 4, 0.1);
    private final BoundsNumberValue rotationSpeed = new BoundsNumberValue("Rotation speed", this, 5, 10, 0, 20, 1);
    private final NumberValue ticksValue = new NumberValue("Attack Ticks", this, 20.0, 1.0, 80.0, 1.0);

    private final BooleanValue showTargets = new BooleanValue("Targets", this, false);
    public final BooleanValue player = new BooleanValue("Player", this, false, () -> !showTargets.getValue());
    public final BooleanValue invisibles = new BooleanValue("Invisibles", this, false, () -> !showTargets.getValue());
    public final BooleanValue animals = new BooleanValue("Animals", this, false, () -> !showTargets.getValue());
    public final BooleanValue mobs = new BooleanValue("Mobs", this, false, () -> !showTargets.getValue());
    public final BooleanValue villagers = new BooleanValue("Villagers", this, false, () -> !showTargets.getValue());
    public final BooleanValue teams = new BooleanValue("Teams", this, false, () -> !showTargets.getValue());

    private long AttackTime = 0;
    private float randomYaw;
    private float randomPitch;
    private boolean AttackFix;

    public static List<Entity> targets;
    public Entity target;
    public StopWatch switchChangeTicks = new StopWatch();

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        KillAura killAura = getModule(KillAura.class);

        if (isNull() || killAura.target != null || this.getModule(Scaffold.class).isEnabled() || this.getModule(Blink.class).isEnabled() || this.getModule(Stuck.class).isEnabled()) return;

        if (mc.thePlayer.inventory.hasItem(Items.snowball) || mc.thePlayer.inventory.hasItem(Items.egg)) {
            this.getTargets(range.getValue().doubleValue());

            if (targets.isEmpty()) {
                this.randomiseTargetRotations();
                target = null;
                return;
            }

            if (mode.getValue().getName().equalsIgnoreCase("Single"))
                target = targets.get(0);
            else if (this.switchChangeTicks.finished(RandomUtil.nextInt(switchTicks.getMin().intValue(), switchTicks.getMax().intValue())) && targets.size() > 1) {
                Client.INSTANCE.getTargetManager().updateTargets();
                if (targets.contains(target)) {
                    targets.remove(target);
                    Entity oldTarget = target;
                    target = targets.get(0);
                    targets.add(oldTarget);
                } else {
                    target = targets.get(0);
                }
                this.switchChangeTicks.reset();
            } else if (targets.size() == 1) {
                target = targets.get(0);
            }

            if (target == null || mc.thePlayer.isDead) {
                this.randomiseTargetRotations();
                return;
            }

            if (mc.thePlayer.inventory.hasItem(Items.snowball) || mc.thePlayer.inventory.hasItem(Items.egg)) {
                int snowballSlot = getItemSlot(Items.snowball);
                int eggSlot = getItemSlot(Items.egg);

                if (snowballSlot != -1) {
                    mc.thePlayer.inventory.currentItem = snowballSlot;
                } else if (eggSlot != -1) {
                    mc.thePlayer.inventory.currentItem = eggSlot;
                }
            }

            if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemSnowball || mc.thePlayer.getHeldItem().getItem() instanceof ItemEgg)) {
                this.rotations();
                this.doAttack();
            }
        }
    };

    private void rotations() {
        final Vector2f targetRotations = RotationUtil.calculate(target, false, range.getValue().floatValue());
        this.randomiseTargetRotations();
        final double minRotationSpeed = this.rotationSpeed.getValue().doubleValue();
        final double maxRotationSpeed = this.rotationSpeed.getSecondValue().doubleValue();
        final float rotationSpeed = (float) MathUtil.getRandom(minRotationSpeed, maxRotationSpeed);

        targetRotations.x += randomYaw / 5;
        targetRotations.y += randomPitch / 5;

        Vec3 playerPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        Vec3 targetPos = new Vec3(target.posX, target.posY + target.getEyeHeight(), target.posZ);
        double distance = playerPos.distanceTo(targetPos);

        if (distance < minRange.getValue().doubleValue()) {
            randomYaw = randomPitch = 0;
            return; // 不在有效范围内，直接返回
        }

        Vec3 direction = targetPos.subtract(playerPos).normalize();
        Vec3 start = playerPos.addVector(0.0, mc.thePlayer.getEyeHeight(), 0.0);
        Vec3 end = start.addVector(direction.xCoord * range.getValue().doubleValue(), direction.yCoord * range.getValue().doubleValue(), direction.zCoord * range.getValue().doubleValue());

        MovingObjectPosition result = mc.theWorld.rayTraceBlocks(start, end);

        if (result != null && result.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
            randomYaw = randomPitch = 0;
            return;
        }

        RotationComponent.setRotations(targetRotations, rotationSpeed, MovementFix.NORMAL);
        AttackFix = true;
    }

    private void doAttack() {
        long currentTime = System.currentTimeMillis();
        long ticks = ticksValue.getValue().intValue() * 20L;

        if (currentTime - AttackTime < ticks)
            return;
        if (AttackFix) {
            rotations();
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
            AttackFix = false;
        }
        AttackTime = currentTime;
    }


    private void randomiseTargetRotations() {
        randomYaw += ((float) (Math.random() - 0.5f)) / 5;
        randomPitch += ((float) (Math.random() - 0.5f) * 2) / 5;
    }

    private int getItemSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public void getTargets(double range) {
        Teams teams1 = Client.INSTANCE.getModuleManager().get(Teams.class);
        AntiBot antiBot = Client.INSTANCE.getModuleManager().get(AntiBot.class);

        targets = mc.theWorld.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase && entity != mc.thePlayer)
                .filter(entity -> !entity.isDead && ((EntityLivingBase) entity).deathTime == 0)
                .filter(entity -> mc.thePlayer.getDistanceToEntity(entity) <= range)
                .filter(entity -> {
                    if (entity.isInvisible() && !invisibles.getValue()) {
                        return false;
                    }
                    if (antiBot.isBot(entity)) {
                        return false;
                    }
                    if (teams1.isInYourTeam((EntityLivingBase) entity) && teams.getValue()) {
                        return false;
                    }
                    return isTargetTypeAllowed(entity);
                })
                .sorted(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceToEntity(entity)))
                .collect(Collectors.toList());

        target = targets.isEmpty() ? null : targets.get(0);
    }

    private boolean isTargetTypeAllowed(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return player.getValue();
        }
        if (entity instanceof EntityAnimal) {
            return animals.getValue();
        }
        if (entity instanceof EntityVillager) {
            return villagers.getValue();
        }
        if (entity instanceof EntitySquid) {
            return false;
        }
        if (entity instanceof EntityMob) {
            return mobs.getValue();
        }
        return false;
    }
}
