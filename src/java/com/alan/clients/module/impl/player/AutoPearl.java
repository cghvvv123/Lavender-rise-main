package com.alan.clients.module.impl.player;

import com.alan.clients.Client;
import com.alan.clients.component.impl.player.FallDistanceComponent;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.other.Stuck;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PostMotionEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.util.MSTimer;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.util.player.ProjectileUtil;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;

import javax.vecmath.Vector2f;

@ModuleInfo(name = "module.player.autopearl.name", category = Category.PLAYER,description = "CNM")
public class AutoPearl extends Module {

    private final NumberValue falldistance = new NumberValue("Fall Distance",this,2.0f, 2.0f,10.0f,1.0f);
    private CalculateThread calculateThread;
    Stuck stuck = Client.INSTANCE.getModuleManager().get(Stuck.class);
    private boolean attempted;
    private boolean calculating;
    private int bestPearlSlot;
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (isNull()) return;
        if (AutoPearl.mc.thePlayer.onGround) {
            this.attempted = false;
            this.calculating = false;
        }
        final boolean overVoid = !AutoPearl.mc.thePlayer.onGround && !PlayerUtil.isBlockUnder(30.0, true);

        if (!this.attempted && overVoid && FallDistanceComponent.distance > falldistance.getValue().doubleValue()) {
            FallDistanceComponent.distance = 0.0f;
            this.attempted = true;

            // 只遍历可能包含末影珍珠的槽位
            for (int slot = 36; slot < 45; ++slot) {
                final ItemStack stack = AutoPearl.mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
                if (stack != null && stack.getItem() instanceof ItemEnderPearl) {
                    // 找到珍珠
                    this.bestPearlSlot = slot;
                    ChatUtil.display("找到了珍珠:" + (this.bestPearlSlot - 36));
                    if (this.bestPearlSlot != 36) {
                        AutoPearl.mc.thePlayer.inventory.currentItem = this.bestPearlSlot - 36;
                    }
                    // 找到珍珠后跳出循环
                    break;
                }
            }
            if (this.bestPearlSlot == 0) {
                return;
            }
            if (!(AutoPearl.mc.thePlayer.inventoryContainer.getSlot(this.bestPearlSlot).getStack().getItem() instanceof ItemEnderPearl)) {
                return;
            }
            this.calculating = true;
            (this.calculateThread = new CalculateThread(AutoPearl.mc.thePlayer.posX, AutoPearl.mc.thePlayer.posY, AutoPearl.mc.thePlayer.posZ, 0.0, 0.0)).start();
            (this.getModule((Class)Stuck.class)).setEnabled(true);
        }
    };
    @EventLink()
    private final Listener<PostMotionEvent> onPostMotion = event -> {
        if (isNull()) return;
        if ( this.calculating && (this.calculateThread == null || this.calculateThread.completed)) {
            this.calculating = false;
            stuck.throwPearl(this.calculateThread.solution);
        }
    };
    private static class CalculateThread extends Thread
    {
        private int iteration;
        private boolean completed;
        private double temperature;
        private double energy;
        private double solutionE;
        private Vector2f solution;
        public boolean stop;
        private final ProjectileUtil.EnderPearlPredictor predictor;

        private CalculateThread(final double predictX, final double predictY, final double predictZ, final double minMotionY, final double maxMotionY) {
            this.predictor = new ProjectileUtil.EnderPearlPredictor(predictX, predictY, predictZ, minMotionY, maxMotionY);
            this.iteration = 0;
            this.completed = false;
            this.solution = null;
            this.temperature = 10.0;
            this.energy = 0.0;
            this.stop = false;
            this.completed = false;
        }
        @Override
        public void run() {
            final MSTimer timer = new MSTimer();
            timer.reset();
            this.solution = new Vector2f((float) MathUtil.getRandomInRange(-180, 180), (float)MathUtil.getRandomInRange(-90, 90));
            Vector2f current = this.solution;
            this.energy = this.predictor.assessRotation(this.solution);
            this.solutionE = this.energy;
            double deltaE;
            double assessment;
            while (this.temperature >= 1.0E-4 && !this.stop) {
                final double randomX = MathUtil.getRandomInRange(-this.temperature * 18.0, this.temperature * 18.0);
                final double randomY = MathUtil.getRandomInRange(-this.temperature * 9.0, this.temperature * 9.0);
                final Vector2f rotation = new Vector2f((float)(current.x + randomX), (float)(current.y + randomY));
                rotation.y = Math.max(-90.0f, Math.min(90.0f, rotation.y));
                assessment = this.predictor.assessRotation(rotation);
                deltaE = assessment - this.energy;
                if (deltaE >= 0.0 || Math.random() < Math.exp(-deltaE / (this.temperature * 100.0))) {
                    this.energy = assessment;
                    current = rotation;
                    if (assessment > this.solutionE) {
                        this.solutionE = assessment;
                        this.solution.set(rotation.x, rotation.y);
                        ChatUtil.display("找到一个更好的解决方案: (" + this.solution.x + ", " + this.solution.y + "), value: " + this.solutionE);
                    }
                }
                this.temperature *= 0.997;
                ++this.iteration;
            }
            ChatUtil.display("使用的时间: " + timer.getDifference() + " solution energy: " + this.solutionE);
            this.completed = true;
        }
    }
}
