package com.gmail.sintinium.peacekeeper.queue;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.Bukkit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseQueueManager {

    public final Runnable thread;
    public Peacekeeper peacekeeper;
    public Queue<IQueueableTask> queue;
    private IQueueableTask currentTask;
    private boolean running = true;

    public DatabaseQueueManager(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        queue = new ConcurrentLinkedQueue<>();
        thread = new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        while (queue.isEmpty()) {
                            synchronized (thread) {
                                wait();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentTask = queue.poll();
                    if (currentTask == null) return;
                    currentTask.runTask();
                }
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(peacekeeper, thread);
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
