package com.larvalabs.redditchat.util;

import java.util.Random;
import java.util.UUID;

/**
 * Created by matt on 12/22/15.
 */
public class Util {

    private static Random random = new Random();

    /**
     * Generate random string ID taken from random long value, only using positive values.
     * @return positive full alphabet string representation of random long value
     */
    public static String getShortRandomId() {
        long l = Math.abs(random.nextLong());
        return Long.toString(l, 26);
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }
}
