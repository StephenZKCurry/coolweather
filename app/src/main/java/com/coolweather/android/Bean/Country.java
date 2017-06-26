package com.coolweather.android.Bean;

import org.litepal.crud.DataSupport;

/**
 * 县实体类
 */

public class Country extends DataSupport {
    private int id;
    private String countryName; // 县名称
    private String weatherId; // 县对应的天气id
    private int cityId; // 县所属的市id

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}