package com.alexfh.captcha_helper;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public
class CaptchaHelper implements ClientModInitializer
{
    public static final Logger LOGGER = LoggerFactory.getLogger("captcha-helper");

    @Override
    public
    void onInitializeClient()
    {
        CaptchaHelper.LOGGER.info("Hello Fabric world!");
    }
}