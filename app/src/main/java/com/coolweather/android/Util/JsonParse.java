package com.coolweather.android.Util;

import android.text.TextUtils;

import com.coolweather.android.Bean.City;
import com.coolweather.android.Bean.Country;
import com.coolweather.android.Bean.Province;
import com.coolweather.android.Bean.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JSON数据解析
 */

public class JsonParse {
    /**
     * 解析和处理服务器返回的省级数据
     *
     * @param response 服务器返回的Json字符串
     * @return
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray provinces = new JSONArray(response);
                for (int i = 0; i < provinces.length(); i++) {
                    JSONObject provinceObject = provinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     *
     * @param response   服务器返回的Json字符串
     * @param provinceId 市所属的省id
     * @return
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray cities = new JSONArray(response);
                for (int i = 0; i < cities.length(); i++) {
                    JSONObject cityObject = cities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     *
     * @param response 服务器返回的Json字符串
     * @param cityId   县所属的id
     * @return
     */
    public static boolean handleCountryResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray countries = new JSONArray(response);
                for (int i = 0; i < countries.length(); i++) {
                    JSONObject countyrObject = countries.getJSONObject(i);
                    Country country = new Country();
                    country.setCountryName(countyrObject.getString("name"));
                    country.setWeatherId(countyrObject.getString("weather_id"));
                    country.setCityId(cityId);
                    country.save();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather对象
     *
     * @param response 服务器返回的Json字符串
     * @return
     */
    public static Weather handleWeatherResponse(String response) {
        Gson gson = new Gson();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            Weather weather = gson.fromJson(jsonArray.getJSONObject(0).toString(), Weather.class);
            return weather;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
