package instamojo.library;

/**
 * Created by shardullavekar on 06/09/17.
 */

public interface InstapayListener {
    void onSuccess(String response);
    void onFailure(int code, String reason);
}
