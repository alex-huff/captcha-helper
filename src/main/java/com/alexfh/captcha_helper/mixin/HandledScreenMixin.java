package com.alexfh.captcha_helper.mixin;

import com.alexfh.captcha_helper.captcha.CaptchaState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract
class HandledScreenMixin
{
    @Inject(
        method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", at = @At(value = "HEAD"), cancellable = true)
    private
    void drawSlot(DrawContext context, Slot slot, CallbackInfo ci)
    {
        if (((HandledScreen<?>) (Object) this).getScreenHandler() instanceof GenericContainerScreenHandler screenHandler)
        {
            boolean isCaptchaWindow = screenHandler.syncId == CaptchaState.currentCaptchaWindowID;
            if (!isCaptchaWindow)
            {
                return;
            }
            boolean slotInCaptchaWindow = slot.id < screenHandler.getRows() * 9;
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
}
