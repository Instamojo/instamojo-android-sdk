package instamojo.library;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by shardullavekar on 17/07/17.
 */

public class Config {
    public final static String ORDER_AUTHURL = "instamojo.orderauth.url";
    public final static int SUCCESS = 10;
    public final static int FAILED = 20;
    public final static int INSTAMOJO = 30;
    public final static String TEST = "test";
    public final static String PROD = "prod";


    public static boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidMobile(String phone) {
        if (phone.length()!=10) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        }
    }

    public static boolean isValidAmount(String amount) {
        try {
            return (Double.valueOf(amount) >= 10);
        } catch (Exception e) {
            return false;
        }
    }

    public static void saveEnv(Context context, String env) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = context.getSharedPreferences("ENV", 0);
        editor = sharedPreferences.edit();
        editor.putString("ENVIRONMENT", env);
        editor.apply();
        editor.commit();
    }

    public static String getEnv(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ENV", 0);
        return sharedPreferences.getString("ENVIRONMENT", null);
    }

}
