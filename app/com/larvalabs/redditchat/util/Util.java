package com.larvalabs.redditchat.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.nibor.autolink.*;

import java.util.*;

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

    public static Iterable<LinkSpan> getLinkSpans(String message) {
        LinkExtractor linkExtractor = LinkExtractor.builder()
                .linkTypes(EnumSet.of(LinkType.URL)) // limit to URLs
                .build();

        final ArrayList<String> localLinks = new ArrayList<String>();
        final ArrayList<String> localImageLinks = new ArrayList<String>();

        Iterable<LinkSpan> links = linkExtractor.extractLinks(message);
        return links;
    }

    public static List<String> getLinks(String message) {
        Iterable<LinkSpan> linkSpans = getLinkSpans(message);
        final ArrayList<String> links = new ArrayList<>();
        Autolink.renderLinks(message, linkSpans,
                new LinkRenderer() {
                    @Override
                    public void render(LinkSpan link, CharSequence text, StringBuilder sb) {
                        String linkStr = text.toString().substring(link.getBeginIndex(), link.getEndIndex());
                        links.add(linkStr);
                    }
                });
        return links;
    }
}
