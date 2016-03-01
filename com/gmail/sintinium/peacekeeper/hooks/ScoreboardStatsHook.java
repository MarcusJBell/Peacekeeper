package com.gmail.sintinium.peacekeeper.hooks;

import com.github.games647.scoreboardstats.ScoreboardStats;
import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.Bukkit;

public class ScoreboardStatsHook {

    public ScoreboardStats scoreboardStats;
    Peacekeeper peacekeeper;

    public ScoreboardStatsHook(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        scoreboardStats = (ScoreboardStats) Bukkit.getPluginManager().getPlugin("ScoreboardStats");
        if (scoreboardStats == null) {
            return;
        }

        final Peacekeeper f_peacekeeper = this.peacekeeper;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.peacekeeper, new Runnable() {
            @Override
            public void run() {
                if (scoreboardStats != null && !f_peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.isEmpty()) {
                    int count = 0;
                    for (String s : f_peacekeeper.commandManager.superVanishCommand.superVanishedPlayers) {
                        if (Bukkit.getPlayerExact(s) != null) count++;
                    }
                    scoreboardStats.getReplaceManager().updateScore("online", Bukkit.getOnlinePlayers().size() - count);
                }
            }
        }, 0L, 5L * 20L);
    }

    public void loadPlugin() {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("ScoreboardStats"))
            scoreboardStats = (ScoreboardStats) Bukkit.getServer().getPluginManager().getPlugin("ScoreboardStats");
    }

    public void updateScoreboard() {
        try {
            if (scoreboardStats != null) {
                scoreboardStats.getReplaceManager().updateScore("online", Bukkit.getOnlinePlayers().size() - peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.size());
            }
        } catch (Exception e) {
        }
    }

}
