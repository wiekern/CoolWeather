package com.wiekern.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wiekern.coolweather.R;
import com.wiekern.coolweather.service.AutoUpdateService;
import com.wiekern.coolweather.util.HttpCallbackListener;
import com.wiekern.coolweather.util.HttpUtil;
import com.wiekern.coolweather.util.Utility;

/**
 * Created by yafei on 9/8/16.
 */

public class WeatherActivity extends Activity implements View.OnClickListener {

    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;
    private TextView publishText;
    private TextView currentTempText;
    private TextView currentCondText;
    private TextView minTempText;
    private TextView maxTempText;
    private TextView currentDateText;
    private Button switchCity;
    private Button refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        currentTempText = (TextView) findViewById(R.id.current_temp);
        currentCondText = (TextView) findViewById(R.id.weather_desp);
        minTempText = (TextView) findViewById(R.id.temp1);
        maxTempText = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            publishText.setText("Syncing...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            showWeather();
        }


    }

    private void queryWeatherCode(String countyCode) {
        String address = "https://api.heweather.com/x3/weather?cityid="
                + countyCode + "&key=a968bef20f9c42e4a5a7d8c249c65086";
        //Log.d("ChooseAreaActivity", "address=" + address);
        queryFromServer(address);
    }

    private void queryFromServer(final String address) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(WeatherActivity.this, response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeather();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("Sync failed");
                    }
                });
            }
        });

    }

    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("ChooseAreaActivity", "showWeather. city_name=" + prefs.getString("city_name", ""));
        cityNameText.setText(prefs.getString("city_name", ""));
        publishText.setText("今日 " + prefs.getString("update_time", "") + " 发布");
        currentTempText.setText(prefs.getString("current_temp", "") + "\u2103");
        currentDateText.setText(prefs.getString("current_date", ""));
        currentCondText.setText(prefs.getString("current_cond", ""));
        minTempText.setText(prefs.getString("min_temp", "") + "\u2103");
        maxTempText.setText(prefs.getString("max_temp", "") + "\u2103");
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("Syncing...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String countyCode = prefs.getString("weatherCode", "");
                queryWeatherCode(countyCode);
                break;
            default:
                break;
        }
    }
}
