package com.spy.parkourfest.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for creating styled ItemStacks used in the hotbar editor UI.
 * Each item gets a persistent data key so we can identify it in click handlers.
 */
public class ItemBuilder {

    /** Namespace key for identifying ParkourFest editor items */
    public static final NamespacedKey PF_TOOL_KEY = new NamespacedKey("parkourfest", "tool_id");

    private final ItemStack item;
    private final ItemMeta meta;
    private final List<Component> lore = new ArrayList<>();

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    /**
     * Set the display name with gold color and bold.
     */
    public ItemBuilder name(String name) {
        meta.displayName(Component.text(name)
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        return this;
    }

    /**
     * Set the display name with a custom color.
     */
    public ItemBuilder name(String name, NamedTextColor color) {
        meta.displayName(Component.text(name)
                .color(color)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        return this;
    }

    /**
     * Add a line of lore (gray text).
     */
    public ItemBuilder lore(String line) {
        lore.add(Component.text(line)
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        return this;
    }

    /**
     * Add a line of lore with a custom color.
     */
    public ItemBuilder lore(String line, NamedTextColor color) {
        lore.add(Component.text(line)
                .color(color)
                .decoration(TextDecoration.ITALIC, false));
        return this;
    }

    /**
     * Add an empty lore line (spacer).
     */
    public ItemBuilder loreSpacer() {
        lore.add(Component.empty());
        return this;
    }

    /**
     * Set a persistent data key to identify this tool item.
     * Used in EditorListener to detect which tool was clicked.
     */
    public ItemBuilder toolId(String id) {
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(PF_TOOL_KEY, PersistentDataType.STRING, id);
        return this;
    }

    /**
     * Make the item glow (enchantment glint) without showing enchantment text.
     */
    public ItemBuilder glow() {
        meta.setEnchantmentGlintOverride(true);
        return this;
    }

    /**
     * Build the final ItemStack.
     */
    public ItemStack build() {
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Static helper to check if an ItemStack is a ParkourFest tool.
     */
    public static boolean isPFTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(PF_TOOL_KEY, PersistentDataType.STRING);
    }

    /**
     * Static helper to get the tool ID from an ItemStack.
     */
    public static String getToolId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(PF_TOOL_KEY, PersistentDataType.STRING);
    }
}
