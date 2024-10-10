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
    private static final int numBorders = 2;

    @Inject(
        method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getMatrices()Lnet/minecraft/client/util/math/MatrixStack;", ordinal = 2))
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
                return;
            }
            context.getMatrices().push();
            // text is drawn with 200z, use 201z to draw on top of text
            context.getMatrices().translate(0, 0, 200 + 1);
            for (int i = 0; i < HandledScreenMixin.numBorders; i++)
            {
                int offset = 1 + i * 2;
                int length = 18 + i * 4;
                context.drawBorder(slot.x - offset, slot.y - offset, length, length, 0xFFCC241D);
            }
            context.getMatrices().pop();
        }
    }
}
