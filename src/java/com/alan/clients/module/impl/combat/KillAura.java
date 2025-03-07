package com.alan.clients.module.impl.combat;

import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.component.impl.hypixel.InventoryDeSyncComponent;
import com.alan.clients.component.impl.player.BadPacketsComponent;
import com.alan.clients.component.impl.player.GUIDetectionComponent;
import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.component.impl.player.SlotComponent;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.component.impl.render.ESPComponent;
import com.alan.clients.component.impl.render.espcomponent.api.ESPColor;
import com.alan.clients.component.impl.render.espcomponent.impl.AboveBox;
import com.alan.clients.component.impl.render.espcomponent.impl.BoxGlow;
import com.alan.clients.component.impl.render.espcomponent.impl.FullBox;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.other.Stuck;
import com.alan.clients.module.impl.player.Blink;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.Priorities;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.input.ClickEvent;
import com.alan.clients.newevent.impl.motion.PostMotionEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.motion.SlowDownEvent;
import com.alan.clients.newevent.impl.other.AttackEvent;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.newevent.impl.render.MouseOverEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.newevent.impl.render.RenderItemEvent;
import com.alan.clients.noti.utils.Animation;
import com.alan.clients.noti.utils.DecelerateAnimation;
import com.alan.clients.noti.utils.Direction;
import com.alan.clients.util.RandomUtil;
import com.alan.clients.util.RayCastUtil;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.rotation.RotationUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.impl.*;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.viamcp.ViaMCP;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;
import org.lwjgl.opengl.GL11;
import util.time.StopWatch;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alan
 * @since 11/17/2021
 */
@Rise
@ModuleInfo(name = "module.combat.killaura.name", description = "module.combat.killaura.description", category = Category.COMBAT)
public final class KillAura extends Module {

    private final ModeValue mode = new ModeValue("Attack Mode", this)
            .add(new SubMode("Single"))
            .add(new SubMode("Switch"))
            .add(new SubMode("Multiple"))
            .setDefault("Single");

    private final BoundsNumberValue switchTicks = new BoundsNumberValue("Switch Ticks", this, 100, 1000, 0, 2000, 1);

    public final ModeValue autoBlock = new ModeValue("Auto Block", this)
            .add(new SubMode("None"))
            .add(new SubMode("Fake"))
            .add(new SubMode("Vanilla"))
            .add(new SubMode("NCP"))
            .add(new SubMode("Watchdog"))
            .add(new SubMode("Watchdog HvH"))
            .add(new SubMode("GrimAC"))
            .add(new SubMode("Legit"))
            .add(new SubMode("Intave"))
            .add(new SubMode("Old Intave"))
            .add(new SubMode("Imperfect Vanilla"))
            .add(new SubMode("Vanilla ReBlock"))
            .add(new SubMode("New NCP"))
            .setDefault("None");

