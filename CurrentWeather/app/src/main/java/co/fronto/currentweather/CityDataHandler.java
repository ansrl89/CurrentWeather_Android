package co.fronto.currentweather;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import co.fronto.currentweather.data.CityData;

/**
 * Created by MoonKi on 8/9/16.
 */
public class CityDataHandler {
    private static final int MAX_NUMBER = 4; // Maximum Number of items
    private static final String FILENAME = "city_history";

    private Context mContext = null;
    private SharedPreferences mSharedPreferences = null;

    private LinkedList<CityData> mCityDataList = null;

    public CityDataHandler(Context context){
        mContext = context;
    }

    public boolean add(JSONObject jsonWeather){
        CityData cityData = new CityData();
        if(cityData.parseData(jsonWeather)){    // if data added, return true
            readSavedData();

            if(alreadyHas(cityData)){   // if already has the item, do not add
                return false;
            }

            removeOldestOne();  // keep recent 4 items
            mCityDataList.add(cityData);
            sortList(); // sorting list
            writeData();
            return true;
        } else { // if data not added, return false
            return false;
        }
    }

    /**
     * read data from sharedpreferences and set the member list the content
     * @return
     */
    public boolean readSavedData(){
        getmSharedPreferences();

        // reset List
        if(mCityDataList != null){
            mCityDataList.clear();
            mCityDataList = null;
        }
        getmCityDataList();

        // read all data from SharedPreferences
        HashMap hashMap = (HashMap) mSharedPreferences.getAll();
        Iterator iterator = hashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            JSONObject jsonCityData = (JSONObject) JSONValue.parse(entry.getValue().toString());

            CityData cityData = new CityData();
            cityData.getDataFromJSONString(jsonCityData);

            mCityDataList.add(cityData);
        }

        // sorting
        sortList();

        return true;
    }

    /**
     * if the item already exists on list, don't need to add
     * @param cityData
     * @return
     */
    private boolean alreadyHas(CityData cityData){
        for(int i=0; i<mCityDataList.size(); i++){
            if(cityData.cityName.equals(mCityDataList.get(i).cityName))
                return true;
        }

        return false;
    }

    /**
     * remove the Oldest One in the list to keep 4 items
     */
    private void removeOldestOne(){
        if(mCityDataList.size() < MAX_NUMBER) return;

        while (mCityDataList.size() >= MAX_NUMBER) {
            int index = 0;
            long timestamp = 0;

            for (int i = 0; i < mCityDataList.size(); i++) {
                if (i == 0) {
                    timestamp = mCityDataList.get(i).timestamp;
                    index = i;
                } else {
                    if (timestamp > mCityDataList.get(i).timestamp)
                        index = i;
                }
            }
            mCityDataList.remove(index);
        }
    }

    /**
     * sorting the member list
     */
    private void sortList(){
        Collections.sort(mCityDataList, new CompareByName());
    }

    /**
     * sorting list by cityName of CityData
     */
    static class CompareByName implements Comparator<CityData> {
        @Override
        public int compare(CityData lhs, CityData rhs) {
            return (lhs.cityName.compareTo(rhs.cityName));
        }
    }

    /**
     * getter
     * @return
     */
    public SharedPreferences getmSharedPreferences() {
        if(mSharedPreferences == null) {
            mSharedPreferences = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    /**
     * getter
     * @return
     */
    public LinkedList<CityData> getmCityDataList() {
        if(mCityDataList == null)
            mCityDataList = new LinkedList<>();
        return mCityDataList;
    }

    /**
     * write data at sharedPrefernences
     */
    public void writeData(){
        getmSharedPreferences();

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();

        for(int i=0; i<mCityDataList.size(); i++){
            CityData cityData = mCityDataList.get(i);
            editor.putString(cityData.cityName, cityData.toJSONString());
        }

        editor.commit();
    }
}
