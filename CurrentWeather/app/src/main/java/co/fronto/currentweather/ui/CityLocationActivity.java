package co.fronto.currentweather.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import co.fronto.currentweather.R;
import co.fronto.currentweather.data.Const;

/**
 * Created by MoonKi on 8/9/16.
 */
public class CityLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings mUiSettings;

    private float mLat, mLon;
    private String mCityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Const.Log(" === CityLocationActivity, onCreate() === ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_location);

        // get datas from intent
        Intent intent = getIntent();
        mLat = intent.getFloatExtra(Const.KEY_LAT, 0);
        mLon = intent.getFloatExtra(Const.KEY_LON, 0);
        mCityName = intent.getStringExtra(Const.KEY_CITYNAME);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Const.Log(" === CityLocationActivity, onMapReady() === ");

        mMap = googleMap;

        // Add marker including the title
        LatLng cityPos = new LatLng(mLat, mLon);
        mMap.addMarker(new MarkerOptions().position(cityPos).title(mCityName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityPos, 8));

        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
    }
}
