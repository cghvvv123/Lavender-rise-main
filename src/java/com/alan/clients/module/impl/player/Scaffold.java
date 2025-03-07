package com.alan.clients.module.impl.player;

import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.component.impl.player.BadPacketsComponent;
import com.alan.clients.component.impl.player.BlinkComponent;
import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.component.impl.player.SlotComponent;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.component.impl.player.rotationcomponent.Rotation;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.module.impl.combat.Velocity;
import com.alan.clients.module.impl.ghost.Eagle;
import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.module.impl.other.ChestAura;
import com.alan.clients.module.impl.player.scaffold.sprint.*;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.input.MoveInputEvent;
import com.alan.clients.newevent.impl.motion.PostMotionEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.other.PossibleClickEvent;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.noti.NotificationManager;
import com.alan.clients.noti.NotificationType;
import com.alan.clients.util.RandomUtil;
import com.alan.clients.util.RayCastUtil;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.*;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.rotation.RotationUtil;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.impl.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Fix By Lavender
 * @since ??/??/21
 */

@Rise
@ModuleInfo(name = "module.player.scaffold.name", description = "module.player.scaffold.description", category = Category.PLAYER)
public class Scaffold extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Test"))
            .add(new SubMode("Telly"))
            .setDefault("Telly");
    private final ModeValue placeTime = new ModeValue("Place Time", this)
            .add(new SubMode("Pre"))
            .add(new SubMode("Post"))
            .add(new SubMode("Tick"))
            .add(new SubMode("Legit"))
            .setDefault("Pre");
    private final NumberValue uptellyTick = new NumberValue("UP Telly Ticks", this, 0.0, 0.0, 10.0, 0.1);
    private final NumberValue sameYtellyTick = new NumberValue("SameY Telly Ticks", this, 0.0, 0.0, 5.0, 0.1);

    private final ModeValue rayCast = new ModeValue("Ray Cast", this)
            .add(new SubMode("Off"))
            .add(new SubMode("Normal"))
            .add(new SubMode("Strict"))
            .setDefault("Strict");

    private final ModeValue sprint = new ModeValue("Sprint", this)
            .add(new SubMode("Normal"))
            .add(new DisabledSprint("Disabled", this))
            .add(new LegitSprint("Legit", this))
            .add(new BypassSprint("Bypass", this))
            .add(new VulcanSprint("Vulcan", this))
            .add(new NCPSprint("No Cheat Plus", this))
            .add(new MatrixSprint("Matrix", this))
            .add(new HuaYuTingSprint("HuaYuTing", this))
            .add(new WatchdogSprint("Watchdog", this))
            .setDefault("Normal");


    private final ModeValue sameY = new ModeValue("Same Y", this)
            .add(new SubMode("Off"))
            .add(new SubMode("On"))
            .add(new SubMode("Auto Jump"))
            .setDefault("Auto Jump");
    private final ListValue<Rotation> RotationMode = new ListValue<>("RotationMode", this);
    private final ListValue<MovementFix> movementCorrection = new ListValue<>("Movement correction", this);
    private final BooleanValue noSwing = new BooleanValue("No Swing", this, false);
    private final BooleanValue safeWalk = new BooleanValue("Safe Walk", this, false);
    private final BooleanValue eagle = new BooleanValue("Eagle", this, true);

    private final BoundsNumberValue rotationSpeed = new BoundsNumberValue("Rotation Speed", this, 5, 10, 0, 15, 1);
    private final BoundsNumberValue placeDelay = new BoundsNumberValue("Place Delay", this, 0, 0, 0, 5, 1);
    private final BooleanValue render = new BooleanValue("Render", this, true);
    public final BooleanValue enablefov = new BooleanValue("EnableFov", this, false);
    public final NumberValue fov = new NumberValue("Fov", this, 1.0, 0.1, 1.4, 0.1);
    private final BooleanValue markValue = new BooleanValue("Mark", this, true);
    private final BooleanValue autodis = new BooleanValue("AutoDisable", this, false);

    public static final List<Block> invalidBlocks = Arrays.asList(Blocks.enchanting_table, Blocks.furnace, Blocks.carpet, Blocks.crafting_table, Blocks.trapped_chest, Blocks.chest, Blocks.dispenser, Blocks.air, Blocks.water, Blocks.lava, Blocks.flowing_water, Blocks.flowing_lava, Blocks.sand, Blocks.snow_layer, Blocks.torch, Blocks.anvil, Blocks.jukebox, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.noteblock, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.wooden_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_slab, Blocks.wooden_slab, Blocks.stone_slab2, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.yellow_flower, Blocks.red_flower, Blocks.anvil, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.cactus, Blocks.ladder, Blocks.web);
    private Vec3 targetBlock;
    private EnumFacingOffset enumFacing;
    private BlockPos blockFace;
    private float targetYaw, targetPitch;
    private int ticksOnAir;
    private double startY;
    public boolean fix;
    public float forward, strafe;
    public Scaffold() {
        for (MovementFix movementFix : MovementFix.values()) {
            movementCorrection.add(movementFix);
        }
        movementCorrection.setDefault(MovementFix.OFF);
        for (Rotation rotationmode : Rotation.values()) {
            RotationMode.add(rotationmode);
        }
        RotationMode.setDefault(Rotation.OFF);
    }

    @Override
    protected void onEnable() {
        targetYaw = InstanceAccess.mc.thePlayer.rotationYaw - 180;
        targetPitch = 90;
        startY = Math.floor(InstanceAccess.mc.thePlayer.posY);
        targetBlock = null;
    }

    protected void onDisable() {
        InstanceAccess.mc.gameSettings.keyBindSneak.setPressed(Keyboard.isKeyDown(InstanceAccess.mc.gameSettings.keyBindSneak.getKeyCode()));
        InstanceAccess.mc.gameSettings.keyBindJump.setPressed(Keyboard.isKeyDown(InstanceAccess.mc.gameSettings.keyBindJump.getKeyCode()));
        BlinkComponent.blinking = false;
        if (InstanceAccess.mc.thePlayer != null) {
            SlotComponent.setSlot(InstanceAccess.mc.thePlayer.inventory.currentItem);
        }
    }



    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        if (isNull()) return;
        final Packet<?> packet = event.getPacket();
        if (event.getPacket() instanceof S08PacketPlayerPosLook && autodis.getValue()) {
            NotificationManager.post(NotificationType.WARNING, "Flag Detector", "Scaffold disabled due to " + (InstanceAccess.mc.thePlayer == null || InstanceAccess.mc.thePlayer.ticksExisted < 5 ? "world change" : "lagback"), 1.5F);
            this.setEnabled(false);
        }
        if (packet instanceof S2FPacketSetSlot) {
            final S2FPacketSetSlot wrapper = ((S2FPacketSetSlot) packet);

            if (wrapper.func_149174_e() == null) {
                event.setCancelled(true);
            } else {
                try {
                    int slot = wrapper.func_149173_d() - 36;
                    if (slot < 0) return;
                    final ItemStack itemStack = InstanceAccess.mc.thePlayer.inventory.getStackInSlot(slot);
                    final Item item = wrapper.func_149174_e().getItem();

                    if ((InstanceAccess.mc.thePlayer != null && itemStack == null && wrapper.func_149174_e().stackSize <= 6 && item instanceof ItemBlock && !SlotUtil.blacklist.contains(((ItemBlock) item).getBlock())) ||
                            itemStack != null && Math.abs(Objects.requireNonNull(itemStack).stackSize - wrapper.func_149174_e().stackSize) <= 6 ||
                            wrapper.func_149174_e() == null) {
                        event.setCancelled(true);
                    }
                } catch (ArrayIndexOutOfBoundsException exception) {
                    exception.printStackTrace();
                }
            }
        }
    };

    public void calculateSneaking(MoveInputEvent moveInputEvent) {
        forward = moveInputEvent.getForward();
        strafe = moveInputEvent.getStrafe();
    }

    public void calculateSneaking() {
        InstanceAccess.mc.gameSettings.keyBindSneak.setPressed(false);
    }

    public void calculateRotations() {
        if (isNull()) return;
        /* Calculating target rotations */
        switch (mode.getValue().getName()) {
            case "Normal":
                if (!mc.thePlayer.onGround) {
                    getRotations();
                } else {
                    getRotations();
                    targetYaw = InstanceAccess.mc.thePlayer.rotationYaw;
                }
                break;
            case "Test":
                if (mc.thePlayer.offGroundTicks > uptellyTick.getValue().floatValue()) {
                    getRotations();
                } else {
                    getRotations();
                    targetYaw = InstanceAccess.mc.thePlayer.rotationYaw ;
                }
                break;
            case "Telly":
                if (mc.thePlayer.offGroundTicks >=  uptellyTick.getValue().floatValue()) {
                    getRotations();
                } else {
                    getRotations();
                    targetYaw = InstanceAccess.mc.thePlayer.rotationYaw ;

                   // smoothRotations();
                }
                break;
        }


        /* Smoothing rotations */
        final double minRotationSpeed = this.rotationSpeed.getValue().doubleValue();
        final double maxRotationSpeed = this.rotationSpeed.getSecondValue().doubleValue();
        float rotationSpeed = (float) MathUtil.getRandom(minRotationSpeed, maxRotationSpeed);

        if (rotationSpeed != 0) {
            RotationComponent.setRotations(new Vector2f(targetYaw, targetPitch), rotationSpeed, movementCorrection.getValue());
        }
    }
    public void runMode() {
        if (mc.thePlayer.onGround && MoveUtil.isMoving()) {mc.thePlayer.jump();}
    }
    //哎呀妈呀哎呀妈呀哎呀妈呀哎呀妈呀

    private boolean isWorking = false;
    private synchronized void work() {
        if (isNull() || isWorking) return;
        isWorking = true;

        try {
            InstanceAccess.mc.thePlayer.safeWalk = this.safeWalk.getValue() && InstanceAccess.mc.thePlayer.onGround;

            //Used to detect when to place a block, if over air, allow placement of blocks
            if (PlayerUtil.blockRelativeToPlayer(0, -1, 0) instanceof BlockAir) {
                ticksOnAir++;
            } else {
                ticksOnAir = 0;
            }

            this.calculateSneaking();

            // Gets block to place
            targetBlock = PlayerUtil.getPlacePossibility(0, 0, 0, 5);

            if (targetBlock == null || targetBlock.yCoord > startY && !GameSettings.isKeyDown(InstanceAccess.mc.gameSettings.keyBindJump)) {
                if (InstanceAccess.mc.thePlayer.offGroundTicks >= sameYtellyTick.getValue().intValue()) {
                    RotationComponent.setRotations(new Vector2f(targetYaw, targetPitch), 10, movementCorrection.getValue());
                    return;
                }
            }

            //Gets EnumFacing
            enumFacing = PlayerUtil.getEnumFacing(targetBlock);

            if (enumFacing == null) {
                return;
            }

            final BlockPos position = new BlockPos(targetBlock.xCoord, targetBlock.yCoord, targetBlock.zCoord);
            blockFace = position.add(enumFacing.getOffset().xCoord, enumFacing.getOffset().yCoord, enumFacing.getOffset().zCoord);
            if (blockFace == null || enumFacing == null) {
                return;
            }

            this.calculateRotations();

            if (targetBlock == null || enumFacing == null || blockFace == null) {
                return;
            }

            if (this.sameY.getValue().getName().equals("Auto Jump")) {
                InstanceAccess.mc.gameSettings.keyBindJump.setPressed((InstanceAccess.mc.thePlayer.onGround && MoveUtil.isMoving()) || GameSettings.isKeyDown(InstanceAccess.mc.gameSettings.keyBindJump));
            }
            // Same Y
            final boolean sameY = ((!this.sameY.getValue().getName().equals("Off") || this.getModule(Speed.class).isEnabled()) && !GameSettings.isKeyDown(InstanceAccess.mc.gameSettings.keyBindJump)) && MoveUtil.isMoving();

            if (startY - 1 != Math.floor(targetBlock.yCoord) && sameY) {
                return;
            }
            if (mc.thePlayer.inventory.alternativeCurrentItem == SlotComponent.getItemIndex()) {
                if (!BadPacketsComponent.bad(false, true, false, false, true) &&
                        ticksOnAir > MathUtil.getRandom(placeDelay.getValue().intValue(), placeDelay.getSecondValue().intValue()) &&
                        (RayCastUtil.overBlock(enumFacing.getEnumFacing(), blockFace, rayCast.getValue().getName().equals("Strict")) || rayCast.getValue().getName().equals("Off"))) {

                    Vec3 hitVec = this.getHitVec();
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, SlotComponent.getItemStack(), blockFace, enumFacing.getEnumFacing(), hitVec)) {
                        if (noSwing.getValue()) PacketUtil.send(new C0APacketAnimation());
                        else InstanceAccess.mc.thePlayer.swingItem();
                    }

                    mc.rightClickDelayTimer = 0;
                    ticksOnAir = 0;

                    assert SlotComponent.getItemStack() != null;
                    if (SlotComponent.getItemStack() != null && SlotComponent.getItemStack().stackSize == 0) {
                        mc.thePlayer.inventory.mainInventory[SlotComponent.getItemIndex()] = null;
                    }
                }
            }

            if (InstanceAccess.mc.thePlayer != null && (InstanceAccess.mc.thePlayer.onGround || GameSettings.isKeyDown(InstanceAccess.mc.gameSettings.keyBindJump))) {
                startY = Math.floor(InstanceAccess.mc.thePlayer.posY);
            }

            if (InstanceAccess.mc.thePlayer != null && InstanceAccess.mc.thePlayer.posY < startY) {
                startY = InstanceAccess.mc.thePlayer.posY;
            }
        } finally {
            isWorking = false;
        }
    }


    public void getRotations() {
        final Vector2f rotations = RotationUtil.calculate(new com.alan.clients.util.vector.Vector3d(blockFace.getX(),
                blockFace.getY(), blockFace.getZ()), enumFacing.getEnumFacing());

        if (RotationMode.getValue() == Rotation.Lavender) {
                targetYaw = rotations.x;
                targetPitch = rotations.y;
        }
        if (RotationMode.getValue() == Rotation.New) {
            targetYaw = mc.thePlayer.rotationYaw;
            targetPitch = (rotations.y);
        }
        if (RotationMode.getValue() == Rotation.Rise) {
            boolean found = false;
            for (float possibleYaw = rotations.x; possibleYaw <= mc.thePlayer.rotationYaw + 360 - 180 && !found; possibleYaw += 45) {
                for (float possiblePitch = rotations.y; possiblePitch > 30 && !found; possiblePitch -= possiblePitch > (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 60 : 80) ? 1 : 10) {
                    if (RayCastUtil.overBlock(new Vector2f(possibleYaw, possiblePitch), enumFacing.getEnumFacing(), blockFace, true)) {
                        targetYaw = possibleYaw;
                        targetPitch = possiblePitch;
                        found = true;
                    }
                }
            }

            if (!found) {
                targetYaw = rotations.x;
                targetPitch = rotations.y;
            }
        }
        if (sprint.getValue().getName().equalsIgnoreCase("Watchdog")) {
            targetYaw += RandomUtil.nextInt(10, 20);
        }
    }

    public Vec3 getHitVec() {
        /* Correct HitVec */
        Vec3 hitVec = new Vec3(blockFace.getX() + Math.random(), blockFace.getY() + Math.random(), blockFace.getZ() + Math.random());

        final MovingObjectPosition movingObjectPosition = RayCastUtil.rayCast(RotationComponent.rotations, mc.playerController.getBlockReachDistance());

        switch (enumFacing.getEnumFacing()) {
            case DOWN:
                hitVec.yCoord = blockFace.getY();
                break;

            case UP:
                hitVec.yCoord = blockFace.getY() + 1;
                break;

            case NORTH:
                hitVec.zCoord = blockFace.getZ();
                break;

            case EAST:
                hitVec.xCoord = blockFace.getX() + 1;
                break;

            case SOUTH:
                hitVec.zCoord = blockFace.getZ() + 1;
                break;

            case WEST:
                hitVec.xCoord = blockFace.getX();
                break;
        }

        if (movingObjectPosition != null && movingObjectPosition.getBlockPos().equals(blockFace) &&
                movingObjectPosition.sideHit == enumFacing.getEnumFacing()) {
            hitVec = movingObjectPosition.hitVec;
        }
        return hitVec;
    }

    @EventLink()
    public final Listener<MoveInputEvent> onMove = this::calculateSneaking;

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (isNull() ) return;
        if (Scaffold.mc.thePlayer == null) {
            return;
        }
        if (this.eagle.getValue()) {
            if (Eagle.getBlockUnderPlayer(Scaffold.mc.thePlayer) instanceof BlockAir) {
                if (Scaffold.mc.thePlayer.onGround) {
                    KeyBinding.setKeyBindState(Scaffold.mc.gameSettings.keyBindSneak.getKeyCode(), true);
                }
            }
            else if (Scaffold.mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(Scaffold.mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }
        }
    };

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (isNull()) return;
        // Getting ItemSlot
        SlotComponent.setSlot(SlotUtil.findBlock(), render.getValue()); // it must work in PreUpdate.
        if (placeTime.getValue().getName().equalsIgnoreCase("Pre"))
            work();
    };

    @EventLink
    private final Listener<PostMotionEvent> onPostMotion = event -> {
        if (isNull()) return;
        if (placeTime.getValue().getName().equalsIgnoreCase("Post"))
            work();
    };

    @EventLink
    private final Listener<PossibleClickEvent> onPossibleClick = event -> {
        if (isNull()) return;
        if (placeTime.getValue().getName().equalsIgnoreCase("Legit"))
            work();
    };

    @EventLink
    private final Listener<TickEvent> onTick = event -> {
        if (isNull()) return;
        if (placeTime.getValue().getName().equalsIgnoreCase("Tick"))
            work();
        if (Client.name == null) System.exit(-1);
    };

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {
        this.runMode();
        if (movementCorrection.getValue() == MovementFix.OFF) {
            MoveUtil.useDiagonalSpeed();
        }
    };

    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (!markValue.getValue())
            return;
        if (targetBlock == null)return;
        for (int i = 0; i < (2); i++) {
            final BlockPos blockPos = new BlockPos(InstanceAccess.mc.thePlayer.posX + (InstanceAccess.mc.thePlayer.getHorizontalFacing() == EnumFacing.WEST ? -i : InstanceAccess.mc.thePlayer.getHorizontalFacing() == EnumFacing.EAST ? i : 0), InstanceAccess.mc.thePlayer.posY - (InstanceAccess.mc.thePlayer.posY == (int) InstanceAccess.mc.thePlayer.posY + 0.5D ? 0D : 1.0D) - ( 0), InstanceAccess.mc.thePlayer.posZ + (InstanceAccess.mc.thePlayer.getHorizontalFacing() == EnumFacing.NORTH ? -i : InstanceAccess.mc.thePlayer.getHorizontalFacing() == EnumFacing.SOUTH ? i : 0));
            if (BlockUtil.isReplaceable(blockPos)) {
                RenderUtil.drawBlockBox(blockPos, new Color(246, 0, 0, 255), false);
                break;
            }
        }
    };

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> this.setEnabled(false);

    public int getBlockCount() {
        int n = 0;
        for (int i = 36; i < 45; ++i) {
            if (Scaffold.mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack stack = Scaffold.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                final Item item = stack.getItem();
                if (stack.getItem() instanceof ItemBlock && this.isValid(item)) {
                    n += stack.stackSize;
                }
            }
        }
        return n;
    }
    private boolean isValid(final Item item) {
        return item instanceof ItemBlock && !Scaffold.invalidBlocks.contains(((ItemBlock)item).getBlock());
    }
}
