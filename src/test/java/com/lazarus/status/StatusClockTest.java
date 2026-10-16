package com.lazarus.status;

public final class StatusClockTest {
    public static void main(String[] args) {
        assert StatusClock.restoreDays(20, 17) == 72000L;
        assert StatusClock.restoreDays(2, 5) == 0L;
        assert StatusClock.advance(72000L, 1000L, 2000L, true) == 73000L;
        assert StatusClock.advance(73000L, 2000L, 12000L, false) == 73000L;
        // Login sets the previous world time to now, so offline days are not added.
        assert StatusClock.advance(73000L, 12000L, 13000L, true) == 74000L;
        assert StatusClock.advance(74000L, 13000L, 12000L, true) == 74000L;
        assert StatusClock.display(37000L).equals("1d 13h");
    }
}
