package com.larvalabs.redditchat.util;

import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to show an alert at the top of the screen like http://twitter.github.com/bootstrap/#alerts
 * User: matt
 * Date: 10/18/11
 * Time: 11:30 AM
 */
public class TopAlert {

    public static final String FLASH_KEY = "_TopAlert";

    public enum Type {
        WARNING("alert-warning"),
        ERROR("alert-danger"),
        SUCCESS("alert-success"),
        NOTICE("alert-info");

        public String cssClass;

        Type(String cssClass) {
            this.cssClass = cssClass;
        }
    }

    public Type type;
    public String boldMessage;
    public String message;

    public TopAlert(Type type, String boldMessage, String message) {
        this.type = type;
        this.boldMessage = boldMessage;
        this.message = message;
        if (StringUtils.isEmpty(this.message)) {
            this.message = " ";
        }
    }

    public void toFlash(Scope.Flash flash) {
        String s = "" + type.ordinal() + "|" + (boldMessage == null ? "" : boldMessage) + "|" + (message == null ? "" : message);
        int attempts = 0;
        String key = FLASH_KEY + attempts;
        while (flash.contains(key) && attempts < 10) {
            attempts++;
            key = FLASH_KEY + attempts;
        }
        Logger.info("Putting top alert into flash: " + key + ": " + s);
        flash.put(key, s);
    }

    public static List<TopAlert> getFromFlash(Scope.Flash flash) {
        ArrayList<TopAlert> topAlerts = new ArrayList<TopAlert>();
        if (flash == null) {
            return topAlerts;
        }
        int flashCount = 0;
        String flashKey = FLASH_KEY + flashCount;
        String s = flash.get(flashKey);
        while (s != null) {
            flash.discard(flashKey);
            String[] tokens = s.split("\\|");
            try {
                final int index = Integer.parseInt(tokens[0]);
                Logger.info("Found flash key: " + flashKey + ": " + s);
                topAlerts.add(new TopAlert(Type.values()[index], tokens[1], tokens[2]));
            } catch (Exception e) {
                Logger.warn("Warning: problem with top alert flash" + e);
                return null;
            }
            flashCount++;
            flashKey = FLASH_KEY + flashCount;
            s = flash.get(flashKey);
        }
        return topAlerts;
    }

    public static boolean flashContainsAlert(Scope.Flash flash) {
        List<TopAlert> alerts = TopAlert.getFromFlash(flash);
        return alerts != null && alerts.size() > 0;
    }
}
