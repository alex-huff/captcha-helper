package com.alexfh.captcha_helper.mixin;

import com.alexfh.captcha_helper.CaptchaHelper;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public
class ExampleMixin
{
    @Inject(at = @At("HEAD"), method = "run")
    private
    void run(CallbackInfo info)
    {
        CaptchaHelper.LOGGER.info("Hello from mixin");
    }
}