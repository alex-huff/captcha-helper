package com.alexfh.captcha_helper.mixin;

import com.alexfh.captcha_helper.captcha.CaptchaState;
import com.alexfh.captcha_helper.captcha.CaptchaUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.Optional;

@Mixin(GenericContainerScreen.class)
public
class GenericContainerScreenMixin
{
    private static final long minSolveDelay = 600;
    private static final long maxSolveDelay = 1200;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/screen/GenericContainerScreenHandler;Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/text/Text;)V")
    private
    void init(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci)
    {
        Optional<String> itemNameOptional = CaptchaUtil.extractItemName(title.getString());
        if (itemNameOptional.isEmpty())
        {
            return;
        }
        String itemName = itemNameOptional.get();
        CaptchaState.currentCaptchaItem     = itemName.toLowerCase(Locale.ROOT);
        CaptchaState.currentCaptchaWindowID = handler.syncId;
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        final String    oldCaptchaItem  = CaptchaState.currentCaptchaItem;
        final int       oldSyncID       = CaptchaState.currentCaptchaWindowID;
        Thread clickThread = new Thread(() ->
        {
            try
            {
                Thread.sleep((long) (GenericContainerScreenMixin.minSolveDelay + Math.random() *
                                                                                 (GenericContainerScreenMixin.maxSolveDelay -
                                                                                  GenericContainerScreenMixin.minSolveDelay)));
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
                if (minecraftClient.currentScreen instanceof GenericContainerScreen genericContainerScreen)
                {
                    GenericContainerScreenHandler genericContainerScreenHandler
                        = genericContainerScreen.getScreenHandler();
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
                            interactionManager.clickSlot(genericContainerScreenHandler.syncId, slot.id, 0, SlotActionType.PICKUP, inventory.player);
                            return;
                        }
                    }
                    genericContainerScreen.close();
                }
            });
        });
        clickThread.start();
    }
}