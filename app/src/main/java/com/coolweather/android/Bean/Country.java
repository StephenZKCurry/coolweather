package com.coolweather.android.Bean;

/**
 * 县实体类
 */

public class Country {
    private int id;
    private String countryName; // 县名称
    private int weatherId; // 县对应的天气id
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

    public int getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(int weatherId) {
        this.weatherId = weatherId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}