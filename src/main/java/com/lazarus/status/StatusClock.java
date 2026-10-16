package com.lazarus.status;

final class StatusClock {
    static final long DAY_TICKS = 24000L;

    private StatusClock() { }

    static long restoreDays(long worldDay, long startDay) {
        return Math.max(0, worldDay - startDay) * DAY_TICKS;
    }

    static long advance(long aliveTicks, long previousWorldTime, long currentWorldTime, boolean surviving) {
        return aliveTicks + (surviving ? Math.max(0, currentWorldTime - previousWorldTime) : 0);
    }

    static String display(long ticks) {
        long safeTicks = Math.max(0, ticks);
        return safeTicks / DAY_TICKS + "d " + safeTicks % DAY_TICKS / 1000L + "h";
    }
}
