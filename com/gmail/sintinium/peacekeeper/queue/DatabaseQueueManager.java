package com.gmail.sintinium.peacekeeper.queue;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.Bukkit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseQueueManager {

    public Peacekeeper peacekeeper;
    public Queue<IQueueableTask> queue;
    public Runnable thread;
    private IQueueableTask currentTask;
    private boolean running = true;

    public DatabaseQueueManager(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        queue = new ConcurrentLinkedQueue<>();
        Bukkit.getScheduler().runTaskAsynchronously(peacekeeper, thread);
        initThread();
    }

    public void initThread() {
        thread = new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        while (queue.isEmpty()) wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentTask = queue.poll();
                    if (currentTask == null) return;
                    currentTask.runTask();
                }
            }
        };
    }

    public void schedualTask(IQueueableTask task) {
        queue.add(task);
        thread.notify();
    }

    public void onExit() {
        queue.clear();
        running = false;
    }

}
