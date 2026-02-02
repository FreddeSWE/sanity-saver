package com.sanitysaver;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Client;
import net.runelite.api.Menu;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.PostItemComposition;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.AsyncBufferedImage;


import javax.inject.Inject;

import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Sanity Saver"
)
public class SanitySaverPlugin extends Plugin
{
	@Inject
	private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ClientThread clientThread;

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
        itemOverlay.clearCache();
        resetCaches();
        overlayManager.add(itemOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
        itemOverlay.clearCache();
        resetCaches();
        overlayManager.remove(itemOverlay);
	}

	@Provides
	SanitySaverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SanitySaverConfig.class);
	}

    //Ref unidentified-herbs by hex-agon and MoreFillPlugin by UnExploration
    @Subscribe
    public void onPostItemComposition(PostItemComposition event)
    {
        ItemComposition itemComposition = event.getItemComposition();
        int itemId = itemComposition.getId();

        if (!isItemCensored(itemId))
        {
            return;
        }

        if (itemOverlay.isCached(itemId))
        {
            itemComposition.setInventoryModel(-1);
            return;
        }

        AsyncBufferedImage image = itemManager.getImage(itemId);

        image.onLoaded(() ->
        {
            BufferedImage buffered = image.getSubimage(0, 0, image.getWidth(), image.getHeight());
            itemOverlay.cacheSprite(itemId, buffered);

            itemComposition.setInventoryModel(-1);
            resetCaches();
        });
    }

    //Ref unidentified-herbs by hex-agon and MoreFillPlugin by UnExploration
    private void resetCaches() {
        clientThread.invokeLater(() -> {
            client.getItemCompositionCache().reset();
            client.getItemModelCache().reset();
            client.getItemSpriteCache().reset();
        });
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!client.isKeyPressed(KeyCode.KC_SHIFT))
        {
            return;
        }

        if (!event.getOption().equalsIgnoreCase("Examine"))
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

        Menu menu = client.getMenu();
        MenuEntry entry = menu.createMenuEntry(-1); // -1 to append at the end
        entry.setOption(option)
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

        for (String censoredID : list.split(","))
        {
            if (censoredID.trim().equals(String.valueOf(itemId)))
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

        StringBuilder censoredIDList = new StringBuilder();

        boolean removed = false;

        if (current != null && !current.isBlank())
        {
            for (String censoredID : current.split(","))
            {
                censoredID = censoredID.trim();
                if (censoredID.isEmpty())
                {
                    continue;
                }

                if (censoredID.equals(id))
                {
                    removed = true;
                    itemOverlay.removeSprite(itemId);
                    continue;
                }

                if (censoredIDList.length() > 0)
                {
                    censoredIDList.append(", ");
                }
                censoredIDList.append(censoredID);
            }
        }

        if (!removed)
        {
            if (censoredIDList.length() > 0)
            {
                censoredIDList.append(", ");
            }
            censoredIDList.append(id);
        }

        configManager.setConfiguration(
                "sanitysaver",
                "censoredItems",
                censoredIDList.toString()
        );

        resetCaches();
    }
}
