package com.pixilyfokica.weaponsmp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public final class LifestealSword extends JavaPlugin implements Listener {

    private boolean isLifeStealerCrafted = false;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        
        registerLifeStealerRecipe();
        registerFlameBladeRecipe();
        
        getLogger().info("WeaponSMP plugin je uspešno učitan!");
    }

    // ==========================================
    // 1. LifeStealer (Najjači enčanti u igri)
    // ==========================================
    private void registerLifeStealerRecipe() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "LifeStealer");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Jedinstveni mač maksimalne moći."));
            meta.setCustomModelData(1002); 

            // Najjači enčanti za mač:
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
            meta.addEnchant(Enchantment.LOOTING, 3, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
            meta.addEnchant(Enchantment.SWEEPING_EDGE, 3, true);

            sword.setItemMeta(meta);
        }

        NamespacedKey key = new NamespacedKey(this, "custom_lifesteal_sword");
        ShapedRecipe recipe = new ShapedRecipe(key, sword);

        // Tvoj recept:
        // H I H  (H = Player Head, I = Netherite Ingot)
        // T S T  (T = Totem, S = Netherite Sword)
        // H I H  (H = Player Head, I = Netherite Ingot)
        recipe.shape("HIH", "TST", "HIH");
        recipe.setIngredient('H', Material.PLAYER_HEAD);
        recipe.setIngredient('I', Material.NETHERITE_INGOT);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('S', Material.NETHERITE_SWORD);

        Bukkit.addRecipe(recipe);
    }

    // ==========================================
    // 2. FlameBlade Recept
    // ==========================================
    private void registerFlameBladeRecipe() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "FlameBlade");
            meta.setLore(Collections.singletonList(ChatColor.YELLOW + "Zapaljivi mač stvoren iz pakla."));
            meta.setCustomModelData(2001); 
            sword.setItemMeta(meta);
        }

        NamespacedKey key = new NamespacedKey(this, "custom_flame_blade");
        ShapedRecipe recipe = new ShapedRecipe(key, sword);

        recipe.shape(" M ", " MSM ", " M ");
        recipe.setIngredient('M', Material.MAGMA_CREAM);
        recipe.setIngredient('S', Material.DIAMOND_SWORD);

        Bukkit.addRecipe(recipe);
    }

    // ==========================================
    // LIMIT NA 1 CRAFT
    // ==========================================
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();

        if (isWeapon(result, 1002)) {
            if (isLifeStealerCrafted) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    player.sendMessage(ChatColor.RED + "LifeStealer je već napravljen! Može postojati samo jedan na serveru.");
                }
            } else {
                isLifeStealerCrafted = true;
                Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[WeaponSMP] " + 
                                       ChatColor.RED + event.getWhoClicked().getName() + 
                                       ChatColor.YELLOW + " je upravo napravio jedinstveni LifeStealer mač!");
            }
        }
    }

    // ==========================================
    // KRAĐA SRCA I PORUKA O SMRTI
    // ==========================================
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;
        ItemStack itemInHand = killer.getInventory().getItemInMainHand();

        if (isWeapon(itemInHand, 1002)) {
            AttributeInstance victimMaxHealth = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (victimMaxHealth != null && victimMaxHealth.getBaseValue() > 2.0) {
                victimMaxHealth.setBaseValue(victimMaxHealth.getBaseValue() - 2.0);
                victim.sendMessage(ChatColor.RED + "Izgubio si 1 srce jer te ubio " + killer.getName() + "!");
            }

            AttributeInstance killerMaxHealth = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (killerMaxHealth != null && killerMaxHealth.getBaseValue() < 40.0) {
                killerMaxHealth.setBaseValue(killerMaxHealth.getBaseValue() + 2.0);
                killer.sendMessage(ChatColor.GREEN + "Ukrao si 1 srce od " + victim.getName() + "!");
            }

            event.setDeathMessage(ChatColor.YELLOW + victim.getName() + ChatColor.WHITE + " was slain by " + 
                                  ChatColor.RED + killer.getName() + ChatColor.WHITE + " using " + 
                                  ChatColor.RED + "crveno LifeStealer");
        }
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player killer = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            ItemStack itemInHand = killer.getInventory().getItemInMainHand();

            if (isWeapon(itemInHand, 2001)) {
                victim.setFireTicks(100); 
            }
        }
    }

    private boolean isWeapon(ItemStack item, int modelData) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == modelData;
    }
}
