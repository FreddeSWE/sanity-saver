package com.sanitysaver;
import net.runelite.api.*;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.*;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class SanitySaverItemOverlay extends WidgetItemOverlay
{
    private final Map<Integer, BufferedImage> spriteCache = new HashMap<>();

    private final Client client;
    private final SanitySaverConfig config;

    @Inject
    public SanitySaverItemOverlay(Client client, SanitySaverConfig config)
    {
        this.client = client;
        this.config = config;

        showOnInventory();
        showOnBank();
        showOnEquipment();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
    {
        int quantity = itemWidget.getQuantity(); 
        if (!shouldCensor(itemId, quantity)) 
        { 
            return; 
        }

        Rectangle bounds = itemWidget.getCanvasBounds(); 
        int x = bounds.x; 
        int y = bounds.y; 
        int width = bounds.width; 
        int height = 10;

        BufferedImage cached = spriteCache.get(itemId);
        if (cached != null)
        {
            graphics.drawImage(cached, x, y, bounds.width, bounds.height, null);
        }

        graphics.setColor(config.backgroundColor()); 
        graphics.fillRect(x, y, width, height); 
        
        if (config.drawBorder()) 
        { 
            graphics.setColor(config.borderColor());
            graphics.drawRect(x, y, width - 1, height - 1);
        } 
        
        if (config.drawText()) 
        { 
            String text = config.censorText(); 
            FontMetrics fm = graphics.getFontMetrics(); 
            int textX = x + 2; 
            int textY = y + fm.getAscent() - 1; 
            graphics.setColor(Color.BLACK); 
            graphics.drawString(text, textX + 1, textY + 1); 
            graphics.setColor(config.textColor()); 
            graphics.drawString(text, textX, textY); 
        }
    }

    private boolean shouldCensor(int itemId, int quantity)
    {
        if (quantity < config.minAmount())
            return false;

        String list = config.censoredItems().trim();
        if (list.isEmpty())
            return false;

        ItemComposition comp = client.getItemDefinition(itemId);
        String name = comp.getName().toLowerCase();

        for (String token : list.toLowerCase().split(","))
        {
            token = token.trim();
            if (token.isEmpty())
                continue;

            if (token.equals(String.valueOf(itemId)) || name.contains(token))
                return true;
        }

        return false;
    }

    public void cacheSprite(int itemId, BufferedImage image)
    {
        spriteCache.put(itemId, image);
    }

    public void removeSprite(int itemId)
    {
        spriteCache.remove(itemId);
    }

    public boolean isCached(int itemId)
    {
        return spriteCache.containsKey(itemId);
    }

    public void clearCache()
    {
        spriteCache.clear();
    }
}
