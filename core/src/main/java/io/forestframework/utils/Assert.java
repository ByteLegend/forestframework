package io.forestframework.utils;

public final class Assert {
    public static void isTrue(boolean condition) {
        isTrue(condition, "Condition must be true!");
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
