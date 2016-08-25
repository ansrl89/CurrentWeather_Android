package co.fronto.currentweather.data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by MoonKi on 8/9/16.
 */
public class CityData{
    public long timestamp;
    public String cityName;
    public String description;
    public String temperature;
    public float lon;
    public float lat;

    /**
     * get Data to fill member variables from JsonObject, WebService
     * @param jsonResponse
     * @return
     */
    public boolean parseData(JSONObject jsonResponse){
        try{
            // get lon, lat
            JSONObject coord = (JSONObject) jsonResponse.get("coord");
            lon = Float.valueOf(coord.get("lon").toString());
            lat = Float.valueOf(coord.get("lat").toString());

            // get description
            JSONArray weather = (JSONArray) jsonResponse.get("weather");
            JSONObject weather0 = (JSONObject) weather.get(0);
            description = weather0.get("description").toString();

            // get cityName
            cityName = jsonResponse.get("name").toString();

            // get temperature
            JSONObject main = (JSONObject) jsonResponse.get("main");
            float floatTemperature = Float.valueOf(main.get("temp").toString());
            temperature = String.valueOf(Math.round(floatTemperature));

            // get timestamp
            timestamp = System.currentTimeMillis();

            return true;
        } catch(Exception e){
            Const.LogE(e);
            return false;
        }
    }

    /**
     *
     * @return make member variables to JSONObject, to String
     */
    public String toJSONString(){
        JSONObject jobj = new JSONObject();

        jobj.put("timestamp", timestamp);
        jobj.put("cityName", cityName);
        jobj.put("description", description);
        jobj.put("temperature", temperature);
        jobj.put("lon", lon);
        jobj.put("lat", lat);

        return jobj.toJSONString();
    }

    /**
     * set variabes by JSONObject made by toJSONString
     * @param jobj input JSONObject
     */
    public void getDataFromJSONString(JSONObject jobj){
        timestamp = Long.parseLong(jobj.get("timestamp").toString());
        cityName = jobj.get("cityName").toString();
        description = jobj.get("description").toString();
        temperature = jobj.get("temperature").toString();
        lon = Float.parseFloat(jobj.get("lon").toString());
        lat = Float.parseFloat(jobj.get("lat").toString());
    }
}