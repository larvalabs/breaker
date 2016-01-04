package controllers;

import models.ChatUser;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * Created by matt on 1/3/16.
 */
public class PreloadUserController extends Controller {

    public static final String SESSION_UID = "uid";

    @Before
    static void preloadUser() {
        ChatUser user = null;
//        session.put(SESSION_UID, "2hfc8agp4k9ane");
        if (session.contains("uid")) {
            String uid = session.get(SESSION_UID);
            Logger.info("existing user: " + uid);
            user = ChatUser.get(uid);
            if (user != null) {
                renderArgs.put("user", user);
            }
        }
        // Only create user on successful auth
/*
        if (user == null) {
            user = ChatUser.createNew();
            session.put("uid", user.uid);
        }
*/
    }

    static void setUserInSession(ChatUser user) {
        session.put(SESSION_UID, user.uid);
        renderArgs.put("user", user);
    }

    static ChatUser connected() {
        return (ChatUser) renderArgs.get("user");
    }

}
