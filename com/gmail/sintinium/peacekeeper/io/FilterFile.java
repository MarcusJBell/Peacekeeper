package com.gmail.sintinium.peacekeeper.io;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.filter.listeners.ChatSpamFilterListener;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FilterFile {

    public Set<String> blockedWords, semiblocked, exceptions, wholeOnly;
    public Map<String, String> replacedWords;
    //    public Map<String, Long> strictWords;
    public Map<String, Long> strict;
    public Map<Character[], Long> strictChars = new HashMap<>();
    private Peacekeeper peacekeeper;

    public FilterFile(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        loadToggles();
        loadFilter();
        loadSpamFilter();
    }

    public void reloadDefaultConfig() {
        try {
            Reader defConfigStream = new InputStreamReader(peacekeeper.getResource("Filter.yml"), "UTF8");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(defConfigStream);
            File file = new File(peacekeeper.getDataFolder(), "Filter.yml");
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        File file = peacekeeper.getDataFolder();
        if (!file.exists())
            file.mkdir();
        File config = new File(peacekeeper.getDataFolder(), "Filter.yml");
        if (!config.exists()) {
            reloadDefaultConfig();
        }
        return config;
    }


    public void loadSpamFilter() {
        File file = getFile();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ChatSpamFilterListener filter = peacekeeper.chatSpamFilterListener;
        filter.capType = config.getString("spamfilter.type.caps").toLowerCase();
        filter.spamType = config.getString("spamfilter.type.spam").toLowerCase();
        filter.specialType = config.getString("spamfilter.type.specialchars").toLowerCase();
        filter.excessiveCharType = config.getString("spamfilter.type.excessivechars").toLowerCase();

        filter.caps = (float) (config.getInt("spamfilter.amounts.caps")) / 100f;
        filter.spam = (float) (config.getInt("spamfilter.amounts.spam")) / 100f;
        filter.excessiveCharCount = config.getInt("spamfilter.amounts.excessivechars");
        filter.spamCount = config.getInt("spamfilter.amounts.characterspamcount");
    }

    public void loadToggles() {
        File file = getFile();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        peacekeeper.chatBlockingFilterListener.filterChat = config.getBoolean("filter.toggles.chat");
        peacekeeper.chatBlockingFilterListener.filterCommands = config.getBoolean("filter.toggles.commands");
        peacekeeper.chatBlockingFilterListener.filterBook = config.getBoolean("filter.toggles.books");
        peacekeeper.chatBlockingFilterListener.filterItems = config.getBoolean("filter.toggles.items");
        peacekeeper.chatBlockingFilterListener.filterSign = config.getBoolean("filter.toggles.signs");
        peacekeeper.chatBlockingFilterListener.leniency = config.getInt("filter.settings.leniency") - 1;
    }

    public void loadFilter() {
        File file = getFile();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("filter.filteredwords.blocked")) {
            blockedWords = new HashSet<>(config.getStringList("filter.filteredwords.blocked"));
        } else {
            blockedWords = new HashSet<>();
        }

        if (config.contains("filter.filteredwords.semiblocked")) {
            semiblocked = new HashSet<>(config.getStringList("filter.filteredwords.semiblocked"));
        } else {
            semiblocked = new HashSet<>();
        }

        if (config.contains("filter.filteredwords.wholeonly")) {
            wholeOnly = new HashSet<>(config.getStringList("filter.filteredwords.wholeonly"));
        } else {
            wholeOnly = new HashSet<>();
        }

        if (config.contains("filter.filteredwords.exception")) {
            exceptions = new HashSet<>(config.getStringList("filter.filteredwords.exception"));
        } else {
            exceptions = new HashSet<>();
        }

        replacedWords = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("filter.filteredwords.replaced");
        if (section != null && config.contains("filter.filteredwords.replaced")) {
            for (String s : section.getKeys(false)) {
                replacedWords.put(s, config.getString("filter.filteredwords.replaced." + s));
            }
        }

//        strictWords = new HashMap<>();
        strict = new HashMap<>();
        ConfigurationSection strickSelection = config.getConfigurationSection("filter.filteredwords.strict");
        if (strickSelection != null && config.contains("filter.filteredwords.strict")) {
            for (String s : strickSelection.getKeys(false)) {
                String message = config.getString("filter.filteredwords.strict." + s + ".message");
                Long length = config.getLong("filter.filteredwords.strict." + s + ".length");
                strict.put(message, length);
            }
        }

        for (Map.Entry<String, Long> e : strict.entrySet()) {
            Character[] characters = new Character[e.getKey().length()];
            int j = 0;
            for (int i = 0; i < characters.length; i++) {
                char c = e.getKey().charAt(i);
                if (c != '.') {
                    characters[j] = c;
                    j++;
                }
            }
            strictChars.put(characters, e.getValue());
        }
    }

}
