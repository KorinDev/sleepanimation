package net.korin.sleepanimation.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class SleepMixin {

    @Shadow
    private int displayHealth;
    @Unique private float lastCurtainAmount = 0f;

    @Inject(method = "extractSleepOverlay", at = @At("HEAD"), cancellable = true)
    private void onExtractSleepOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player == null) return;

        int sleepTimer = player.getSleepTimer();


        if (sleepTimer == 0) {
            lastCurtainAmount = 0f;
            return;
        }

        ci.cancel();

        graphics.nextStratum();

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        float curtainAmount;

        if (sleepTimer <= 50) {
            curtainAmount = sleepTimer / 50f;
        } else if (sleepTimer <= 100) {
            curtainAmount = 1f;
        } else {
            curtainAmount = 1f - ((sleepTimer - 100f) / 10f);
            curtainAmount = Math.max(0, curtainAmount);
        }

        float eased = 1 - (float)Math.pow(1 - curtainAmount, 3);

        int curtainHeight = (int)(screenHeight * eased / 2);

        graphics.fill(0, 0, screenWidth, curtainHeight, ARGB.color(255, 0, 0, 0));

        graphics.fill(0, screenHeight - curtainHeight, screenWidth, screenHeight, ARGB.color(255, 0, 0, 0));

        lastCurtainAmount = curtainAmount;
    }

}
