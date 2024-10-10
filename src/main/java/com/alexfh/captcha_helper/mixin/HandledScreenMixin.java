package com.alexfh.captcha_helper.mixin;

import com.alexfh.captcha_helper.captcha.CaptchaState;
import com.alexfh.captcha_helper.captcha.CaptchaUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(HandledScreen.class)
public abstract
class HandledScreenMixin
{
    @Inject(
        method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", at = @At(value = "HEAD"), cancellable = true)
    private
    void drawSlot(DrawContext context, Slot slot, CallbackInfo ci)
    {
        if (((HandledScreen<?>) (Object) this).getScreenHandler() instanceof GenericContainerScreenHandler genericContainerScreenHandler)
        {
            boolean isCaptchaWindow = (CaptchaState.currentCaptchaWindowID != null) &&
                                      genericContainerScreenHandler.syncId == CaptchaState.currentCaptchaWindowID;
            if (!isCaptchaWindow)
            {
                return;
            }
            boolean slotInCaptchaWindow = slot.id < genericContainerScreenHandler.getRows() * 9;
            if (!slotInCaptchaWindow)
            {
                return;
            }
            String  itemType      = Registries.ITEM.getId(slot.getStack().getItem()).getPath();
            boolean isCaptchaItem = itemType.equals(CaptchaState.currentCaptchaItem);
            if (!isCaptchaItem)
            {
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/screen/ScreenHandler;Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/text/Text;)V")
    private
    void init(ScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci)
    {
        if (handler instanceof GenericContainerScreenHandler genericContainerScreenHandler)
        {
            CaptchaUtil.extractItemName(title.getString()).ifPresentOrElse((itemName) ->
            {
                CaptchaState.currentCaptchaItem     = itemName.toLowerCase(Locale.ROOT);
                CaptchaState.currentCaptchaWindowID = genericContainerScreenHandler.syncId;
            }, () ->
            {
                CaptchaState.currentCaptchaItem     = null;
                CaptchaState.currentCaptchaWindowID = null;
            });
        }
    }
}