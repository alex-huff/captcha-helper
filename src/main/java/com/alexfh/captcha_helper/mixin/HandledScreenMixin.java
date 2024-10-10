package com.alexfh.captcha_helper.mixin;

import com.alexfh.captcha_helper.captcha.CaptchaConfig;
import com.alexfh.captcha_helper.captcha.CaptchaState;
import com.alexfh.captcha_helper.captcha.CaptchaUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.Optional;

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
            Optional<String> itemNameOptional = CaptchaUtil.extractItemName(title.getString());
            if (itemNameOptional.isEmpty())
            {
                CaptchaState.currentCaptchaWindowID = null;
                CaptchaState.currentCaptchaItem     = null;
                return;
            }
            String itemName = itemNameOptional.get();
            CaptchaState.currentCaptchaItem     = itemName.toLowerCase(Locale.ROOT);
            CaptchaState.currentCaptchaWindowID = genericContainerScreenHandler.syncId;
            this.scheduleClick();
        }
    }

    private
    void scheduleClick()
    {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        final String    oldCaptchaItem  = CaptchaState.currentCaptchaItem;
        final int       oldSyncID       = CaptchaState.currentCaptchaWindowID;
        Thread clickThread = new Thread(() ->
        {
            try
            {
                Thread.sleep((long) (CaptchaConfig.minSolveDelay +
                                     Math.random() * (CaptchaConfig.maxSolveDelay - CaptchaConfig.minSolveDelay)));
            }
            catch (InterruptedException ignored)
            {
                return;
            }
            minecraftClient.execute(() ->
            {
                ClientPlayerInteractionManager interactionManager = minecraftClient.interactionManager;
                if (interactionManager == null)
                {
                    return;
                }
                if (minecraftClient.currentScreen instanceof HandledScreen<?> handledScreen)
                {
                    if (handledScreen.getScreenHandler() instanceof GenericContainerScreenHandler genericContainerScreenHandler)
                    {
                        if (genericContainerScreenHandler.syncId != oldSyncID)
                        {
                            return;
                        }
                        for (int i = 0; i < genericContainerScreenHandler.getRows() * 9; i++)
                        {
                            Slot    slot          = genericContainerScreenHandler.getSlot(i);
                            String  itemType      = Registries.ITEM.getId(slot.getStack().getItem()).getPath();
                            boolean isCaptchaItem = itemType.equals(oldCaptchaItem);
                            if (isCaptchaItem)
                            {
                                interactionManager.clickSlot(genericContainerScreenHandler.syncId, slot.id, 0, SlotActionType.PICKUP, minecraftClient.player);
                                return;
                            }
                        }
                        handledScreen.close();
                    }
                }
            });
        });
        clickThread.start();
    }
}
