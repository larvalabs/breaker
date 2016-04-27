package reddit;

/**
 * Created by euler on 4/27/16.
 */
public class RedditRequestError extends Exception{

    public RedditRequestError() {
        super();
    }

    public RedditRequestError(String message) {
        super(message);
    }
}
