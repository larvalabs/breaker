package com.larvalabs.redditchat.dataobj;

/**
 * Created by matt on 4/27/16.
 */
public class JsonFlair {

    public String flairText;
    public String flairCss;
    public String flairPosition;

    public JsonFlair(String flairText, String flairCss, String flairPosition) {
        this.flairText = flairText;
        this.flairCss = flairCss;
        this.flairPosition = flairPosition;
    }
}
