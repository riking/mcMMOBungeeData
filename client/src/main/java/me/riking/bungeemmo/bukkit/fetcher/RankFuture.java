package me.riking.bungeemmo.bukkit.fetcher;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.riking.bungeemmo.common.data.TransitPlayerRank;

public class RankFuture implements Future<TransitPlayerRank> {
    private TransitPlayerRank object;
    private boolean cancelled;
    private volatile boolean done;

    // Called from main server thread
    public void fulfill(TransitPlayerRank obj) {
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
    public TransitPlayerRank get() throws InterruptedException, ExecutionException {
        while (!done && !cancelled) {
            Thread.sleep(1);
        }
        // Avoid race condition for middle of fulfill()
        synchronized (this) {
            return object;
        }
    }

    @Override
    public TransitPlayerRank get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
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
