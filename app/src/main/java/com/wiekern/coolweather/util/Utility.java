package com.wiekern.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.wiekern.coolweather.db.CoolWeatherDB;
import com.wiekern.coolweather.model.City;
import com.wiekern.coolweather.model.County;
import com.wiekern.coolweather.model.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by yafei on 9/8/16.
 */

public class Utility {
    public synchronized static boolean handleProvincesResponse(
            CoolWeatherDB coolWeatherDB,
            String response) {
        if (!TextUtils.isEmpty(response)) {
            Map<String, String> map = new HashMap<String, String>();
            String provinceCode, provinceName;

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("city_info");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    String id = subJsonObject.getString("id");
                    provinceCode = id.substring(5, 7);

                    if ("01".equals(provinceCode)) {
                        provinceName = "北京";
                    } else if ("02".equals(provinceCode)) {
                        provinceName = "上海";
                    } else if ("03".equals(provinceCode)) {
                        provinceName = "天津";
                    } else if ("04".equals(provinceCode)) {
                        provinceName = "重庆";
                    } else if ("32".equals(provinceCode)) {
                        provinceName = "香港";
                    } else if ("33".equals(provinceCode)) {
                        provinceName = "澳门";
                    } else {
                        provinceName = subJsonObject.getString("prov");
                    }

                    map.put(provinceCode, provinceName);
                }
            } catch (JSONException e) {
                Log.e("Utility", "handle response failed.");
            }

            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Province province = new Province();
                Map.Entry<String, String> entry = iterator.next();
                province.setProvinceCode("CN101" + entry.getKey());
                province.setProvinceName(entry.getValue());
                coolWeatherDB.saveProvince(province);
            }
            return true;
        }
        return false;
    }

    public synchronized static boolean handleCitiesResponse(
            CoolWeatherDB coolWeatherDB,
            String response, int provinceId, String provinceCode) {
        if (!TextUtils.isEmpty(response)) {
            Log.d("ChooseAreaActivity", "city response=" + response);
            Map<String, String> map = new HashMap<String, String>();
            String cityCode, cityName, capitalCityCode;
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("city_info");
                String oldId;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    String id = subJsonObject.getString("id");

                    cityCode = id.substring(7, 9);
                    capitalCityCode = id.substring(9, 11);

                    if (id.startsWith(provinceCode)) {
                        if (id.startsWith("CN10101")) {
                            cityName = "北京";
                            map.put(cityCode, cityName);
                        } else if (id.startsWith("CN10102")) {
                            cityName = "上海";
                            map.put(cityCode, cityName);
                        } else if (id.startsWith("CN10103")) {
                            cityName = "天津";
                            map.put(cityCode, cityName);
                        } else if (id.startsWith("CN10104")) {
                            cityName = "重庆";
                            map.put(cityCode, cityName);
                        } else if ("01".equals(capitalCityCode)) {
                            cityName = subJsonObject.getString("city");
                            map.put(cityCode, cityName);
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                City city = new City();
                city.setCityName(entry.getValue());
                city.setCityCode(provinceCode + entry.getKey());
                city.setProvinceId(provinceId);
                coolWeatherDB.saveCity(city);
            }

            return true;
        }

        return false;
    }

    public synchronized static boolean handleCountiesResponse(
            CoolWeatherDB coolWeatherDB,
            String response, int cityId, String cityCode) {
        if (!TextUtils.isEmpty(response)) {
            Map<String, String> map = new HashMap<String, String>();
            String countyName, countyCode;

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("city_info");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    String id = subJsonObject.getString("id");
                    if (id.startsWith(cityCode)) {
                        countyCode = id;
                        countyName = subJsonObject.getString("city");
                        map.put(countyCode, countyName);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                County county = new County();
                county.setCityId(cityId);
                county.setCountyName(entry.getValue());
                county.setCountyCode(entry.getKey());
                coolWeatherDB.saveCounty(county);
            }

            return  true;
        }

        return false;
    }

    public static void handleWeatherResponse(Context context, String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather data service 3.0");
            JSONObject weatherAllInfo = jsonArray.getJSONObject(0);
            JSONObject weatherBaseInfo = weatherAllInfo.getJSONObject("basic");
            String cityName = weatherBaseInfo.getString("city");
            JSONObject weatherUpdateTime = weatherBaseInfo.getJSONObject("update");
            String updateTime = weatherUpdateTime.getString("loc");
            String weatherCode = weatherBaseInfo.getString("id");
            JSONObject weatherNowInfo = weatherAllInfo.getJSONObject("now");
            String currentTemp = weatherNowInfo.getString("tmp");
            JSONObject weatherNowCond = weatherNowInfo.getJSONObject("cond");
            String currentCond = weatherNowCond.getString("txt");
            JSONArray weatherDailyForecast = weatherAllInfo.getJSONArray("daily_forecast");
            JSONObject weatherCurrentDayInfo = weatherDailyForecast.getJSONObject(0);
            String currentDate = weatherCurrentDayInfo.getString("date");
            JSONObject weatherTempInfo = weatherCurrentDayInfo.getJSONObject("tmp");
            String minTemp = weatherTempInfo.getString("min");
            String maxTemp = weatherTempInfo.getString("max");
            Log.d("ChooseAreaActivity", cityName + " " + currentTemp);
            saveWeatherInfo(context, cityName, weatherCode, currentTemp, currentCond, currentDate, updateTime, minTemp, maxTemp);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void saveWeatherInfo(Context context, String cityName, String weatherCode,
                                       String currentTemp, String currentCond,
                                       String currentDate, String updateTime,
                                       String minTemp, String maxTemp) {
        //SimpleDateFormat sfd = new SimpleDateFormat("yyyy 年 M 月 d 日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("current_temp", currentTemp);
        editor.putString("current_cond", currentCond);
        editor.putString("current_date", currentDate);
        editor.putString("update_time", updateTime);
        editor.putString("min_temp", minTemp);
        editor.putString("max_temp", maxTemp);
        editor.commit();
    }
}
