package com.alexfh.captcha_helper.mixin;

import com.alexfh.captcha_helper.captcha.CaptchaState;
import com.alexfh.captcha_helper.captcha.CaptchaUtil;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(GenericContainerScreen.class)
public
class GenericContainerScreenMixin
{
    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/screen/GenericContainerScreenHandler;Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/text/Text;)V")
    private
    void init(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci)
    {
        CaptchaUtil.extractItemName(title.getString()).ifPresent(itemName ->
        {
            CaptchaState.currentCaptchaItem     = itemName.toLowerCase(Locale.ROOT);
            CaptchaState.currentCaptchaWindowID = handler.syncId;
        });
    }
}