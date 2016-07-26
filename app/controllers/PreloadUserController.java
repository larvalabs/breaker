package controllers;

import models.ChatUser;
import play.Logger;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

/**
 * Created by matt on 1/3/16.
 */
@With(ForceSSL.class)
public class PreloadUserController extends Controller {

    public static final String SESSION_UID = "uid";
    public static final String REMEMBERME_COOKIE = "rememberme";
    public static final String HEADER_MOBILE_ACCESS = "x-breakeraccesscode";

    @Before
    static void preloadUser() {
        Http.Cookie remember = request.cookies.get(REMEMBERME_COOKIE);
        ChatUser user = null;
//        session.put(SESSION_UID, "2hfc8agp4k9ane");
        if (session.contains("uid")) {
            String uid = session.get(SESSION_UID);
            Logger.info("existing user: " + uid);
            user = ChatUser.get(uid);
            if (user != null) {
                renderArgs.put("user", user);
            }
        } else if (request.headers.containsKey(HEADER_MOBILE_ACCESS)) {
            String accessCode = request.headers.get(HEADER_MOBILE_ACCESS).value();
            String uid = Crypto.decryptAES(accessCode);
            Logger.info("Mobile access header: " + accessCode + ", uid: " + uid);
            user = ChatUser.get(uid);
            if (user != null) {
                renderArgs.put("user", user);
            }
        } else if (remember != null && remember.value.indexOf("-") > 0) {
            String sign = remember.value.substring(0, remember.value.indexOf("-"));
            String username = remember.value.substring(remember.value.indexOf("-") + 1);
            Logger.info("rememberme activated, found username " + username);
            if (Crypto.sign(username).equals(sign)) {
                Logger.info("Rememberme worked, redirecting to original url...");
                user = ChatUser.findByUsername(username);
                if (user != null) {
                    setUserInSession(user);
                } else {
                    Logger.warn("Couldn't find user from rememberme cookie.");
                    request.cookies.remove(REMEMBERME_COOKIE);
                }
//                session.put("username", username);
//                redirectToOriginalURL();
            }
        }

    }

    static void setUserInSession(ChatUser user) {
        Logger.info("Storing " + user.getUsername() + " in rememberme cookie.");
        session.put(SESSION_UID, user.uid);
        if (!user.isGuest()) {
            String username = user.getUsername();
            response.setCookie(REMEMBERME_COOKIE, Crypto.sign(username) + "-" + username, "7d");
        }
        renderArgs.put("user", user);
    }

    static ChatUser connected() {
        return (ChatUser) renderArgs.get("user");
    }

}
