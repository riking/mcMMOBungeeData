package me.riking.bungeemmo.bukkit.fetcher;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.riking.bungeemmo.bukkit.BukkitPlugin;

public class DataFuture<V> implements Future<V> {
    private V object;
    private boolean cancelled;
    private volatile boolean done;

    // Called from main server thread
    public void fulfill(V obj) {
        synchronized (this) {
            done = true;
            object = obj;
        }
    }

    @Override
    public boolean cancel(boolean arg0) {
        if (done) {
            return false;
        }
        cancelled = arg0;
        return true;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        // First try with short timeout
        try {
            return get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // Check for deadlock
            if (!BukkitPlugin.getInstance().connMan.isConnected()) {
                cancelled = true;
                throw new ExecutionException(new IOException("Server has been disconnected from BungeeCord, aborting rank fetch"));
            }
        }

        try {
            return get(45, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            if (!BukkitPlugin.getInstance().connMan.isConnected()) {
                cancelled = true;
                throw new ExecutionException(new IOException("Server has been disconnected from BungeeCord, aborting rank fetch"));
            }
            // TODO resend request?
        }

        while (!done && !cancelled) {
            Thread.sleep(1);
        }
        // Avoid race condition for middle of fulfill()
        synchronized (this) {
            return object;
        }
    }

    @Override
    public V get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
        long stopTime = System.currentTimeMillis() + arg1.convert(arg0, TimeUnit.MILLISECONDS);

        while (!done && !cancelled) {
            Thread.sleep(1);
            if (System.currentTimeMillis() > stopTime) {
                throw new TimeoutException();
            }
        }

        synchronized (this) {
            return object;
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
