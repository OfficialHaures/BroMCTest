package nl.inferno.broMCTest.Listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import nl.inferno.broMCTest.BroMCTest;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class AutoCompressor implements Listener {

    private final BroMCTest plugin;
    private final Map<Material, Material> oreToItemMap;
    private final Map<Material, Material> itemToBlockMap;
    private final WorldGuardPlugin worldGuard;

    public AutoCompressor(BroMCTest plugin) {
        this.plugin = plugin;
        this.oreToItemMap = new HashMap<>();
        this.itemToBlockMap = new HashMap<>();
        this.worldGuard = WorldGuardPlugin.inst();
        initializeMaps();
    }

    private void initializeMaps() {
        oreToItemMap.put(Material.DIAMOND_ORE, Material.DIAMOND);
        itemToBlockMap.put(Material.DIAMOND, Material.DIAMOND_BLOCK);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInAutoDropRegion(player)) return;

        Material brokenBlock = event.getBlock().getType();
        if (oreToItemMap.containsKey(brokenBlock)) {
            event.setDropItems(false);
            Material item = oreToItemMap.get(brokenBlock);
            if (hasSpaceInHotbar(player, item)) {
                giveItems(player, item, 3);
                compressItems(player, item);
            } else {
                player.sendMessage(ChatColor.RED + "Warning: Your hotbar is full!");
            }
        }
    }

    private boolean isInAutoDropRegion(Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (regions == null) {
            return false;
        }
        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
        for (ProtectedRegion region : set) {
            if (region.getId().toLowerCase().startsWith("autodrop-")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpaceInHotbar(Player player, Material item) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || (stack.getType() == item && stack.getAmount() < stack.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    private void giveItems(Player player, Material item, int amount) {
        PlayerInventory inventory = player.getInventory();
        ItemStack itemStack = new ItemStack(item, amount);
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType() == item) {
                inventory.setItem(i, itemStack);
                return;
            }
        }
    }

    private void compressItems(Player player, Material item) {
        if (itemToBlockMap.containsKey(item)) {
            Material block = itemToBlockMap.get(item);
            int itemCount = countItemsInHotbar(player, item);
            int blocksToCreate = itemCount / 9;

            if (blocksToCreate > 0) {
                removeItemsFromHotbar(player, item, blocksToCreate * 9);
                giveItems(player, block, blocksToCreate);
            }
        }
    }

    private int countItemsInHotbar(Player player, Material item) {
        int count = 0;
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null && stack.getType() == item) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    private void removeItemsFromHotbar(Player player, Material item, int amount) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null && stack.getType() == item) {
                if (stack.getAmount() <= amount) {
                    amount -= stack.getAmount();
                    inventory.setItem(i, null);
                } else {
                    stack.setAmount(stack.getAmount() - amount);
                    return;
                }
            }
            if (amount == 0) return;
        }
    }
}
