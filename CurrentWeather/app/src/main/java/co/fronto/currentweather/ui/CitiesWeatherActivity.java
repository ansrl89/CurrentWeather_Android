package co.fronto.currentweather.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import co.fronto.currentweather.CityDataHandler;
import co.fronto.currentweather.R;
import co.fronto.currentweather.data.CityData;
import co.fronto.currentweather.data.Const;


/**
 * Created by MoonKi on 8/9/16.
 */
public class CitiesWeatherActivity extends AppCompatActivity
    implements View.OnClickListener, EditText.OnEditorActionListener
{
    // Handler
    private CityDataHandler mCityDataHandler = null;

    // UI variable
    private EditText mEtSearch = null;
    private RelativeLayout mLoadingPanel = null;
    private LinearLayout mLayoutResultText = null;

    private ListView mListview;
    private CityDataArrayAdapter mCityDataArrayAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Const.Log(" === CitiesWeatherActivity, onCreate() === ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cities_weather_activity);

        // set Ui variables
        setUI();

        // read data from SharedPreferences
        getmCityDataHandler();
        mCityDataHandler.readSavedData();

        // update UI
        updateUI();
    }

    private void setUI(){
        // init loading
        mLoadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);

        // init search bar
        findViewById(R.id.ivSearch).setOnClickListener(this);
        mEtSearch = (EditText) findViewById(R.id.etSearch);
        mEtSearch.setOnEditorActionListener(this);

        // init result text
        mLayoutResultText = (LinearLayout) findViewById(R.id.layoutResultText);

        // init list variables
        mListview = (ListView) findViewById(R.id.listView);
        mListview.setOnItemClickListener(mOnItemClickListener);
        mCityDataArrayAdapter = new CityDataArrayAdapter(this, R.layout.city_data_item);
        mListview.setAdapter(mCityDataArrayAdapter);
    }

    private void updateUI(){
        // get list from Handler, sharedPreferences
        LinkedList<CityData> cityDataList = mCityDataHandler.getmCityDataList();

        // clear, add and notify list updated
        mCityDataArrayAdapter.clear();
        for(int i=0; i<cityDataList.size(); i++){
            CityData cityData = cityDataList.get(i);
            mCityDataArrayAdapter.add(cityData);
        }
        mCityDataArrayAdapter.notifyDataSetChanged();

        // set Visibility for result text
        if(cityDataList.size() <= 0){
            mLayoutResultText.setVisibility(View.GONE);
        } else{
            mLayoutResultText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Control Loading panel
     * @param flag true: on Loading, false: Loading panel disappears
     */
    public void setLoading(final boolean flag){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (flag)
                    mLoadingPanel.setVisibility(View.VISIBLE);
                else
                    mLoadingPanel.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This toast function is for both UI thread and others
     * @param note
     */
    public void makeToast(final String note){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CitiesWeatherActivity.this, note, Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * After clicking search button, Keyboard should go
     */
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.ivSearch:
                searchWeather();
                hideKeyboard();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        searchWeather();
        hideKeyboard();
        return false;
    }

    public CityDataHandler getmCityDataHandler() {
        if(mCityDataHandler == null)
            mCityDataHandler = new CityDataHandler(this);
        return mCityDataHandler;
    }

    private void searchWeather(){
        String keyword = mEtSearch.getText().toString();

        if(keyword == null || keyword.length() <= 0)    // check empty, no action
            return;

        new WeatherAsyncTask().execute(keyword);
    }

    /**
     * AsyncTask for Http Client
     */
    private class WeatherAsyncTask extends AsyncTask<Object, Void, String>
    {
        private final int HTTP_TIMEOUT = 10000;
        private final String ENCODE_TYPE = "UTF-8";

        private final String NOT_FOUND = "404";
        private final String SUCCESS = "200";

        private final String KEY_CODE = "cod";
        private final String KEY_ERROR = "Error";

        @Override
        protected void onPreExecute() {
            Const.Log(" === CitiesWeatherActivity, onPreExecute() === ");
            super.onPreExecute();

            setLoading(true);
        }

        @Override
        protected String doInBackground(Object... params) {
            Const.Log(" === CitiesWeatherActivity, doInBackground() === ");
            HttpGet getRequest = null;
            // HttpEntity entity = null; // This Entity needs for Post request

            // init http client variable
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(HTTP_TIMEOUT)
                    .setConnectTimeout(HTTP_TIMEOUT)
                    .setConnectionRequestTimeout(HTTP_TIMEOUT)
                    .setStaleConnectionCheckEnabled(true)
                    .build();
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultRequestConfig(defaultRequestConfig)
                    .build();
            CloseableHttpResponse response = null;

            String responseStr = null;

            try {
                // make url
                String cityName = params[0].toString();
                String encodedCityName = URLEncoder.encode(cityName, ENCODE_TYPE);    // encoding keyword by UTF-8

                // make query
                List<BasicNameValuePair> queryList = new LinkedList<BasicNameValuePair>();
                queryList.add(new BasicNameValuePair(Const.WEBSERVICE_APPID_KEY, Const.WEBSERVICE_APPID));
                queryList.add(new BasicNameValuePair(Const.WEBSERVICE_QUERY_KEY, encodedCityName));

                String url = Const.WEBSERVICE_URL;
                url = (url+"?"+ URLEncodedUtils.format(queryList, ENCODE_TYPE)); // Finally, complete url

                getRequest = new HttpGet(url);  // send request
                response = httpclient.execute(getRequest); // receive response
                responseStr = EntityUtils.toString(response.getEntity(), ENCODE_TYPE);
            } catch (Exception e) {
                Const.LogE(e);
            }

            return responseStr;
        }

        @Override
        protected void onPostExecute(String response) {
            Const.Log(" === CitiesWeatherActivity, onPostExecute() === ");
            if(response == null){
                makeToast(getString(R.string.error_msg));   // exception handling: null response
            } else { // response exists
                JSONObject jsonResponse = (JSONObject) JSONValue.parse(response);
                if(jsonResponse == null){   // exception handling: null pointer - not JSON format
                    makeToast(getString(R.string.error_msg));
                } else {    // get JSON format
                    if (!jsonResponse.containsKey(KEY_CODE)) {  // exception handling: have no code
                        makeToast(getString(R.string.error_msg));
                    } else { // have response code
                        String result = jsonResponse.get(KEY_CODE).toString();
                        if(result.equals(NOT_FOUND)){   // exception handling: not found
                            if(!jsonResponse.containsKey(KEY_ERROR)){   // exception handling: have no error messege
                                makeToast(getString(R.string.error_msg));
                            } else {    // show the error messege
                                makeToast(jsonResponse.get(KEY_ERROR).toString());
                            }
                        } else if(result.equals(SUCCESS)) {    // success case
                            getmCityDataHandler();
                            if(mCityDataHandler.add(jsonResponse)){ // if item is added, update ui
                                updateUI();
                            } else{ // fail to add new data
                                makeToast(getString(R.string.error_msg));
                            }
                        } // close: success case
                        else { // exception handling wrong response code
                            makeToast(getString(R.string.error_msg));
                        }
                    } // close: have response code
                } // close: get JSON format
            } // close: response exists

            setLoading(false);
        }
    }

    /**
     * Click Listner for Listview
     */
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // get the item clicked
            CityData cityData = (CityData) parent.getItemAtPosition(position);

            // put lat and lon in the intent to start CityLocationActivity
            Intent intent = new Intent(getApplicationContext(), CityLocationActivity.class);
            intent.putExtra(Const.KEY_LAT, cityData.lat);
            intent.putExtra(Const.KEY_LON, cityData.lon);
            intent.putExtra(Const.KEY_CITYNAME, cityData.cityName);
            startActivity(intent);
        }
    };

    /**
     * ArrayAdapter for Listview
     */
    public class CityDataArrayAdapter extends ArrayAdapter<CityData> {
        public CityDataArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                holder = new Holder();
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.city_data_item, parent, false);
                convertView.setTag(holder);

                holder.cityName = (TextView) convertView.findViewById(R.id.tvName);
                holder.temperature = (TextView) convertView.findViewById(R.id.tvTemp);
                holder.description = (TextView) convertView.findViewById(R.id.tvDes);
            } else {
                holder = (Holder) convertView.getTag();
            }

            // set list values
            CityData cityData = getItem(position);
            holder.cityName.setText(cityData.cityName);
            holder.temperature.setText(cityData.temperature + getString(R.string.fahrenheit));
            holder.description.setText(cityData.description);

            return convertView;
        }
    }

    /**
     * Holder for List Adapter
     */
    public static class Holder{
        public TextView cityName;
        public TextView temperature;
        public TextView description;
    }

}
