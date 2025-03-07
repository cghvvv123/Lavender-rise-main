package com.alan.clients.module;

import com.alan.clients.Client;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.render.ClickGUI;
import com.alan.clients.module.impl.render.Interface;
import com.alan.clients.module.impl.render.interfaces.ModuleComponent;
import com.alan.clients.newevent.impl.other.ModuleToggleEvent;
import com.alan.clients.noti.NotificationManager;
import com.alan.clients.noti.NotificationType;
import com.alan.clients.util.animations.Animation;
import com.alan.clients.util.animations.Easing;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.value.Value;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.ModeValue;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Keyboard;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Patrick
 * @since 10/19/2021
 */
@Getter
@Setter
public abstract class Module implements InstanceAccess {
    private final String displayName;
    private final List<Value<?>> values = new ArrayList<>();
    private ModuleInfo moduleInfo;
    private boolean hidden;
    private boolean enabled;
    private int keyCode;

    private Animation moveAnim = new Animation(Easing.LINEAR, 100);

    public Module() {
        if (this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            this.moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);
            this.displayName = this.moduleInfo.name();
            this.keyCode = this.moduleInfo.keyBind();
            this.hidden = moduleInfo.hidden();
        } else {
            throw new RuntimeException("ModuleInfo annotation not found on " + this.getClass().getSimpleName());
        }
    }

    public Module(final ModuleInfo info) {
        this.moduleInfo = info;
        this.displayName = this.moduleInfo.name();
        this.keyCode = this.moduleInfo.keyBind();
    }

    public boolean isNull() {
        return mc.thePlayer == null && mc.theWorld == null;
    }

    public void toggle() {
        this.setEnabled(!enabled);
    }


    public void setEnabled(final boolean enabled) {
        if (this.enabled == enabled || (!this.moduleInfo.allowDisable() && !enabled)) {
            return;
        }
        this.enabled = enabled;

        Client.INSTANCE.getEventBus().handle(new ModuleToggleEvent(this));
        final Interface interfaceModule = Client.INSTANCE.getModuleManager().get(Interface.class);
        ModuleComponent moduleComponent = new ModuleComponent(this);
        moduleComponent.setTranslatedName(Localization.get(moduleComponent.getModule().getDisplayName()));
        if (enabled) {
            superEnable();
            if (interfaceModule.isEnabled() && interfaceModule.togglesounds.getValue()) {
                if (!isNull()) {
                    this.playSound(SoundType.ENABLE, 0.7f);
                }
            }
            if (interfaceModule.isEnabled() && interfaceModule.togglenoti.getValue()) {
                if (!isNull()) {
                    NotificationManager.post(NotificationType.SUCCESS, "Module toggled", (moduleComponent.getTranslatedName() + " Enabled"));
                }
            }
        } else {
            superDisable();
            if (interfaceModule.isEnabled() && interfaceModule.togglesounds.getValue()) {
                if (mc.thePlayer != null) {
                    this.playSound(SoundType.DISABLE, 0.7f);
                }
            }
            if (interfaceModule.isEnabled() && interfaceModule.togglenoti.getValue()) {
                if (!isNull()) {
                    NotificationManager.post(NotificationType.DISABLE, "Module toggled", (moduleComponent.getTranslatedName() + " Disabled"));
                }
            }
        }
    }

    /**
     * Called when a module gets enabled
     * -> important: whenever you override this method in a subclass
     * keep the super.onEnable()
     */
    public final void superEnable() {
        Client.INSTANCE.getEventBus().register(this);

        this.values.stream()
                .filter(value -> value instanceof ModeValue)
                .forEach(value -> ((ModeValue) value).getValue().register());

        this.values.stream()
                .filter(value -> value instanceof BooleanValue)
                .forEach(value -> {
                    final BooleanValue booleanValue = (BooleanValue) value;
                    if (booleanValue.getMode() != null && booleanValue.getValue()) {
                        booleanValue.getMode().register();
                    }
                });

        if (mc.thePlayer != null) this.onEnable();
    }

    /**
     * Called when a module gets disabled
     * -> important: whenever you override this method in a subclass
     * keep the super.onDisable()
     */
    public final void superDisable() {
        Client.INSTANCE.getEventBus().unregister(this);

        this.values.stream()
                .filter(value -> value instanceof ModeValue)
                .forEach(value -> ((ModeValue) value).getValue().unregister());

        this.values.stream()
                .filter(value -> value instanceof BooleanValue)
                .forEach(value -> {
                    final BooleanValue booleanValue = (BooleanValue) value;
                    if (booleanValue.getMode() != null) {
                        booleanValue.getMode().unregister();
                    }
                });

        if (mc.thePlayer != null) this.onDisable();
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public List<Value<?>> getAllValues() {
        ArrayList<Value<?>> allValues = new ArrayList<>();

        values.forEach(value -> {
            List<Value<?>> subValues = value.getSubValues();

            allValues.add(value);

            if (subValues != null) {
                allValues.addAll(subValues);
            }
        });

        return allValues;
    }

    public boolean shouldDisplay(Interface instance) {
        if (this instanceof ClickGUI) return false;
        if (!this.getModuleInfo().allowDisable()) return false;

        switch (instance.getModulesToShow().getValue().getName()) {
            case "All": {
                return true;
            }
            case "Exclude render": {
                return !this.getModuleInfo().category().equals(Category.RENDER);
            }
            case "Only bound": {
                return this.getKeyCode() != Keyboard.KEY_NONE;
            }
        }
        return true;
    }

    public void playSound(SoundType st, float volume) {
        new Thread(() -> {
            try {
                AudioInputStream as = AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/minecraft/lavender/sound/" + st.getName()))));
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
        DISABLE("disable.wav"),
        ENABLE("enable.wav");

        final String music;

        SoundType(String fileName) {
            this.music = fileName;
        }

        public String getName() {
            return this.music;
        }
    }
}
