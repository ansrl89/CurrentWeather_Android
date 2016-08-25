package co.fronto.currentweather.data;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by MoonKi on 8/9/16.
 */
public class Const {
    /**
     * If RELEASE_FLAG = true, Log does not work
     * If RELEASE_FLAG = false, Log shows up
     */
    public static final boolean RELEASE_FLAG = true;

    /**
     * Tag name for Logging
     */
    public static final String TAG = "CurrentWeather";

    public static void Log(String msg){
        if(!RELEASE_FLAG)
            Log.d(TAG, msg);
    }

    public static void LogE(String msg){
        if(!RELEASE_FLAG)
            Log.e(TAG, msg);
    }

    /**
     * show stack trace and log at once
     * @param e All Exceptions
     */
    public static void LogE(Exception e){
        if(e == null) {
            LogE("Exception e is null");
            return;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        LogE(sw.toString());
    }

    public static final String WEBSERVICE_URL =  "http://api.openweathermap.org/data/2.5/weather";
    public static final String WEBSERVICE_APPID = "f864249deb6cb57ef6dee5ec303a4b65";
    public static final String WEBSERVICE_APPID_KEY = "APPID";
    public static final String WEBSERVICE_QUERY_KEY = "q";

    public static final String KEY_LAT = "lat";
    public static final String KEY_LON = "lon";
    public static final String KEY_CITYNAME = "cityName";
}