    private final ModeValue clickMode = new ModeValue("Click Delay Mode", this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Hit Select"))
            .add(new SubMode("1.9+"))
            .add(new SubMode("1.9+ (1.8 Visuals)"))
            .setDefault("Normal");

    public final NumberValue range = new NumberValue("Range", this, 3, 3, 6, 0.1);
    private final BoundsNumberValue cps = new BoundsNumberValue("CPS", this, 10, 15, 1, 20, 1);
    private final BoundsNumberValue rotationSpeed = new BoundsNumberValue("Rotation speed", this, 5, 10, 0, 10, 1);
    private final ListValue<MovementFix> movementCorrection = new ListValue<>("Movement correction", this);
    private final BooleanValue keepSprint = new BooleanValue("Keep sprint", this, false);

    private final BooleanValue circle = new BooleanValue("Circle", this, true);
    private final BooleanValue targetcircle = new BooleanValue("Target Circle", this, true);
    private final BooleanValue tracer = new BooleanValue("Tracer",this,true);
    private final ModeValue espMode = new ModeValue("Target ESP Mode", this)
            .add(new SubMode("Ring"))
            .add(new SubMode("Box"))
            .add(new SubMode("None"))
            .setDefault("Ring");

    public final ModeValue boxMode = new ModeValue("Box Mode", this, () -> !(espMode.getValue()).getName().equals("Box"))
            .add(new SubMode("Above"))
            .add(new SubMode("Full"))
            .add(new SubMode("Glow"))
            .setDefault("Ring");

    private final BooleanValue rayCast = new BooleanValue("Ray cast", this, false);

    private final BooleanValue advanced = new BooleanValue("Advanced", this, false);
    private final BooleanValue lookAtTheClosestPoint = new BooleanValue("Look at the closest point on the player", this, true, () -> !advanced.getValue());
    private final BooleanValue subTicks = new BooleanValue("Attack outside ticks", this, false, () -> !advanced.getValue());
    private final StringValue runMovementFixIfNot = new StringValue("Exclude MovementCorrection if", this, "", () -> !advanced.getValue());
    private final ModeValue rotationMode = new ModeValue("Rotation Mode", this, () -> !advanced.getValue())
            .add(new SubMode("Legit/Normal"))
            .add(new SubMode("Autistic AntiCheat"))
            .setDefault("Legit/Normal");
    private final BooleanValue attackWhilstScaffolding = new BooleanValue("Attack whilst Scaffolding", this, false, () -> !advanced.getValue());
    private final BooleanValue noSwing = new BooleanValue("No swing", this, false, () -> !advanced.getValue());
    private final BooleanValue autoDisable = new BooleanValue("Auto disable", this, false, () -> !advanced.getValue());
    private final BooleanValue grimFalse = new BooleanValue("Prevent Grim false positives", this, false, () -> !advanced.getValue());

    private final BooleanValue showTargets = new BooleanValue("Targets", this, false);
    public final BooleanValue player = new BooleanValue("Player", this, false, () -> !showTargets.getValue());
    public final BooleanValue invisibles = new BooleanValue("Invisibles", this, false, () -> !showTargets.getValue());
    public final BooleanValue animals = new BooleanValue("Animals", this, false, () -> !showTargets.getValue());
    public final BooleanValue mobs = new BooleanValue("Mobs", this, false, () -> !showTargets.getValue());
    public final BooleanValue villagers = new BooleanValue("Villagers",this,false, () -> !showTargets.getValue());
    public final BooleanValue teams = new BooleanValue("Teams", this, false, () -> !showTargets.getValue());


    private final StopWatch attackStopWatch = new StopWatch();
    private final StopWatch clickStopWatch = new StopWatch();

    private float randomYaw;
    private float randomPitch;
    public boolean blocking, swing, allowAttack;
    private EntityLivingBase auraESPTarget;
    private long nextSwing;

    public static List<Entity> targets;
    public Entity target;

    public StopWatch subTicksStopWatch = new StopWatch();
    public StopWatch switchChangeTicks = new StopWatch();
    private int attack, hitTicks, expandRange;


    public KillAura() {
        for (MovementFix movementFix : MovementFix.values()) {
            movementCorrection.add(movementFix);
        }

        movementCorrection.setDefault(MovementFix.OFF);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (isNull()) return;
        this.hitTicks++;

        if (GUIDetectionComponent.inGUI()) {
            return;
        }

        if (target == null || InstanceAccess.mc.thePlayer.isDead || this.getModule(Scaffold.class).isEnabled()|| this.getModule(Blink.class).isEnabled() || this.getModule(Stuck.class).isEnabled()) {
            this.unblock();
            target = null;
        }
    };

    @Override
    protected void onEnable() {
        this.attack = 0;
        this.switchChangeTicks.reset();
    }

    @Override
    public void onDisable() {
        target = null;
        if (InstanceAccess.mc.thePlayer != null && blocking) unblock();
    }

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        if (this.autoDisable.getValue()) {
            this.toggle();
        }
    };

