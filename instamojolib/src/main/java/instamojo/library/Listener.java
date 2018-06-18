package instamojo.library;

import java.io.Serializable;

/**
 * Created by shardullavekar on 05/09/17.
 */

public interface Listener extends Serializable {
    void onSuccess(String response);
    void onFailure(int code, String reason);
}
