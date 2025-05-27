package de.construkter.bossbarLevels.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

public class TextCommands implements Listener {

    private final Map<Player, BossBar> playerBars = new HashMap<>();
    private final Map<Player, Integer> playerLevels = new HashMap<>();
    private final Map<Player, Integer> playerXp = new HashMap<>();

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().hasPermission("*")) return;
        if (!event.getMessage().startsWith("bb://api?cmd=")) return;
        event.setCancelled(true);

        String commandString = event.getMessage().substring("bb://api?cmd=".length());
        String[] parts = commandString.split("&");

        Map<String, String> params = new HashMap<>();
        for (String part : parts) {
            String[] keyVal = part.split("=");
            if (keyVal.length == 2) {
                params.put(keyVal[0], keyVal[1]);
            }
        }

        String command = parts[0].split("=")[0];
        Player sender = event.getPlayer();

        switch (command) {
            case "create_level" -> {
                Player target = Bukkit.getPlayer(params.get("player"));
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Spieler nicht gefunden.");
                    return;
                }

                int level = 1;
                int xp = 0;
                playerLevels.put(target, level);
                playerXp.put(target, xp);

                BossBar bar = Bukkit.createBossBar(getBarTitle(level, xp), BarColor.GREEN, BarStyle.SEGMENTED_20);
                bar.setProgress(0);
                bar.setVisible(true);
                bar.addPlayer(target);
                playerBars.put(target, bar);

                sender.sendMessage(ChatColor.GREEN + "BossBar für " + target.getName() + " erstellt.");
            }

            case "update_level" -> {
                Player target = Bukkit.getPlayer(params.get("player"));
                String levelStr = params.get("level");

                if (target == null || !playerBars.containsKey(target)) {
                    sender.sendMessage(ChatColor.RED + "BossBar für Spieler nicht gefunden.");
                    return;
                }

                try {
                    int level = Integer.parseInt(levelStr);
                    int xp = 0;

                    playerLevels.put(target, level);
                    playerXp.put(target, xp);

                    BossBar bar = playerBars.get(target);
                    bar.setTitle(getBarTitle(level, xp));
                    bar.setProgress(0);

                    sender.sendMessage(ChatColor.GREEN + "Level für " + target.getName() + " auf " + level + " gesetzt.");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Ungültige Level-Zahl.");
                }
            }

            case "add_xp" -> {
                Player target = Bukkit.getPlayer(params.get("player"));
                String amountStr = params.get("amount");

                if (target == null || !playerBars.containsKey(target)) {
                    sender.sendMessage(ChatColor.RED + "Spieler oder BossBar nicht gefunden.");
                    return;
                }

                try {
                    int amount = Integer.parseInt(amountStr);
                    int currentXp = playerXp.getOrDefault(target, 0);
                    int currentLevel = playerLevels.getOrDefault(target, 1);

                    currentXp += amount;

                    // XP-Check für Level-Ups
                    while (currentXp >= getXpNeeded(currentLevel)) {
                        currentXp -= getXpNeeded(currentLevel);
                        currentLevel++;
                        sender.sendMessage(ChatColor.YELLOW + target.getName() + " ist jetzt Level " + currentLevel + "!");
                    }

                    playerLevels.put(target, currentLevel);
                    playerXp.put(target, currentXp);

                    BossBar bar = playerBars.get(target);
                    bar.setTitle(getBarTitle(currentLevel, currentXp));
                    bar.setProgress((double) currentXp / getXpNeeded(currentLevel));

                    sender.sendMessage(ChatColor.GREEN + "XP für " + target.getName() + " hinzugefügt.");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Ungültige XP-Zahl.");
                }
            }

            default -> sender.sendMessage(ChatColor.RED + "Unbekannter Befehl.");
        }
    }

    private int getXpNeeded(int level) {
        return 50 * level; // z.B. Level 1 -> 50 XP, Level 2 -> 100 XP, usw.
    }

    private String getBarTitle(int level, int xp) {
        return ChatColor.GOLD + "Level: " + level + ChatColor.GRAY + " | XP: " + xp + "/" + getXpNeeded(level);
    }
}
