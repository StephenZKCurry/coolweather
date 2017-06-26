package com.coolweather.android.Bean;

import org.litepal.crud.DataSupport;

/**
 * 市实体类
 */
public class City extends DataSupport {
    private int id;
    private String cityName; // 市名称
    private int cityCode; // 市代号
    private int provinceId; // 市所属省的id

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
