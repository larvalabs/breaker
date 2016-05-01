package com.larvalabs.redditchat.dataobj;

/**
 * Created by matt on 4/27/16.
 */
public class JsonRoomStyles{

    public String sidebarBackgroundColor;
    public String sidebarTextColor;
    public String sidebarRoomSelectedColor;
    public String sidebarRoomHoverColor;
    public String sidebarRoomTextColor;
    public String sidebarUnreadColor;
    public String sidebarUnreadTextColor;
    public String signinButtonColor;
    public String signinButtonTextColor;

    public JsonRoomStyles(String sidebarBackgroundColor, String sidebarTextColor, String sidebarRoomSelectedColor,
                          String sidebarRoomHoverColor, String sidebarRoomTextColor, String sidebarUnreadColor,
                          String sidebarUnreadTextColor, String signinButtonColor, String signinButtonTextColor) {

        this.sidebarBackgroundColor = sidebarBackgroundColor;
        this.sidebarTextColor = sidebarTextColor;
        this.sidebarRoomSelectedColor = sidebarRoomSelectedColor;
        this.sidebarRoomHoverColor = sidebarRoomHoverColor;
        this.sidebarRoomTextColor = sidebarRoomTextColor;
        this.sidebarUnreadColor = sidebarUnreadColor;
        this.sidebarUnreadTextColor = sidebarUnreadTextColor;
        this.signinButtonColor = signinButtonColor;
        this.signinButtonTextColor = signinButtonTextColor;
    }
}
