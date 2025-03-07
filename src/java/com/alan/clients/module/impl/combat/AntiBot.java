package com.alan.clients.module.impl.combat;

import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.other.AttackEvent;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.NumberValue;
import com.alan.clients.value.impl.SubMode;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldSettings;

import java.util.*;
import java.util.regex.Pattern;

@Rise
@ModuleInfo(name = "module.combat.antibot.name", description = "module.combat.antibot.description", category = Category.COMBAT)
public final class AntiBot extends Module {
    private final BooleanValue tabValue = new BooleanValue("Tab", this, false);
    private final ModeValue tabModeValue = new ModeValue("TabMode", this, () -> !tabValue.getValue())
            .add(new SubMode("Equals"))
            .add(new SubMode("Contains"))
            .setDefault("Contains");

    private final BooleanValue entityIDValue = new BooleanValue("EntityID", this, false);
    private final BooleanValue colorValue = new BooleanValue("Color", this, false);
    private final BooleanValue livingTimeValue = new BooleanValue("Living Time", this, false);
    private final NumberValue livingTimeTicksValue = new NumberValue("Living Time Ticks", this, 40,1,200,1);
    private final BooleanValue groundValue = new BooleanValue("Ground", this, false);
    private final BooleanValue airValue = new BooleanValue("Air", this, false);
    private final BooleanValue invalidGroundValue = new BooleanValue("InvalidGround", this ,true);
    private final BooleanValue swingValue = new BooleanValue("Swing", this, false);
    private final BooleanValue healthValue = new BooleanValue("Health", this, false);
    private final BooleanValue derpValue = new BooleanValue("Derp", this, false);
    private final BooleanValue wasInvisibleValue = new BooleanValue("Was Invisible", this, false);
    private final BooleanValue validNameValue = new BooleanValue("Valid Name", this, false);
    private final BooleanValue hiddenNameValue = new BooleanValue("Hidden Name", this, false);
    private final BooleanValue armorValue = new BooleanValue("Armor", this, false);
    private final BooleanValue pingValue = new BooleanValue("Ping", this, false);
    private final BooleanValue needHitValue = new BooleanValue("Need Hit", this, false);
    private final BooleanValue noClipValue = new BooleanValue("No Clip", this, false);
    private final BooleanValue czechHekValue = new BooleanValue("Czech Matrix", this, false);
    private final BooleanValue czechHekPingCheckValue = new BooleanValue("Ping Check", this, false, () -> !czechHekValue.getValue());
    private final BooleanValue czechHekGMCheckValue = new BooleanValue("GameMode Check", this, false, () -> !czechHekValue.getValue());
    private final BooleanValue reusedEntityIdValue = new BooleanValue("Reused EntityId",this , false);
    private final BooleanValue spawnInCombatValue = new BooleanValue("Spawn In Combat",this, false);
    private final BooleanValue duplicateInWorldValue = new BooleanValue("Duplicate In World",this, false);
    private final BooleanValue duplicateInTabValue = new BooleanValue("Duplicate In Tab",this, false);
    private final ModeValue duplicateCompareModeValue = new ModeValue("Duplicate Compare Mode", this, ()-> !duplicateInWorldValue.getValue() || !duplicateInTabValue.getValue())
            .add(new SubMode("On Time"))
            .add(new SubMode("When Spawn"))
            .setDefault("On Time");
    private final BooleanValue fastDamageValue = new BooleanValue("Fast Damage",this, false);
    private final NumberValue fastDamageTicksValue = new NumberValue("Fast Damage Ticks",this,5,1,20,1, () -> !fastDamageValue.getValue());
    private final BooleanValue removeFromWorld = new BooleanValue("Remove From World", this, false);
    private final NumberValue removeIntervalValue = new NumberValue("Remove-Interval", this, 20,1,100,1, () -> !removeFromWorld.getValue());
    private final BooleanValue debugValue = new BooleanValue("Debug", this, false);
    private final BooleanValue alwaysInRadiusValue = new BooleanValue("Always In Radius", this, false);
    private final NumberValue alwaysRadiusValue = new NumberValue("Always In Radius Blocks", this, 20F, 5F, 30F, 1F, () -> !alwaysInRadiusValue.getValue());
    private final BooleanValue alwaysInRadiusRemoveValue = new BooleanValue("Always In Radius Remove", this, false, () -> !alwaysInRadiusValue.getValue());
    private final BooleanValue alwaysInRadiusWithTicksCheckValue = new BooleanValue("Always In Radius With Ticks Check", this, false, () -> !alwaysInRadiusValue.getValue() || !livingTimeValue.getValue());

