/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.vicky.platform.PlatformScheduler;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ForgePlatformScheduler implements PlatformScheduler {

    private final Queue<ScheduledTask> taskQueue = new ConcurrentLinkedQueue<>();

    public ForgePlatformScheduler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void runMain(Runnable task) {
        taskQueue.add(new ScheduledTask(task, 0));
    }

    @Override
    public void runScheduled(Runnable task, Long tickOffset) {
        taskQueue.add(new ScheduledTask(task, tickOffset != null ? tickOffset : 0));
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Iterator<ScheduledTask> iterator = taskQueue.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();
            task.tick--;
            if (task.tick <= 0) {
                task.run();
                iterator.remove(); // Fast and safe
            }
        }
    }

    private static class ScheduledTask {
        Runnable task;
        long tick;

        ScheduledTask(Runnable task, long tick) {
            this.task = task;
            this.tick = tick;
        }

        void run() {
            try {
                task.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
