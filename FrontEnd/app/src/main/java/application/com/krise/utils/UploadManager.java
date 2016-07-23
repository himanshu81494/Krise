package application.com.krise.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import application.com.krise.KriseApplication;

/**
 * Created by dell on 16-Jul-16.
 */
public class UploadManager {
    public static Hashtable<Integer, AsyncTask> asyncs = new Hashtable<Integer, AsyncTask>();
    public static Context context;
    private static SharedPreferences prefs;
    private static ArrayList<UploadManagerCallback> callbacks = new ArrayList<UploadManagerCallback>();
    private static KriseApplication zapp;

    public static void setContext(Context context) {
        UploadManager.context = context;
        prefs = context.getSharedPreferences("application_settings", 0);

        if (context instanceof KriseApplication) {
            zapp = (KriseApplication) context;
        }
    }

    public static void addCallback(UploadManagerCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }

    }

    public static void removeCallback(UploadManagerCallback callback) {
        if (callbacks.contains(callback)) {
            callbacks.remove(callback);
        }
    }


    public static void login(String username, String email, String password) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.LOGIN, 0, email, password);
        }
        new Login().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{username, email, password});

    }

    public static void updateRegistrationId(String regId)
    {
        new UpdateRegistrationId().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{regId});
    }


    public static void signup(String username, String email, String password) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.SIGNUP, 0, email, null);
        }
        new SignUp().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{username, email, password});

    }

    public static void register(String username, String dob, String mob, String num1,String num2, String num3) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.SIGNUP, 0, username, null);
        }
        new Register().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{username, dob, mob,num1,num2,num3});

    }

    public static void sendEmergencyMessage(double lat, double lon) {

        new EmergencyMessage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{lat, lon});

    }

    public static void disaster()
    {
        new Disaster().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{});
    }

    private static class UpdateRegistrationId extends AsyncTask<Object, Void, Object[]> {

        private String regId;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            regId = (String) params[0];

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            JSONObject json = new JSONObject();
            String URL= CommonLib.SERVER_URL+"api/gcm";

            try {
                HttpPost post = new HttpPost(URL);
                json.put("token",prefs.getString("access_token",""));
                json.put("regId", regId);
                Log.d("id", regId);
                json.put("api_key", CommonLib.key);
                StringEntity se = new StringEntity( json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);
                HttpEntity entity = response.getEntity();
                String output = EntityUtils.toString(entity);
                JSONObject jObject = new JSONObject(output);
                String mResponse = jObject.getString("response");
                if(mResponse.equalsIgnoreCase("success")){
                    result = new Object[2];
                    result[0] = mResponse;
                    result[1] = "success";
                }else{
                    result = new Object[2];
                    result[0] = mResponse;
                    result[1] = "error occured";
                }
            } catch(Exception e) {
                e.printStackTrace();
            }


            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.UPDATE_ID, 0, 0, arg[1], 0,

                        arg[0].equals("success"), "");
            }
        }
    }

    private static class Login extends AsyncTask<Object, Void, Object[]> {

        private String name, email, password;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            name = (String) params[0];
            email = (String) params[1];
            password = (String) params[2];

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            JSONObject json = new JSONObject();
            String URL= CommonLib.SERVER_URL+"api/login";

            try {
                HttpPost post = new HttpPost(URL);
                json.put("email", email);
                json.put("password", password);
                StringEntity se = new StringEntity( json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);
                HttpEntity entity = response.getEntity();
                String output = EntityUtils.toString(entity);
                JSONObject jObject = new JSONObject(output);
                String mResponse = jObject.getString("response");
                if(mResponse.equalsIgnoreCase("success")){
                    String token = jObject.getString("token");
                    result = new Object[2];
                    result[0] = mResponse;
                    result[1] = token;
                }else{
                    result = new Object[2];
                    result[0] = mResponse;
                    result[1] = "error occured";
                }


            } catch(Exception e) {
                e.printStackTrace();
            }


            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.LOGIN, 0, 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class SignUp extends AsyncTask<Object, Void, Object[]> {

        private String name, email, password;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            name = (String) params[0];
            email = (String) params[1];
            password = (String) params[2];

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            JSONObject json = new JSONObject();
            String URL= CommonLib.SERVER_URL+"api/register";

            try {
                HttpPost post = new HttpPost(URL);
                json.put("username",name);
                json.put("email", email);
                json.put("password", password);
                StringEntity se = new StringEntity( json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);
                HttpEntity entity = response.getEntity();
                String output = EntityUtils.toString(entity);
                JSONObject jObject = new JSONObject(output);
                String mResponse = jObject.getString("response");
                if(mResponse.equalsIgnoreCase("success")){
                    result = new Object[2];
                    result[0] = mResponse;
                    result[1] = "success";
                }else{
                    result = new Object[2];
                    result[0] = mResponse;
                    result[1] = "error occured";
                }


            } catch(Exception e) {
                e.printStackTrace();
            }


            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.SIGNUP, 0, 0, arg[1], 0,

                        arg[0].equals("success"), "");
            }
        }
    }

    private static class Register extends AsyncTask<Object, Void, Object[]> {

        private String name, dob, mob,num1,num2,num3;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            name = (String) params[0];
            dob = (String) params[1];
            mob = (String) params[2];
            num1 = (String) params[3];
            num2 = (String) params[4];
            num3 = (String) params[5];

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            JSONObject json = new JSONObject();
            String URL= CommonLib.SERVER_URL+"api/filldetails";

            try {
                HttpPost post = new HttpPost(URL);
                json.put("token",prefs.getString("access_token",""));
                json.put("name",name);
                json.put("mobileno", mob);
                json.put("contacts", num1+","+num2+","+num3);
                json.put("lat",zapp.lat);
                json.put("lon",zapp.lon);
                json.put("gcmid",CommonLib.GCM_SENDER_ID);
                StringEntity se = new StringEntity( json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);
                HttpEntity entity = response.getEntity();
                String output = EntityUtils.toString(entity);
                JSONObject jObject = new JSONObject(output);
                String mResponse = jObject.getString("response");
                if(mResponse.equalsIgnoreCase("success")){
                    result = new Object[2];
                    result[0] = mResponse;
                    result[1] = "success";
                }else{
                    result = new Object[2];
                    result[0] = mResponse;
                    result[1] = "error occured";
                }


            } catch(Exception e) {
                Log.d("abc", e.toString());
            }


            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.REGISTER, 0, 0, arg[1], 0,

                        arg[0].equals("success"), "");
            }
        }
    }

    private static class EmergencyMessage extends AsyncTask<Object, Void, Object[]> {

        private double lat, lon;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            lat = (double) params[0];
            lon = (double) params[1];

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            JSONObject json = new JSONObject();
            String URL= CommonLib.SERVER_URL+"api/emergency";

            try {
                HttpPost post = new HttpPost(URL);
                json.put("token",prefs.getString("access_token",""));
                json.put("lat",lat);
                json.put("lon", lon);
                StringEntity se = new StringEntity( json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);
                HttpEntity entity = response.getEntity();
                String output = EntityUtils.toString(entity);
                JSONObject jObject = new JSONObject(output);
                String mResponse = jObject.getString("response");
                if(mResponse.equalsIgnoreCase("success")){
                    result = new Object[2];
                    result[0] = mResponse;
                    result[1] = "success";
                }else{
                    result = new Object[2];
                    result[0] = mResponse;
                    result[1] = "error occured";
                }


            } catch(Exception e) {
                Log.d("abc",e.toString());
            }


            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.EMERGENCY, 0, 0, arg[1], 0,

                        arg[0].equals("success"), "");
            }
        }
    }

    private static class Disaster extends AsyncTask<Object, Void, Object[]> {
        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            JSONObject json = new JSONObject();
            String URL= CommonLib.SERVER_URL+"api/pushnotif";

            try {
                HttpPost post = new HttpPost(URL);
                json.put("token",prefs.getString("access_token",""));
                StringEntity se = new StringEntity( json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);
                HttpEntity entity = response.getEntity();
                String output = EntityUtils.toString(entity);
                JSONObject jObject = new JSONObject(output);
                int mResponse = jObject.getInt("response");
                if(mResponse==1){
                    result = new Object[2];
                    result[0] = "success";
                    result[1] = jObject.getString("notif");
                }else{
                    result = new Object[2];
                    result[0] = "failure";
                    result[1] = "error occured";
                }


            } catch(Exception e) {
                Log.d("abc",e.toString());
            }


            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.DISASTER, 0, 0, arg[1], 0,

                        arg[0].equals("success"), "");
            }
        }
    }
}
