package com.spy.spymotion.editor.pages;

import com.spy.spymotion.editor.EditorSession;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

/**
 * Abstract base class for hotbar editor pages.
 * Each page defines 9 item slots and their click behaviors.
 */
public abstract class HotbarPage {

    protected final EditorSession session;

    public HotbarPage(EditorSession session) {
        this.session = session;
    }

    /**
     * Set up the player's hotbar with this page's items.
     * Called when switching to this page.
     */
    public abstract void setup(Player player);

    /**
     * Handle a click event on a hotbar slot.
     * @param player The player who clicked
     * @param slot The hotbar slot (0-8)
     * @param action The click action (LEFT/RIGHT click on AIR or BLOCK)
     * @param clickedBlockX X coordinate of clicked block (if action involves a block), -1 otherwise
     * @param clickedBlockY Y coordinate of clicked block
     * @param clickedBlockZ Z coordinate of clicked block
     * @return true if the click was handled by this page
     */
    public abstract boolean handleClick(Player player, int slot, Action action,
                                         int clickedBlockX, int clickedBlockY, int clickedBlockZ);

    /**
     * Get a user-friendly name for this page (shown in action bar on switch).
     */
    public abstract String getPageName();
}