    private final List<Integer> ground = new ArrayList<>();
    private final List<Integer> air = new ArrayList<>();
    private final Map<Integer, Integer> invalidGround = new HashMap<>();
    private final List<Integer> swing = new ArrayList<>();
    private final List<Integer> invisible = new ArrayList<>();
    private final List<Integer> hitted = new ArrayList<>();
    private final List<Integer> spawnInCombat = new ArrayList<>();
    private final List<Integer> notAlwaysInRadius = new ArrayList<>();
    private final Map<Integer, Integer> lastDamage = new HashMap<>();
    private final Map<Integer, Float> lastDamageVl = new HashMap<>();
    private final List<UUID> duplicate = new ArrayList<>();
    private final List<Integer> noClip = new ArrayList<>();
    private final List<Integer> hasRemovedEntities = new ArrayList<>();
    private final Pattern regex = Pattern.compile("\\w{3,16}");
    private boolean wasAdded = InstanceAccess.mc.thePlayer != null;

    @EventLink()
    private final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (InstanceAccess.mc.thePlayer == null || InstanceAccess.mc.theWorld == null) return;
        if (removeFromWorld.getValue() && InstanceAccess.mc.thePlayer.ticksExisted > 0 && InstanceAccess.mc.thePlayer.ticksExisted % removeIntervalValue.getValue().intValue() == 0) {
            List<EntityPlayer> ent = new ArrayList<>();
            for (EntityPlayer entity : InstanceAccess.mc.theWorld.playerEntities) {
                if (entity != InstanceAccess.mc.thePlayer && isBot(entity)) {
                    ent.add(entity);
                }
            }
            if (ent.isEmpty()) return;
            for (EntityPlayer e : ent) {
                InstanceAccess.mc.theWorld.removeEntity(e);
                if (debugValue.getValue()) {
                    ChatUtil.display("§7[§a§lAnti Bot§7] §fRemoved §r" + e.gameProfile.getName() + " §fdue to it being a bot.");
                }
            }
        }

    };
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    public static String stripColor(String input) {
        return COLOR_PATTERN.matcher(input == null ? "" : input).replaceAll("");
    }

    public boolean isBot(Entity entity) {
        if (!(entity instanceof EntityPlayer) || entity == InstanceAccess.mc.thePlayer) {
            return false;
        }

        if (!Client.INSTANCE.getModuleManager().get(AntiBot.class).isEnabled()) {
            return false;
        }

        if (validNameValue.getValue() && !regex.matcher(entity.getDisplayName().getUnformattedText()).matches()) {
            return true;
        }

        if (hiddenNameValue.getValue() && (entity.getDisplayName().getUnformattedText().contains("§") || (entity.hasCustomName() && entity.getCustomNameTag().contains(entity.getDisplayName().getUnformattedText())))) {
            return true;
        }

        if (colorValue.getValue() && !entity.getDisplayName().getFormattedText().replace("§r", "").contains("§")) {
            return true;
        }

        if (livingTimeValue.getValue() && entity.ticksExisted < livingTimeTicksValue.getValue().intValue()) {
            return true;
        }

        if (groundValue.getValue() && !ground.contains(entity.getEntityId())) {
            return true;
        }

        if (airValue.getValue() && !air.contains(entity.getEntityId())) {
            return true;
        }

        if (swingValue.getValue() && !swing.contains(entity.getEntityId())) {
            return true;
        }

        if (noClipValue.getValue() && noClip.contains(entity.getEntityId())) {
            return true;
        }

        if (reusedEntityIdValue.getValue() && hasRemovedEntities.contains(entity.getEntityId())) {
            return false;
        }

        if (healthValue.getValue() && (((EntityPlayer) entity).getHealth() > 20F || ((EntityPlayer) entity).getHealth() <= 0F)) {
            return true;
        }

        if (spawnInCombatValue.getValue() && spawnInCombat.contains(entity.getEntityId())) {
            return true;
        }

        if (entityIDValue.getValue() && (entity.getEntityId() >= 1000000000 || entity.getEntityId() <= -1)) {
            return true;
        }

        if (derpValue.getValue() && (entity.rotationPitch > 90F || entity.rotationPitch < -90F)) {
            return true;
        }

        if (wasInvisibleValue.getValue() && invisible.contains(entity.getEntityId())) {
            return true;
        }

        if (armorValue.getValue()) {
            if (((EntityPlayer) entity).inventory.armorInventory[0] == null && ((EntityPlayer) entity).inventory.armorInventory[1] == null &&
                    ((EntityPlayer) entity).inventory.armorInventory[2] == null && ((EntityPlayer) entity).inventory.armorInventory[3] == null) {
                return true;
            }
        }

        if (needHitValue.getValue() && !hitted.contains(entity.getEntityId())) {
            return true;
        }

        if (invalidGroundValue.getValue() && invalidGround.getOrDefault(entity.getEntityId(), 0) >= 10) {
            return true;
        }

        if (tabValue.getValue()) {
            boolean equals = tabModeValue.equals("Equals");
            String targetName = stripColor(entity.getDisplayName().getFormattedText());

            for (NetworkPlayerInfo networkPlayerInfo : InstanceAccess.mc.getNetHandler().getPlayerInfoMap()) {
                String networkName = stripColor(networkPlayerInfo.getDisplayName().getFormattedText());

                if ((equals && targetName.equals(networkName)) || (!equals && targetName.contains(networkName))) {
                    return false;
                }
            }

            return true;
        }

        if (duplicateCompareModeValue.equals("When Spawn") && duplicate.contains(((EntityPlayer) entity).getGameProfile().getId())) {
            return true;
        }

        if (duplicateInWorldValue.getValue() && duplicateCompareModeValue.equals("On Time") &&
                InstanceAccess.mc.theWorld.getLoadedEntityList().stream().filter(it -> it instanceof EntityPlayer && it.getDisplayName().getUnformattedText().equals(entity.getDisplayName().getUnformattedText())).count() > 1) {
            return true;
        }

        if (duplicateInTabValue.getValue() && duplicateCompareModeValue.equals("On Time") &&
                InstanceAccess.mc.getNetHandler().getPlayerInfoMap().stream().filter(it -> entity.getDisplayName().getUnformattedText().equals(it.getGameProfile().getName())).count() > 1) {
            return true;
        }

        if (fastDamageValue.getValue() && lastDamageVl.getOrDefault(entity.getEntityId(), 0f) > 0) {
            return true;
        }

        if (alwaysInRadiusValue.getValue() && !notAlwaysInRadius.contains(entity.getEntityId())) {
            if (alwaysInRadiusRemoveValue.getValue()) {
                InstanceAccess.mc.theWorld.removeEntity(entity);
            }
            return true;
        }

        return entity.getDisplayName().getUnformattedText().isEmpty() || entity.getDisplayName().getUnformattedText().equals(InstanceAccess.mc.thePlayer.getDisplayName().getUnformattedText());
    }

    private void processEntityMove(Entity entity, boolean onGround) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;

            if (onGround && !ground.contains(entityPlayer.getEntityId())) {
                ground.add(entityPlayer.getEntityId());
            }

            if (!onGround && !air.contains(entityPlayer.getEntityId())) {
                air.add(entityPlayer.getEntityId());
            }

            if (onGround) {
                if (entityPlayer.prevPosY != entityPlayer.posY) {
                    int currentInvalidGround = invalidGround.getOrDefault(entityPlayer.getEntityId(), 0) + 1;
                    invalidGround.put(entityPlayer.getEntityId(), currentInvalidGround);
                }
            } else {
                int currentVL = invalidGround.getOrDefault(entityPlayer.getEntityId(), 0) / 2;
                if (currentVL <= 0) {
                    invalidGround.remove(entityPlayer.getEntityId());
                } else {
                    invalidGround.put(entityPlayer.getEntityId(), currentVL);
                }
            }

            if (entityPlayer.isInvisible() && !invisible.contains(entityPlayer.getEntityId())) {
                invisible.add(entityPlayer.getEntityId());
            }

            if (!noClip.contains(entityPlayer.getEntityId())) {
                List<AxisAlignedBB> cb = InstanceAccess.mc.theWorld.getCollidingBoundingBoxes(entityPlayer, entityPlayer.getEntityBoundingBox().contract(0.0625, 0.0625, 0.0625));
                if (!cb.isEmpty()) {
                    noClip.add(entityPlayer.getEntityId());
                }
            }

            if ((!livingTimeValue.getValue() || entityPlayer.ticksExisted > livingTimeTicksValue.getValue().intValue() || !alwaysInRadiusWithTicksCheckValue.getValue()) &&
                    !notAlwaysInRadius.contains(entityPlayer.getEntityId()) && InstanceAccess.mc.thePlayer.getDistanceToEntity(entityPlayer) > alwaysRadiusValue.getValue().floatValue()) {
                notAlwaysInRadius.add(entityPlayer.getEntityId());
            }
        }
    }

    @EventLink()
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (!isNull()) {
            if (czechHekValue.getValue()) {
                Packet packet = event.getPacket();

                if (packet instanceof S41PacketServerDifficulty) {
                    wasAdded = false;
                }

                if (packet instanceof S38PacketPlayerListItem) {
                    S38PacketPlayerListItem packetListItem = (S38PacketPlayerListItem) packet;
                    S38PacketPlayerListItem.AddPlayerData data = packetListItem.func_179767_a().get(0);

                    if (data.getProfile() != null && data.getProfile().getName() != null) {
                        if (!wasAdded) {
                            wasAdded = data.getProfile().getName().equals(InstanceAccess.mc.thePlayer.getNameClear());
                        } else if (!InstanceAccess.mc.thePlayer.isSpectator() && !InstanceAccess.mc.thePlayer.capabilities.allowFlying &&
                                (!czechHekPingCheckValue.getValue() || data.getPing() != 0) &&
                                (!czechHekGMCheckValue.getValue() || data.getGameMode() != WorldSettings.GameType.NOT_SET)) {
                            event.setCancelled(true);
                            if (debugValue.getValue()) {
                                ChatUtil.display("§7[§a§lAnti Bot/§6Matrix§7] §fPrevented §r" +
                                        data.getProfile().getName() + " §ffrom spawning.");
                            }
                        }
                    }
                }
            }

            Packet packet = event.getPacket();

            if (packet instanceof S18PacketEntityTeleport) {
                processEntityMove(InstanceAccess.mc.theWorld.getEntityByID(((S18PacketEntityTeleport) packet).getEntityId()), ((S18PacketEntityTeleport) packet).getOnGround());
            } else if (packet instanceof S14PacketEntity) {
                processEntityMove(((S14PacketEntity) packet).getEntity(InstanceAccess.mc.theWorld), ((S14PacketEntity) packet).getOnGround());
            } else if (packet instanceof S0BPacketAnimation) {
                S0BPacketAnimation animationPacket = (S0BPacketAnimation) packet;
                Entity entity = InstanceAccess.mc.theWorld.getEntityByID(animationPacket.getEntityID());

                if (entity instanceof EntityLivingBase && animationPacket.getAnimationType() == 0 &&
                        !swing.contains(entity.getEntityId())) {
                    swing.add(entity.getEntityId());
                }
            } else if (packet instanceof S38PacketPlayerListItem) {
                S38PacketPlayerListItem listItemPacket = (S38PacketPlayerListItem) packet;

                if (duplicateCompareModeValue.equals("When Spawn") &&
                        listItemPacket.func_179768_b() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                    for (S38PacketPlayerListItem.AddPlayerData entry : listItemPacket.func_179767_a()) {
                        String name = entry.getProfile().getName();

                        if ((duplicateInWorldValue.getValue() && InstanceAccess.mc.theWorld.playerEntities.stream()
                                .anyMatch(player -> player.getDisplayName().getUnformattedText().equals(name))) ||
                                (duplicateInTabValue.getValue() && InstanceAccess.mc.getNetHandler().getPlayerInfoMap().stream()
                                        .anyMatch(info -> info.getGameProfile().getName().equals(name)))) {
                            duplicate.add(entry.getProfile().getId());
                        }
                    }
                }
            } else if (packet instanceof S0CPacketSpawnPlayer) {
                S0CPacketSpawnPlayer spawnPlayerPacket = (S0CPacketSpawnPlayer) packet;

                if (false && !hasRemovedEntities.contains(spawnPlayerPacket.getEntityID())) {
                    spawnInCombat.add(spawnPlayerPacket.getEntityID());
                }

            } else if (packet instanceof S13PacketDestroyEntities) {

                //hasRemovedEntities.addAll(((S13PacketDestroyEntities) packet).getEntityIDs().strea );
            }

            if (packet instanceof S19PacketEntityStatus && ((S19PacketEntityStatus) packet).getOpCode() == 2 ||
                    packet instanceof S0BPacketAnimation && ((S0BPacketAnimation) packet).getAnimationType() == 1) {
                Entity entity;

                if (packet instanceof S19PacketEntityStatus) {
                    entity = ((S19PacketEntityStatus) packet).getEntity(InstanceAccess.mc.theWorld);
                } else if (packet instanceof S0BPacketAnimation) {
                    entity = InstanceAccess.mc.theWorld.getEntityByID(((S0BPacketAnimation) packet).getEntityID());
                } else {
                    entity = null;
                }

                if (entity instanceof EntityPlayer) {
                    EntityPlayer playerEntity = (EntityPlayer) entity;
                    float currentVl = lastDamageVl.getOrDefault(playerEntity.getEntityId(), 0f);
                    int ticksExistedDiff = playerEntity.ticksExisted - lastDamage.getOrDefault(playerEntity.getEntityId(), 0);

                    lastDamageVl.put(playerEntity.getEntityId(),
                            currentVl + (ticksExistedDiff <= fastDamageTicksValue.getValue().intValue() ? 1f : -0.5f));
                    lastDamage.put(playerEntity.getEntityId(), playerEntity.ticksExisted);
                }
            }
        }
    };
    @EventLink
    private final Listener<AttackEvent> onAttack = e -> {
        Entity entity = e.getTarget();

        if (entity instanceof EntityLivingBase && !hitted.contains(entity.getEntityId())) {
            hitted.add(entity.getEntityId());
        }
    };

    @EventLink
    private final Listener<WorldChangeEvent> onWorld = event -> clearAll();

    @Override
    protected void onDisable() {
        clearAll();
    }
    private void clearAll() {
        hitted.clear();
        swing.clear();
        ground.clear();
        invalidGround.clear();
        invisible.clear();
        lastDamage.clear();
        lastDamageVl.clear();
        notAlwaysInRadius.clear();
        duplicate.clear();
        spawnInCombat.clear();
        noClip.clear();
        hasRemovedEntities.clear();
    }
}
