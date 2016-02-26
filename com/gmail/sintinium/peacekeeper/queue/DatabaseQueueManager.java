package com.gmail.sintinium.peacekeeper.queue;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseQueueManager {

    public final Runnable thread;
    public final BukkitTask bukkitTask;
    public Peacekeeper peacekeeper;
    public Queue<IQueueableTask> queue;
    public boolean running = true, closed = false;
    private IQueueableTask currentTask;

    public DatabaseQueueManager(final Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        queue = new ConcurrentLinkedQueue<>();
        thread = new Runnable() {
            @Override
            public void run() {
                main:
                while (true) {
                    try {
                        while (queue.isEmpty()) {
                            if (!running) break main;
                            synchronized (thread) {
                                wait();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentTask = queue.poll();
                    try {
                        if (currentTask == null) return;
                        peacekeeper.database.open();
                        try {
                            peacekeeper.database.getConnection().setAutoCommit(false);
                            currentTask.runTask();
                            peacekeeper.database.getConnection().commit();
                            peacekeeper.database.getConnection().setAutoCommit(true);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        peacekeeper.database.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                closed = true;
            }
        };
        bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(peacekeeper, thread);
    }

    public void scheduleTask(IQueueableTask task) {
        queue.add(task);
        synchronized (thread) {
            thread.notify();
        }
    }

    public boolean canExit() {
        if (!queue.isEmpty()) {
            return false;
        }
        running = false;
        return true;
    }

}
