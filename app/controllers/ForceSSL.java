package controllers;

import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.server.Server;

public class ForceSSL extends Controller {

    @Before(priority = 10)
    static void redirectToHttps() {
        Http.Request request = Http.Request.current();

        redirectIfNeeded(request);
    }

    /**
     * Protected scope to stop it from being an external URL, just want it to be a utility method.
     *
     * @param request
     */
    protected static void redirectIfNeeded(Http.Request request) {
        if (request.headers.get("x-forwarded-proto") != null) {
//            Logger.info("Found x-forwarded header :" + request.headers.get("x-forwarded-proto"));
            request.secure = request.headers.get("x-forwarded-proto").values.contains("https");
        }
        if (!request.secure) {
            request.secure = true;
            if (Server.httpsPort > 0) {
                request.port = Server.httpsPort;
            }
            Logger.info("Redirecting to https with path: " + request.url);
            flash.keep();
            redirect(request.url);
        }
    }

}
