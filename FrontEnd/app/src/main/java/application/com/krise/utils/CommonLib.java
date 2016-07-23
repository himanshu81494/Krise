package application.com.krise.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import org.apache.http.Header;
//import org.apache.http.HttpResponse;

/**
 * Created by dell on 16-Jul-16.
 */
public class CommonLib {
    public static final String SERVER_URL = "http://10.1.32.116:5000/";
    public static final int LOCATION_DETECTION_RUNNING = 6;
    public static final int LOCATION_NOT_DETECTED = 1;
    public static final int LOCATION_DETECTED = 2;
    public static final int LOCATION_NOT_ENABLED = 0;

    public static final int LOGIN = 228;
    public static final int SIGNUP = 229;
    public static final int REGISTER = 230;
    public static final int UPDATE_ID = 231;
    public static final int EMERGENCY = 232;
    public static final int DISASTER = 233;

    private static SharedPreferences prefs;

    /**
     * Preferences
     */
    public final static String APP_SETTINGS = "application_settings";
    public static final String PROPERTY_REG_ID = "registration_id";
    /**
     * Places API Key
     */
    public static final String GOOGLE_PLACES_API = "AIzaSyC74ve88RgucJzw8U5WSkRVA4z3xQCxi20";

    /**
     * GCM Sender ID
     */
    public static final String GCM_SENDER_ID = "433920600854";

    public static final String api_key = "AIzaSyDrvGfB9D1t6WJPfOiZ1swauKo7NQ7-rm0";

    public static final String key = "AIzaSyCOV9NF0aE1yDc3SXp8_UwnnfmFpcRA3-c";

    // Broadcasts generated after gcm messages are recieved.
    public static final String LOCAL_SMS_BROADCAST = "sms-phone-verification-message";

    public static final String LOCAL_PUSH_PROMOTIONAL_BROADCAST = "LOCAL_PUSH_PROMOTIONAL_BROADCAST";

    /**
     * NOTIFICATION
     */
    public static final String DISASTER_NOTIFICATION = "NOTIFICATION_DISASTER";
    /**
     * Thread pool executors
     */
    private static final int mImageAsyncsMaxSize = 4;
    public static final BlockingQueue<Runnable> sPoolWorkQueueImage = new LinkedBlockingQueue<Runnable>(128);
    private static ThreadFactory sThreadFactoryImage = new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    };
    public static final Executor THREAD_POOL_EXECUTOR_IMAGE = new ThreadPoolExecutor(mImageAsyncsMaxSize,
            mImageAsyncsMaxSize, 1, TimeUnit.SECONDS, sPoolWorkQueueImage, sThreadFactoryImage);

    // Calculate the sample size of bitmaps
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;
        double ratioH = (double) options.outHeight / reqHeight;
        double ratioW = (double) options.outWidth / reqWidth;

        int h = (int) Math.round(ratioH);
        int w = (int) Math.round(ratioW);

        if (h > 1 || w > 1) {
            if (h > w) {
                inSampleSize = h >= 2 ? h : 2;

            } else {
                inSampleSize = w >= 2 ? w : 2;
            }
        }
        return inSampleSize;
    }

    public static final Hashtable<String, Typeface> typefaces = new Hashtable<String, Typeface>();

    public static Typeface getTypeface(Context c, String name) {
        synchronized (typefaces) {
            if (!typefaces.containsKey(name)) {
                try {
                    InputStream inputStream = c.getAssets().open(name);
                    File file = createFileFromInputStream(inputStream, name);
                    if (file == null) {
                        return Typeface.DEFAULT;
                    }
                    Typeface t = Typeface.createFromFile(file);
                    typefaces.put(name, t);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Typeface.DEFAULT;
                }
            }
            return typefaces.get(name);
        }
    }

    private static File createFileFromInputStream(InputStream inputStream, String name) {

        try {
            File f = File.createTempFile("font", null);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return f;
        } catch (Exception e) {
            // Logging exception
            e.printStackTrace();
        }

        return null;
    }

    /*public static InputStream getStream(HttpResponse response) throws IllegalStateException, IOException {
        InputStream instream = response.getEntity().getContent();
        Header contentEncoding = response.getFirstHeader("Content-Encoding");
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
            instream = new GZIPInputStream(instream);
        }
        return instream;
    }*/

    // Checks if network is available
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return distance in km
     */

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        return dist;
    }
    public static boolean isAndroidL() {
        return android.os.Build.VERSION.SDK_INT >= 21;
    }

    public static String getDateFromUTC(long timestamp) {
        Date date = new Date(timestamp);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        return (cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DATE) + " " + cal.get(Calendar.HOUR) + ":"
                + cal.get(Calendar.MINUTE) + (cal.get(Calendar.AM_PM) == 0 ? "AM" : "PM"));
    }



    /**
     * Returns the bitmap associated
     */
    public static Bitmap getBitmap(Context mContext, int resId, int width, int height) throws OutOfMemoryError {
        if (mContext == null)
            return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(mContext.getResources(), resId, options);
        options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Config.RGB_565;

        if (!CommonLib.isAndroidL())
            options.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId, options);

        return bitmap;
    }



    public static String constructFileName(String url) {
        return url.replaceAll("/", "_");
    }

    public static Bitmap getRoundedCornerBitmap(final Bitmap bitmap, final float roundPx) {

        if (bitmap != null) {
            try {
                final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
                Canvas canvas = new Canvas(output);

                final Paint paint = new Paint();
                final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                final RectF rectF = new RectF(rect);

                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

                paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
                canvas.drawBitmap(bitmap, rect, rect, paint);

                return output;

            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    public static String getTimeDifferenceString(Date fromDate) {

        Date currentDate = new Date(System.currentTimeMillis());

        if (currentDate.after(fromDate)) {
            currentDate = new Date(System.currentTimeMillis());
        } else {
            currentDate = fromDate;
            fromDate = new Date(System.currentTimeMillis());
        }

        StringBuilder builder = new StringBuilder();
        int year = currentDate.getYear() - fromDate.getYear();
        int month = currentDate.getMonth() - fromDate.getMonth();
        int date = currentDate.getDate() - fromDate.getDate();
        int hour = currentDate.getHours() - fromDate.getHours();
        int minute = currentDate.getMinutes() - fromDate.getMinutes();

        if (year > 1)
            builder.append(year + " years, ");

        if (month > 1)
            builder.append(month + " months, ");

        if (date > 1)
            builder.append(date + " days, ");
        else if (date == 1)
            builder.append(date + " day, ");

        if (hour > 1)
            builder.append(hour + " hours, ");
        else if (hour == 1)
            builder.append(hour + " hour, ");

        if (minute > 1)
            builder.append(minute + " minutes, ");
        else if (minute == 1)
            builder.append(minute + " minute, ");

        return builder.toString().substring(0, builder.toString().length() - 2);
    }

    /**
     * Remove the keyboard explicitly.
     */
    public static void hideKeyBoard(Activity mActivity, View mGetView) {
        try {
            ((InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(mGetView.getRootView().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void showSoftKeyboard(Context context, View v) {
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
    public static boolean isDayTime() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12){
            return true;
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            return true;
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            return true;
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            return false;
        }
        return true;
    }
}
