package com.alexfh.captcha_helper.captcha;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public
class CaptchaUtil
{
    /*
    CAPTCHA: SPRUCE_SAPLING
    | CAPTCHA: | Click RED_BED
    CHALLENGE: SPRUCE_BUTTON
     */
    public static final Pattern captchaRegex
        = Pattern.compile("^(?:\\| )?(?:CAPTCHA|CHALLENGE): (?:\\| Click )?([A-Z_]+)$");

    public static
    Optional<String> extractItemName(String captchaString)
    {
        Matcher captchaRegexMatcher = CaptchaUtil.captchaRegex.matcher(captchaString);
        return captchaRegexMatcher.matches() ? Optional.of(captchaRegexMatcher.group(1)) : Optional.empty();
    }
}
