package application.com.krise.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Created by dell on 16-Jul-16.
 */
public class PostWrapper {

    private static SharedPreferences prefs;

    /** Constants */
    public static String LOGOUT = "logout";
    public static String LOGIN = "register";
    public static String SIGNUP = "signup";

    public static void Initialize(Context context) {
        // helper = new ResponseCacheManager(context);
        prefs = context.getSharedPreferences("application_settings", 0);
    }

    public static String convertStreamToString(InputStream is) {
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }


}
