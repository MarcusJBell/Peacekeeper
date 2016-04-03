package com.gmail.sintinium.peacekeeper.io;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogFile {

    private Peacekeeper peacekeeper;

    public LogFile(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    public void logToFile(String message) {
        File folder = new File(peacekeeper.getDataFolder(), File.separator + "logs");
        if (!folder.exists())
            folder.mkdir();
        File file = new File(peacekeeper.getDataFolder(), File.separator + "logs" + File.separator + "log.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            PrintWriter writer = new PrintWriter(fileWriter);
            writer.println("[" + TimeUtils.formatTime(System.currentTimeMillis()) + "] " + message);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logChat(String player, String message) {
        File folder = new File(peacekeeper.getDataFolder(), File.separator + "logs");
        if (!folder.exists())
            folder.mkdir();
        File file = new File(peacekeeper.getDataFolder(), File.separator + "logs" + File.separator + "chatlogs.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            PrintWriter writer = new PrintWriter(fileWriter);
            writer.println("[" + TimeUtils.formatTime(System.currentTimeMillis()) + "] (" + player + ") " + message);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logBlockedChat(String player, String message) {
        File folder = new File(peacekeeper.getDataFolder(), File.separator + "logs");
        if (!folder.exists())
            folder.mkdir();
        File file = new File(peacekeeper.getDataFolder(), File.separator + "logs" + File.separator + "blockedlog.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            PrintWriter writer = new PrintWriter(fileWriter);
            writer.println("[" + TimeUtils.formatTime(System.currentTimeMillis()) + "] (" + player + ") " + message);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logBook(String player, String message) {
        File folder = new File(peacekeeper.getDataFolder(), File.separator + "logs");
        if (!folder.exists())
            folder.mkdir();
        File file = new File(peacekeeper.getDataFolder(), File.separator + "logs" + File.separator + "booklogs.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            PrintWriter writer = new PrintWriter(fileWriter);
            writer.println("[" + TimeUtils.formatTime(System.currentTimeMillis()) + "] (" + player + ") " + message);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logSign(String player, String message) {
        File folder = new File(peacekeeper.getDataFolder(), File.separator + "logs");
        if (!folder.exists())
            folder.mkdir();
        File file = new File(peacekeeper.getDataFolder(), File.separator + "logs" + File.separator + "signlogs.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            PrintWriter writer = new PrintWriter(fileWriter);
            writer.println("[" + TimeUtils.formatTime(System.currentTimeMillis()) + "] (" + player + ") " + message);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
