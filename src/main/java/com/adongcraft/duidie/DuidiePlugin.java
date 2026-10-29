package com.adongcraft.duidie;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;

/** 把可堆叠物品的 max_stack_size 组件提到配置上限（原版组件硬上限 99），药水因此从 1/16 提到 99。 */
public final class DuidiePlugin extends JavaPlugin implements Listener {
    private BukkitTask sweep;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        maxStack = StackCaps.effectiveMaxStack(getConfig().getInt("max-stack", 99));
        getServer().getPluginManager().registerEvents(this, this);
        long interval = Math.max(20, getConfig().getLong("sweep-interval-ticks", 60));
        sweep = getServer().getScheduler().runTaskTimer(this, this::sweepAll, interval, interval);
        getLogger().info("堆叠上限已提升到 " + maxStack + "（耐久物品、潜影盒、WitchHoe 专属物品除外）");
    }

    @Override
    public void onDisable() {
        if (sweep != null) sweep.cancel();
    }

    private int maxStack;

    /** 原地提升一个物品的堆叠上限；返回是否实际修改（修改后需写回容器）。 */
    private boolean bump(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        Material type = item.getType();
        if (type.getMaxDurability() > 0) return false;            // 工具、盔甲、骨剑等耐久物品整堆共享耐久，不提升
        if (type.name().endsWith("SHULKER_BOX")) return false;    // 带内容的容器
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Set<NamespacedKey> keys = meta.getPersistentDataContainer().getKeys();
        for (NamespacedKey key : keys) {
            if (key.getNamespace().equals("witchhoe")) return false; // 刻印载体、法杖、强化石等逐件数据物品
        }
        int current = meta.getMaxStackSize();
        if (current >= maxStack) return false;
        meta.setMaxStackSize(maxStack);
        item.setItemMeta(meta);
        return true;
    }

    private void bumpInventory(Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            if (bump(contents[slot])) inventory.setItem(slot, contents[slot]);
        }
    }

    private void sweepAll() {
        for (Player player : getServer().getOnlinePlayers()) {
            bumpInventory(player.getInventory());
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack stack = event.getEntity().getItemStack();
        if (bump(stack)) event.getEntity().setItemStack(stack);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        ItemStack stack = event.getItem().getItemStack();
        if (bump(stack)) event.getItem().setItemStack(stack);
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (bump(result)) event.getInventory().setResult(result);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        bumpInventory(event.getInventory());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        bumpInventory(event.getPlayer().getInventory());
    }
}
