package io.github.mortuusars.exposure.util;

public class UnixTimestamp {
    public static class Seconds {
        public static long now() {
            return System.currentTimeMillis() / 1000;
        }

        public static long fromNow(int seconds) {
            return now() + seconds;
        }
    }

    public static class Milliseconds {
        public static long now() {
            return System.currentTimeMillis();
        }
    }
}
