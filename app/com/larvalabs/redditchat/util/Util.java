package com.larvalabs.redditchat.util;

import com.larvalabs.redditchat.Constants;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

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

    public static String clean(String input) {
        return cleanAndLimitLength(input, Integer.MAX_VALUE);
    }

    public static String cleanAndLimitLength(String input, int length) {
        String cleaned = Jsoup.clean(input, Whitelist.none());
        String userMessageJson = cleaned.substring(0, Math.min(cleaned.length(), length));
        return userMessageJson;
    }

}
