package com.alexfh.captcha_helper;

import com.alexfh.captcha_helper.captcha.CaptchaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptchaHelper implements ClientModInitializer
{
    public static final Logger LOGGER = LoggerFactory.getLogger("captcha-helper");
    private KeyBinding toggleAutoCaptchaKeyBinding;

    @Override
    public void onInitializeClient()
    {
        this.toggleAutoCaptchaKeyBinding
            = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.captcha-helper.toggleAutoCaptcha", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.captcha-helper"));
        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            while (this.toggleAutoCaptchaKeyBinding.wasPressed())
            {
                CaptchaConfig.autoCaptcha = !CaptchaConfig.autoCaptcha;
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of(
                    "Auto Captcha: " + CaptchaConfig.autoCaptcha), false);
            }
        });
    }
}