package com.alan.clients.module.impl.render;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.other.AttackEvent;
import com.alan.clients.noti.NotificationManager;
import com.alan.clients.noti.NotificationType;
import com.alan.clients.util.animation.impl.ContinualAnimation;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.sound.SoundUtil;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.EnumParticleTypes;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;

@ModuleInfo(name = "module.render.killeffect.name", description = "module.render.killeffect.description", category = Category.RENDER)
public final class KillEffect extends Module {
    private final BooleanValue tipsKillsValue = new BooleanValue("TipsKills", this, true);
    private final BooleanValue lightning = new BooleanValue("Lightning", this, true);
    private final BooleanValue explosion = new BooleanValue("Explosion", this, false);
    private final BooleanValue squids = new BooleanValue("Squid", this, true);
    private final BooleanValue killSoundValue = new BooleanValue("KillSound", this, true);
    public EntitySquid squid;
    private double percent = 0.0;
    public static int kills = 0;
    private EntityLivingBase target;
    private final ContinualAnimation anim = new ContinualAnimation();

    public double easeInOutCirc(double x) {
        return x < 0.5 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * x, 2.0))) / 2.0 : (Math.sqrt(1.0 - Math.pow(-2.0 * x + 2.0, 2.0)) + 1.0) / 2.0;
    }

    @Override
    protected void onEnable() {
        kills = 0;
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        kills = 0;
        super.onDisable();
    }

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUPdateEvent = event -> {
        if (isNull()) return;
        if (this.squids.getValue() && this.squid != null) {
            if (KillEffect.mc.theWorld.loadedEntityList.contains(this.squid)) {
                if (this.percent < 1.0) {
                    this.percent += Math.random() * 0.048;
                }
                if (this.percent >= 1.0) {
                    this.percent = 0.0;
                    for (int i = 0; i <= 8; ++i) {
                        KillEffect.mc.effectRenderer.emitParticleAtEntity(this.squid, EnumParticleTypes.FLAME);
                    }
                    KillEffect.mc.theWorld.removeEntity(this.squid);
                    this.squid = null;
                    return;
                }
            } else {
                this.percent = 0.0;
            }
            double easeInOutCirc = this.easeInOutCirc(1.0 - this.percent);
            this.anim.animate((float) easeInOutCirc, 450);
            this.squid.setPositionAndUpdate(this.squid.posX, this.squid.posY + (double) this.anim.getOutput() * 0.9, this.squid.posZ);
        }
        if (this.squid != null && squids.getValue()) {
            this.squid.squidPitch = 0.0f;
            this.squid.prevSquidPitch = 0.0f;
            this.squid.squidYaw = 0.0f;
            this.squid.squidRotation = 90.0f;
        }
        if (this.target != null && this.target.getHealth() <= 0.0f && !KillEffect.mc.theWorld.loadedEntityList.contains(this.target)) {
            ++kills;
            if (tipsKillsValue.getValue()) {
                NotificationManager.post(NotificationType.SUCCESS, "Kills +1", "Killed " + this.kills + " Players.  ");
            }
            if (killSoundValue.getValue()) {
                this.playSound(SoundType.KILL, 0.75f);
            }
            if (this.squids.getValue()) {
                this.squid = new EntitySquid(KillEffect.mc.theWorld);
                KillEffect.mc.theWorld.addEntityToWorld(-8, this.squid);
                this.squid.setPosition(this.target.posX, this.target.posY, this.target.posZ);
            }
            if (this.lightning.getValue()) {
                final EntityLightningBolt entityLightningBolt = new EntityLightningBolt(InstanceAccess.mc.theWorld, target.posX, target.posY, target.posZ);
                InstanceAccess.mc.theWorld.addEntityToWorld((int) (-Math.random() * 100000), entityLightningBolt);

                SoundUtil.playSound("ambient.weather.thunder");
            }
            if (this.explosion.getValue()) {
                for (int i = 0; i <= 8; i++) {
                    InstanceAccess.mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.FLAME);
                }
                SoundUtil.playSound("item.fireCharge.use");
            }
            this.target = null;
        }
    };

    @EventLink()
    public final Listener<AttackEvent> onAttack = event -> {
        final Entity entity = event.getTarget();
        if (entity instanceof EntityLivingBase) {
            target = (EntityLivingBase) entity;
        }
    };

    public void playSound(SoundType st, float volume) {
        new Thread(() -> {
            try {
                AudioInputStream as = AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/minecraft/lavender/" + st.getName()))));
                Clip clip = AudioSystem.getClip();
                clip.open(as);
                clip.start();
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume);
                clip.start();
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public enum SoundType {
        KILL("kill.wav");

        final String music;

        SoundType(String fileName) {
            this.music = fileName;
        }

        public String getName() {
            return this.music;
        }
    }
}
