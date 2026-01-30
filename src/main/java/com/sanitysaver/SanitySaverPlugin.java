package com.sanitysaver;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Client;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;

@Slf4j
@PluginDescriptor(
	name = "Sanity Saver"
)
public class SanitySaverPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private SanitySaverConfig config;

    @Inject
    private SanitySaverItemOverlay itemOverlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ConfigManager configManager;

	@Override
	protected void startUp() throws Exception
	{
        overlayManager.add(itemOverlay);
		log.debug("Sanity Saver started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
        overlayManager.remove(itemOverlay);
		log.debug("Sanity Saver stopped!");
	}

	@Provides
	SanitySaverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SanitySaverConfig.class);
	}

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!event.getOption().equalsIgnoreCase("Examine"))
        {
            return;
        }

        if (!client.isKeyPressed(KeyCode.KC_SHIFT))
        {
            return;
        }

        Widget widget = event.getMenuEntry().getWidget();
        if (widget == null || widget.getItemId() <= 0)
        {
            return;
        }

        int itemId = widget.getItemId();

        boolean censored = isItemCensored(itemId);

        String option = censored ? "Uncensor" : "Censor";

        client.createMenuEntry(-1)
                .setOption(option)
                .setTarget(event.getTarget())
                .setType(MenuAction.RUNELITE)
                .onClick(e -> toggleItemCensor(itemId));
    }

    private boolean isItemCensored(int itemId)
    {
        String list = config.censoredItems();
        if (list == null || list.isBlank())
        {
            return false;
        }

        for (String token : list.split(","))
        {
            if (token.trim().equals(String.valueOf(itemId)))
            {
                return true;
            }
        }

        return false;
    }

    private void toggleItemCensor(int itemId)
    {
        String id = String.valueOf(itemId);
        String current = config.censoredItems();

        StringBuilder result = new StringBuilder();

        boolean removed = false;

        if (current != null && !current.isBlank())
        {
            for (String token : current.split(","))
            {
                token = token.trim();
                if (token.isEmpty())
                {
                    continue;
                }

                if (token.equals(id))
                {
                    removed = true;
                    continue;
                }

                if (result.length() > 0)
                {
                    result.append(", ");
                }
                result.append(token);
            }
        }

        if (!removed)
        {
            if (result.length() > 0)
            {
                result.append(", ");
            }
            result.append(id);
        }

        configManager.setConfiguration(
                "sanitysaver",
                "censoredItems",
                result.toString()
        );
    }
}