    public void getTargets(double range) {
        Teams teams1 = Client.INSTANCE.getModuleManager().get(Teams.class);
        AntiBot antiBot = Client.INSTANCE.getModuleManager().get(AntiBot.class);

        targets = InstanceAccess.mc.theWorld.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase && entity != InstanceAccess.mc.thePlayer)
                .filter(entity -> !entity.isDead && ((EntityLivingBase) entity).deathTime == 0)
                .filter(entity -> InstanceAccess.mc.thePlayer.getDistanceToEntity(entity) <= range)
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
                .sorted(Comparator.comparingDouble(entity -> InstanceAccess.mc.thePlayer.getDistanceToEntity(entity)))
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


    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (isNull()) return;
        if (InstanceAccess.mc.thePlayer.getHealth() <= 0.0 && this.autoDisable.getValue()) {
            this.toggle();
        }

        if (getModule(Scaffold.class).isEnabled() && !attackWhilstScaffolding.getValue() || this.getModule(Stuck.class).isEnabled()|| this.getModule(Blink.class).isEnabled()||
                (InstanceAccess.mc.gameSettings.keyBindAttack.isKeyDown() && InstanceAccess.mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.BLOCK))) {
            this.unblock();
            return;
        }
        this.attack = Math.max(Math.min(this.attack, this.attack - 2), 0);

        /*
         * Historic fix
         */
        if (InstanceAccess.mc.thePlayer.ticksExisted % 20 == 0) {
            expandRange = (int) (2 + Math.random() * 3);
        }

        if (GUIDetectionComponent.inGUI()) {
            return;
        }

        /*
         * Getting targets and selecting the nearest one
         */
        this.getTargets(range.getValue().doubleValue() + expandRange);

        if (targets.isEmpty()) {
            this.randomiseTargetRotations();
            target = null;
            return;
        }

        if (mode.getValue().getName().equalsIgnoreCase("Single"))
            target = targets.get(0);
        else
        if (this.switchChangeTicks.finished(RandomUtil.nextInt(switchTicks.getMin().intValue(), switchTicks.getMax().intValue())) && targets.size() > 1) {
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

        if (target == null || InstanceAccess.mc.thePlayer.isDead) {
            this.randomiseTargetRotations();
            return;
        }

        Color color = getTheme().getAccentColor(new Vector2d(0, 500));

        if (espMode.getValue().getName().equals("Box")) {
            switch (boxMode.getValue().getName()) {
                case "Glow":
                    ESPComponent.add(new BoxGlow(new ESPColor(color, color, color)));
                case "Full":
                    ESPComponent.add(new FullBox(new ESPColor(color, color, color)));
                    break;
                case "Above":
                    ESPComponent.add(new AboveBox(new ESPColor(color, color, color)));
                    break;
            }
        }

        if (this.canBlock() && target.getDistanceToEntity(InstanceAccess.mc.thePlayer) <= range.getValue().intValue()) {
            this.preBlock();
        }

        /*
         * Calculating rotations to target
         */
        this.rotations();

        /*
         * Doing the attack
         */
        this.doAttack(targets);

        /*
         * Blocking
         */
        if (this.canBlock() && target.getDistanceToEntity(InstanceAccess.mc.thePlayer) <= range.getValue().intValue()) {
            this.postAttackBlock();
        }
    };

    public void rotations() {
        if (InstanceAccess.mc.thePlayer.getDistanceToEntity(target) > range.getValue().doubleValue()) return;
        final double minRotationSpeed = this.rotationSpeed.getValue().doubleValue();
        final double maxRotationSpeed = this.rotationSpeed.getSecondValue().doubleValue();
        final float rotationSpeed = (float) MathUtil.getRandom(minRotationSpeed, maxRotationSpeed);

        switch (rotationMode.getValue().getName()) {
            case "Legit/Normal":
                final Vector2f targetRotations = RotationUtil.calculate(target, lookAtTheClosestPoint.getValue(), range.getValue().doubleValue());

                this.randomiseTargetRotations();

                targetRotations.x += randomYaw;
                targetRotations.y += randomPitch;

                if (RayCastUtil.rayCast(targetRotations, range.getValue().doubleValue()).typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                    randomYaw = randomPitch = 0;
                }

                if (rotationSpeed != 0) {
                    RotationComponent.setRotations(targetRotations, rotationSpeed,
                            movementCorrection.getValue() == MovementFix.OFF || shouldRun() ? MovementFix.OFF : movementCorrection.getValue());
                }
                break;

            case "Autistic AntiCheat":
                double speed = rotationSpeed * 10;
                RotationComponent.setRotations(new Vector2f((float) (RotationComponent.rotations.x + speed), 0), speed / 18,
                        movementCorrection.getValue() == MovementFix.OFF || shouldRun() ? MovementFix.OFF : movementCorrection.getValue());
                break;
        }

    }

    public boolean shouldRun() {
        // If you're Tecnio don't scroll down
        String userEnteredCode = runMovementFixIfNot.getValue();

        // Legit no one can bypass this to make a rce
        if (userEnteredCode.length() > 60 || userEnteredCode.length() <= 1 || userEnteredCode.contains(";") || userEnteredCode.contains(".")) {
            return false;
        }

        // I don't think you could write something more scuffed if you tried
        String script =
                // Don't kill me please
                "boolean onGround = " + InstanceAccess.mc.thePlayer.onGround + ";" +
                        "boolean ground = onGround;" +

                        "int ticksOnGround = " + InstanceAccess.mc.thePlayer.onGroundTicks + ";" +
                        "int onGroundTicks = ticksOnGround;" +
                        "int groundTicks = ticksOnGround;" +

                        "int ticksInAir = " + InstanceAccess.mc.thePlayer.offGroundTicks + ";" +
                        "int airTicks = ticksInAir;" +
                        "int ticksOffGround = ticksInAir;" +

                        "int ticksSinceVelocity = " + InstanceAccess.mc.thePlayer.ticksSinceVelocity + ";" +
                        "int velocityTicks = ticksSinceVelocity;" +

                        "boolean runIf = " + userEnteredCode + ";" +

                        "System.out.println(runIf);";

        ScriptEvaluator scriptEvaluator = new ScriptEvaluator();

        // Preserve current console which contains.
        PrintStream previousConsole = System.out;

        // Set the standard output to use newConsole.
        ByteArrayOutputStream newConsole = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newConsole));

        try {
            scriptEvaluator.cook(script);
            scriptEvaluator.evaluate(new Object[0]);
        } catch (CompileException | InvocationTargetException e) {
            return false;
        }

        boolean result = newConsole.toString().contains("true");

        System.setOut(previousConsole);

        return result;
    }

    /*
     * Randomising rotation target to simulate legit players
     */
    private void randomiseTargetRotations() {
        randomYaw += (float) (Math.random() - 0.5f);
        randomPitch += (float) (Math.random() - 0.5f) * 2;
    }

    @EventLink
    public final Listener<MouseOverEvent> onMouseOver = event -> event.setRange(event.getRange() + range.getValue().doubleValue() - 3);

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        if (isNull()) return;
        if (target != null && this.canBlock() && target.getDistanceToEntity(InstanceAccess.mc.thePlayer) <= range.getValue().intValue()) {
            this.postBlock();
        }
    };

    private void doAttack(final List<Entity> targets) {
        String autoBlock = this.autoBlock.getValue().getName();
        if (BadPacketsComponent.bad(false, true, true, true, true) &&
                (autoBlock.equals("Fake") || autoBlock.equals("None") ||
                        autoBlock.equals("Imperfect Vanilla") || autoBlock.equals("Vanilla ReBlock"))) {
            return;
        }

        double delay = -1;
        boolean flag = false;

        switch (clickMode.getValue().getName()) {
            case "Hit Select": {
                delay = 9;
                flag = target.hurtResistantTime <= 10;
                break;
            }

            case "1.9+": {
                double speed = 4;

                if (InstanceAccess.mc.thePlayer.getHeldItem() != null) {
                    final Item item = InstanceAccess.mc.thePlayer.getHeldItem().getItem();

                    if (item instanceof ItemSword) {
                        speed = 1.6;
                    } else if (item instanceof ItemSpade) {
                        speed = 1;
                    } else if (item instanceof ItemPickaxe) {
                        speed = 1.2;
                    } else if (item instanceof ItemAxe) {
                        switch (((ItemAxe) item).getToolMaterial()) {
                            case WOOD:
                            case STONE:
                                speed = 0.8;
                                break;

                            case IRON:
                                speed = 0.9;
                                break;

                            default:
                                speed = 1;
                                break;
                        }
                    } else if (item instanceof ItemHoe) {
                        switch (((ItemHoe) item).getToolMaterial()) {
                            case WOOD:
                            case GOLD:
                                speed = 1;
                                break;

                            case STONE:
                                speed = 2;
                                break;

                            case IRON:
                                speed = 3;
                                break;
                        }
                    }
                }

                delay = 1 / speed * 20 - 1;
                break;
            }
        }

        if (attackStopWatch.finished(this.nextSwing) && (!grimFalse.getValue() || !(InstanceAccess.mc.thePlayer.ticksSprint <= 1 && InstanceAccess.mc.thePlayer.isSprinting())) && !BadPacketsComponent.bad(false, true, true, false, true) && target != null && (clickStopWatch.finished((long) (delay * 50)) || flag)) {
            final long clicks = (long) (Math.round(MathUtil.getRandom(this.cps.getValue().intValue(), this.cps.getSecondValue().intValue())) * 1.5);
            this.nextSwing = 1000 / clicks;

            if (Math.sin(nextSwing) + 1 > Math.random() || attackStopWatch.finished(this.nextSwing + 500) || Math.random() > 0.5) {
                this.allowAttack = true;

                if (this.canBlock() && target.getDistanceToEntity(InstanceAccess.mc.thePlayer) <= range.getValue().intValue()) {
                    this.attackBlock();
                }

                if (this.allowAttack) {
                    /*
                     * Attacking target
                     */
                    final double range = this.range.getValue().doubleValue();
                    final MovingObjectPosition movingObjectPosition = InstanceAccess.mc.objectMouseOver;

                    switch (this.mode.getValue().getName()) {
                        case "Single":
                        case "Switch": {
                            if ((InstanceAccess.mc.thePlayer.getDistanceToEntity(target) <= range && !rayCast.getValue()) ||
                                    (rayCast.getValue() && movingObjectPosition != null && movingObjectPosition.entityHit == target)) {
                                this.attack(target);
                            } else if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                                this.attack(movingObjectPosition.entityHit);
                            } else {
                                switch (clickMode.getValue().getName()) {
                                    case "Normal":
                                    case "Hit Select":
                                        PacketUtil.send(new C0APacketAnimation());
                                        this.clickStopWatch.reset();
                                        this.hitTicks = 0;
                                        break;
                                }
                            }
                            break;
                        }

                        case "Multiple": {
                            targets.removeIf(target -> InstanceAccess.mc.thePlayer.getDistanceToEntity(target) > range);

                            if (!targets.isEmpty()) {
                                targets.forEach(this::attack);
                            }
                            break;
                        }
                    }
                }

                this.attackStopWatch.reset();
            }
        }
    }

    private void attack(final Entity target) {
        this.attack = Math.min(Math.max(this.attack, this.attack + 2), 5);

        Client.INSTANCE.getEventBus().handle(new ClickEvent());
        if (!this.noSwing.getValue()) InstanceAccess.mc.thePlayer.swingItem();

        final AttackEvent event = new AttackEvent(target);
        Client.INSTANCE.getEventBus().handle(event);

        if (!event.isCancelled()) {
            if (noSwing.getValue()) PacketUtil.sendNoEvent(new C0APacketAnimation());
            else InstanceAccess.mc.thePlayer.swingItem();

            if (this.keepSprint.getValue()) {
                InstanceAccess.mc.playerController.syncCurrentPlayItem();

                PacketUtil.send(new C02PacketUseEntity(event.getTarget(), C02PacketUseEntity.Action.ATTACK));

                if (InstanceAccess.mc.thePlayer.fallDistance > 0 && !InstanceAccess.mc.thePlayer.onGround && !InstanceAccess.mc.thePlayer.isOnLadder() && !InstanceAccess.mc.thePlayer.isInWater() && !InstanceAccess.mc.thePlayer.isPotionActive(Potion.blindness) && InstanceAccess.mc.thePlayer.ridingEntity == null) {
                    InstanceAccess.mc.thePlayer.onCriticalHit(target);
                }
            } else {
                InstanceAccess.mc.playerController.attackEntity(InstanceAccess.mc.thePlayer, target);
            }
        }

        this.clickStopWatch.reset();
        this.hitTicks = 0;

//        if (!pastTargets.contains(target)) pastTargets.add(target);
    }

    private void block(final boolean check, final boolean interact) {
        if (!blocking || !check) {
            if (interact && target != null && InstanceAccess.mc.objectMouseOver.entityHit == target) {
                InstanceAccess.mc.playerController.interactWithEntitySendPacket(InstanceAccess.mc.thePlayer, target);
            }
            PacketUtil.send(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
            blocking = true;
        }
    }

    private void unblock() {
        if (blocking) {
            if (!InstanceAccess.mc.gameSettings.keyBindUseItem.isKeyDown()) {
                PacketUtil.sendNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            } else {
                InstanceAccess.mc.gameSettings.keyBindUseItem.setPressed(false);
            }
            blocking = false;
        }
    }

    @EventLink(value = Priorities.HIGH)
    public final Listener<RenderItemEvent> onRenderItem = event -> {
        if (target != null && !autoBlock.getValue().getName().equals("None") && this.canBlock()) {
            event.setEnumAction(EnumAction.BLOCK);
            event.setUseItem(true);
        }
    };
    private final Animation auraESPAnim = new DecelerateAnimation(300, 1);
    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (isNull()) return;
        auraESPAnim.setDirection(target != null ? Direction.FORWARDS : Direction.BACKWARDS);
        if(target != null) {
            auraESPTarget = (EntityLivingBase) target;
        }

        if(auraESPAnim.finished(Direction.BACKWARDS)) {
            auraESPTarget = null;
        }
        if (auraESPTarget != null) {
            if (tracer.getValue()) {
                RenderUtil.drawTracerLine(auraESPTarget, 4f, Color.BLACK, (float) auraESPAnim.getOutput());
                RenderUtil.drawTracerLine(auraESPTarget, 2.5f, getTheme().getFirstColor(), (float) auraESPAnim.getOutput());
            }
            if (espMode.getValue().getName().equals("Ring")) {
                RenderUtil.drawCircle(auraESPTarget, event.getPartialTicks(), .75f, getTheme().getAccentColor().getRGB(), (float) auraESPAnim.getOutput());
            }
        }
        if (circle.getValue()) {
            Color color = getTheme().getFirstColor();
            GL11.glPushMatrix();
            GL11.glTranslated(KillAura.mc.thePlayer.lastTickPosX + (KillAura.mc.thePlayer.posX - KillAura.mc.thePlayer.lastTickPosX) * (double) KillAura.mc.timer.renderPartialTicks - KillAura.mc.getRenderManager().renderPosX, KillAura.mc.thePlayer.lastTickPosY + (KillAura.mc.thePlayer.posY - KillAura.mc.thePlayer.lastTickPosY) * (double) KillAura.mc.timer.renderPartialTicks - KillAura.mc.getRenderManager().renderPosY, KillAura.mc.thePlayer.lastTickPosZ + (KillAura.mc.thePlayer.posZ - KillAura.mc.thePlayer.lastTickPosZ) * (double) KillAura.mc.timer.renderPartialTicks - KillAura.mc.getRenderManager().renderPosZ);
            GL11.glEnable(3042);
            GL11.glEnable(2848);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glBlendFunc(770, 771);
            GL11.glLineWidth(1.8f);
            ColorUtil.glColor(color);
            GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
            GL11.glBegin(3);
            int i = 0;
            while (i <= 360) {
                GL11.glVertex2f((float) (Math.cos((double) i * Math.PI / 180.0) * (double) this.range.getValue().floatValue()), (float) (Math.sin((double) i * Math.PI / 180.0) * (double) this.range.getValue().floatValue()));
                i = (int) ((double) i + (61.0 - 6));
            }
            GL11.glVertex2f((float) (Math.cos(Math.PI * 2) * (double) this.range.getValue().floatValue()), (float) (Math.sin(Math.PI * 2) * (double) this.range.getValue().floatValue()));
            GL11.glEnd();
            GL11.glDisable(3042);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDisable(2848);
            GL11.glPopMatrix();
        }
        if (targetcircle.getValue() && target != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(this.target.lastTickPosX + (this.target.posX - this.target.lastTickPosX) * (double) KillAura.mc.timer.renderPartialTicks - KillAura.mc.getRenderManager().renderPosX, this.target.lastTickPosY + (this.target.posY - this.target.lastTickPosY) * (double) KillAura.mc.timer.renderPartialTicks - KillAura.mc.getRenderManager().renderPosY, this.target.lastTickPosZ + (this.target.posZ - this.target.lastTickPosZ) * (double) KillAura.mc.timer.renderPartialTicks - KillAura.mc.getRenderManager().renderPosZ);
            GL11.glEnable(3042);
            GL11.glEnable(2848);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glBlendFunc(770, 771);
            GL11.glLineWidth(1.8f);
            ColorUtil.glColor(new Color(227, 227, 227));
            GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
            GL11.glBegin(3);
            int i = 0;
            while (i <= 360) {
                GL11.glVertex2f((float)(Math.cos((double)i * Math.PI / 180.0) * (double) this.range.getValue().floatValue()), (float)(Math.sin((double)i * Math.PI / 180.0) * (double) this.range.getValue().floatValue()));
                i = (int)((double)i + (61.0 - 33));
            }
            GL11.glVertex2f((float) (Math.cos(Math.PI * 2) * (double) this.range.getValue().floatValue()), (float) (Math.sin(Math.PI * 2) * (double) this.range.getValue().floatValue()));
            GL11.glEnd();
            GL11.glDisable(3042);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDisable(2848);
            GL11.glPopMatrix();
        }
    };
    @EventLink()
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C0APacketAnimation) {
            swing = true;
        } else if (packet instanceof C03PacketPlayer) {
            swing = false;
        }

        this.packetBlock(event);
    };

    public void packetBlock(final PacketSendEvent event) {
        final Packet<?> packet = event.getPacket();

        switch (autoBlock.getValue().getName()) {
            case "Intave":
                if (packet instanceof C03PacketPlayer) {
                    event.setCancelled(true);
                    this.unblock();
                    PacketUtil.sendNoEvent(packet);
                    this.block(false, true);
                }
                break;

            case "Fake":
            case "None":
                if (SlotComponent.getItemStack() == null || !(SlotComponent.getItemStack().getItem() instanceof ItemSword)) {
                    return;
                }

                if (packet instanceof C08PacketPlayerBlockPlacement) {
                    final C08PacketPlayerBlockPlacement wrapper = (C08PacketPlayerBlockPlacement) packet;

                    if (wrapper.getPlacedBlockDirection() == 255) {
                        event.setCancelled(true);
                    }
                } else if (packet instanceof C07PacketPlayerDigging) {
                    C07PacketPlayerDigging wrapper = ((C07PacketPlayerDigging) packet);

                    if (wrapper.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                        event.setCancelled(true);
                    }
                }
                break;
        }
    }

    private void attackBlock() {
        if ("Legit".equals(autoBlock.getValue().getName())) {
            if (InstanceAccess.mc.gameSettings.keyBindUseItem.isKeyDown()) {
                InstanceAccess.mc.gameSettings.keyBindUseItem.setPressed(false);
            }


            this.allowAttack = !BadPacketsComponent.bad(false, false, false, true, false);
        }
    }

    private void postAttackBlock() {
        switch (autoBlock.getValue().getName()) {
            case "Legit":
                if (this.hitTicks == 1) {
                    InstanceAccess.mc.gameSettings.keyBindUseItem.setPressed(true);
                    this.blocking = true;
                }
                break;

            case "Intave":
                this.block(false, false);
                break;

            case "Vanilla":
                if (this.hitTicks != 0) {
                    this.block(false, true);
                }
                break;

            case "Imperfect Vanilla":
                if (this.hitTicks == 1 && InstanceAccess.mc.thePlayer.isSwingInProgress && Math.random() > 0.1) {
                    this.block(false, true);
                }
                break;

            case "Vanilla ReBlock":
                if (this.hitTicks == 1 || !this.blocking) {
                    this.block(false, true);
                }
                break;

            case "Watchdog HvH":
                InstanceAccess.mc.gameSettings.keyBindUseItem.setPressed(true);
                if ((this.hitTicks == 1 || !this.blocking) && !BadPacketsComponent.bad(false, true, true, true, false)) {
                    this.block(false, true);
                }
                break;

            case "GrimAC":
                if (InstanceAccess.mc.thePlayer.getHeldItem() != null && InstanceAccess.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                    InstanceAccess.mc.gameSettings.keyBindUseItem.setPressed(true);
                    InstanceAccess.mc.thePlayer.itemInUse = InstanceAccess.mc.thePlayer.getHeldItem();
                    blocking = true;
                }
                break;
        }
    }

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<SlowDownEvent> onSlowDown = event -> {
        if (autoBlock.getValue().getName().equals("Watchdog HvH")) {
            event.setCancelled(false);
            event.setStrafeMultiplier(0.2F);
            event.setForwardMultiplier(0.2F);
        }
    };

    private void preBlock() {
        switch (autoBlock.getValue().getName()) {
            case "NCP":
            case "Intave":
                this.unblock();
                break;

            case "New NCP":
                if (this.blocking) {
                    PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
                    PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
                    this.blocking = false;
                }
                break;

            case "Old Intave":
//                InventoryDeSyncComponent.setActive("/booster");

                if (InstanceAccess.mc.thePlayer.isUsingItem()) {
                    PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
                    PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
                }

                break;
            case "Watchdog":
                if ((InstanceAccess.mc.thePlayer.getDistanceToEntity(target) <= range.getValue().doubleValue() && !rayCast.getValue()) ||
                        (rayCast.getValue() && InstanceAccess.mc.objectMouseOver != null && InstanceAccess.mc.objectMouseOver.entityHit == target) || (InstanceAccess.mc.objectMouseOver != null && InstanceAccess.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)) {
                    if (InstanceAccess.mc.thePlayer.ticksExisted % 4 == 0) {
                        PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
                        PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
                        PacketUtil.send(new C08PacketPlayerBlockPlacement(InstanceAccess.mc.thePlayer.getHeldItem()));
                        blocking = true;
                    }
                } else
                    this.unblock();

                break;
            case "GrimAC":
                if (ViaMCP.getInstance().getVersion() <= 47){
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                    break;
                }
                if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                    PacketUtil.sendPacketC0F();
                    PacketWrapper useItem_1_9 = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                    useItem_1_9.write(Type.VAR_INT, 1);
                    com.viaversion.viarewind.utils.PacketUtil.sendToServer(useItem_1_9, Protocol1_8To1_9.class, true, true);
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                    blocking = true;
                }
                break;
        }
    }

    private void postBlock() {
        switch (autoBlock.getValue().getName()) {
            case "NCP":
            case "New NCP":
                this.block(true, false);
                break;

            case "Old Intave":
                if (InstanceAccess.mc.thePlayer.isUsingItem() && InventoryDeSyncComponent.isDeSynced()) {
                    PacketUtil.send(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
                }
                break;
        }
    }

    private boolean canBlock() {
        return SlotComponent.getItemStack() != null && SlotComponent.getItemStack().getItem() instanceof ItemSword;
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (this.subTicks.getValue() && this.attack <= 5 && target != null && this.subTicksStopWatch.finished(10)) {
            this.subTicksStopWatch.reset();

            /*
             * Getting targets and selecting the nearest one
             */
            targets = Client.INSTANCE.getTargetManager().getTargets(range.getValue().doubleValue() + expandRange);

            if (targets.isEmpty()) {
                this.randomiseTargetRotations();
                target = null;
                return;
            }

            this.doAttack(targets);
        }
    };
}
