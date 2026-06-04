package net.korin.sleepanimation.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.compress.harmony.pack200.NewAttributeBands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class SleepMixin {

    @Unique private long animationStartTime = 0;
    @Unique private boolean isAnimating = false;
    @Unique private boolean wasSleeping = false;

    private static final int CLOSE_DURATION = 2500;
    private static final int HOLD_DURATION = 500;
    private static final int OPEN_DURATION = 500;

    @Inject(method = "extractSleepOverlay", at = @At("HEAD"), cancellable = true)
    private void onExtractSleepOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player == null) return;

        boolean isSleeping = player.isSleeping();

        if (isSleeping && !wasSleeping) {
            animationStartTime = System.currentTimeMillis();
            isAnimating = true;
        } else if (!isSleeping && wasSleeping) {
            animationStartTime = System.currentTimeMillis();
            isAnimating = true;
        }
        wasSleeping = isSleeping;

        if (!isAnimating) return;

        long elapsed = System.currentTimeMillis() - animationStartTime;
        float curtainAmount;
        boolean animationComplete = false;

        if (isSleeping) {
            if (elapsed < CLOSE_DURATION) {
                float progress = elapsed / (float)CLOSE_DURATION;
                curtainAmount = Math.min(1f, progress);
            } else if (elapsed < CLOSE_DURATION + HOLD_DURATION) {
                curtainAmount = 1f;
            } else {
                curtainAmount = 1f;
            }
        } else {
            if (elapsed < OPEN_DURATION) {
                float progress = elapsed / (float)OPEN_DURATION;
                curtainAmount = 1f - Math.min(1f, progress);
            } else {
                curtainAmount = 0f;
                animationComplete = true;
            }
        }

        float eased;
        if (isSleeping) {
            eased = 1 - (float)Math.pow(1 - curtainAmount, 3);
        } else {
            eased = (float)Math.pow(curtainAmount, 3);
        }


        if (curtainAmount <= 0.01f && animationComplete) {
            isAnimating = false;
            return;
        }

        if (curtainAmount <= 0.01f) {
            return;
        }

        ci.cancel();
        graphics.nextStratum();

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        int curtainHeight = (int)(screenHeight * eased / 2);

        int alpha = (int)(225 * Math.min(1f, curtainAmount * 1.2f));
        int color = ARGB.color(alpha, 0, 0, 0);

        graphics.fill(0, 0, screenWidth, curtainHeight, color);
        graphics.fill(0, screenHeight - curtainHeight - 1, screenWidth, screenHeight, color);
    }

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void onExtractCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player != null && player.isSleeping()) {
            ci.cancel();
        }
    }
}