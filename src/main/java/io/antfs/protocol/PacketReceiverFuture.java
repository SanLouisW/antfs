package io.antfs.protocol;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * PacketReceiverFuture
 * The response future returned by AbstractPacketSender.sendPacket()
 * @author gris.wang
 * @since 2017/11/20
 */
public class PacketReceiverFuture<T> implements Future {

    private volatile T result;
    private volatile boolean completed;

    public PacketReceiverFuture() {
        completed = false;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return completed;
    }

    @Override
    public synchronized T get() throws InterruptedException, ExecutionException {
        while (!this.completed) {
            wait();
        }
        return result;
    }

    @Override
    public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        while (!this.completed) {
            wait(timeout);
        }
        return result;
    }

    public void complete(T result) {
        this.completed = true;
        this.result = result;
        notifyAll();
    }
}