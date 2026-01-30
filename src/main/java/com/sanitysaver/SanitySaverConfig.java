package com.sanitysaver;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("sanitysaver")
public interface SanitySaverConfig extends Config
{
    @ConfigItem(
            keyName = "censoredItems",
            name = "Censored items",
            description = "Item IDs or names to censor (comma-separated)",
            position = 0
    )
    default String censoredItems()
    {
        return "";
    }

    @ConfigItem(
            keyName = "minAmount",
            name = "Minimum amount",
            description = "Only censor stacks equal to or above this amount",
            position = 1

    )
    default int minAmount()
    {
        return 1;
    }

    @ConfigSection(
            name = "Overlay",
            description = "Overlay display settings",
            position = 02
    )
    String overlaySection = "overlay";

    @ConfigItem(
            keyName = "backgroundColor",
            name = "Overlay background color",
            description = "Background color of the censor overlay",
            position = 3,
            section = overlaySection
    )
    default Color backgroundColor()
    {
        return new Color(122, 131, 123, 255);
    }

    @ConfigItem(
            keyName = "borderColor",
            name = "Border color",
            description = "Color of the censor overlay border",
            position = 4,
            section = overlaySection
    )
    default Color borderColor()
    {
        return new Color(102, 26, 26, 255);
    }

    @ConfigItem(
            keyName = "drawBorder",
            name = "Draw border",
            description = "Draw a border around the censor overlay",
            position = 5,
            section = overlaySection
    )
    default boolean drawBorder()
    {
        return true;
    }

    @ConfigItem(
            keyName = "drawText",
            name = "Draw text",
            description = "Draw text on top of the censor overlay",
            position = 6,
            section = overlaySection
    )
    default boolean drawText()
    {
        return true;
    }

    @ConfigItem(
            keyName = "censorText",
            name = "Censor text",
            description = "The text that will be drawn on top of your censor overlay",
            position = 7,
            section = overlaySection
    )
    default String censorText()
    {
        return "???";
    }

    @ConfigItem(
            keyName = "textColor",
            name = "Text color",
            description = "Color of the ??? text",
            position = 8,
            section = overlaySection
    )
    default Color textColor()
    {
        return new Color(59, 255, 68, 255);
    }
}
