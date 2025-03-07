package com.alan.clients.util;

public final class MSTimer {
    public long time = -1L;

    private long lastMS;

    public long getDifference() {
        return this.getCurrentMS() - this.lastMS;
    }
    public boolean hasTimePassed(final long MS) {
        return System.currentTimeMillis() >= time + MS;
    }
    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset) reset();
            return true;
        }

        return false;
    }

    public void reset() {
        time = System.currentTimeMillis();
    }

    public boolean hasReached(double milliseconds) {
        return (double)(this.getCurrentMS() - this.lastMS) >= milliseconds;
    }

    private long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

}
