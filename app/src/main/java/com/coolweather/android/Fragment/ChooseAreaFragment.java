package com.coolweather.android.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.Activity.MainActivity;
import com.coolweather.android.Activity.WeatherActivity;
import com.coolweather.android.Bean.City;
import com.coolweather.android.Bean.Country;
import com.coolweather.android.Bean.Province;
import com.coolweather.android.Bean.Weather;
import com.coolweather.android.R;
import com.coolweather.android.Util.Common;
import com.coolweather.android.Util.HttpUtil;
import com.coolweather.android.Util.JsonParse;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 选择地区Fragment
 */

public class ChooseAreaFragment extends Fragment {

    private TextView tv_title; // 标题
    private Button bt_back; // 返回
    private ListView listView; // 省市县ListView
    private ProgressDialog progressDialog; // 请求数据加载进度条
    private ArrayAdapter<String> adapter; // ListView适配器
    private List<String> data = new ArrayList<>(); // ListView数据源
    private List<Province> provinces; // 省列表
    private List<City> cities; // 市列表
    private List<Country> countries; // 县列表
    private Province selectedProvince; // 选中的省
    private City selectedCity; // 选中的市
    private Country selectedCountry; // 选中的县
    private int currentLevel; // 当前选中的级别

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        bt_back = (Button) view.findViewById(R.id.bt_back);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProvinces(); // 初始化，加载省级数据
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回上一级
                if (currentLevel == Common.LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == Common.LEVEL_COUNTRY) {
                    queryCities();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == Common.LEVEL_PROVINCE) {
                    selectedProvince = provinces.get(position);
                    queryCities();
                } else if (currentLevel == Common.LEVEL_CITY) {
                    selectedCity = cities.get(position);
                    queryCountries();
                } else if (currentLevel == Common.LEVEL_COUNTRY) {
                    selectedCountry = countries.get(position);
                    String weatherId = selectedCountry.getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        // 如果Fragmrnt在MainActivity中，则跳转到WeatherActivity
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weatherId", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        // 如果Fragmrnt在WeatherActivity中，则不需要跳转，关闭滑动菜单，请求新地点的天气
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.refreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
    }

    /**
     * 查询全国所有的省
     * 优先从数据库查询，如果查询不到再从服务器查询
     */
    private void queryProvinces() {
        tv_title.setText("中国");
        bt_back.setVisibility(View.GONE); // 隐藏返回按钮
        provinces = DataSupport.findAll(Province.class);
        if (provinces.size() > 0) {
            data.clear();
            for (Province province : provinces) {
                data.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = Common.LEVEL_PROVINCE;
        } else {
            // 如果数据库查询不到则从服务器查询
            String address = "http:/guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省所有的市
     * 优先从数据库查询，如果查询不到再从服务器查询
     */
    private void queryCities() {
        tv_title.setText(selectedProvince.getProvinceName());
        bt_back.setVisibility(View.VISIBLE); // 显示返回按钮
        cities = DataSupport.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cities.size() > 0) {
            data.clear();
            for (City city : cities) {
                data.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = Common.LEVEL_CITY;
        } else {
            // 如果数据库查询不到则从服务器查询
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http:/guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市所有的县
     * 优先从数据库查询，如果查询不到再从服务器查询
     */
    private void queryCountries() {
        tv_title.setText(selectedCity.getCityName());
        bt_back.setVisibility(View.VISIBLE); // 显示返回按钮
        countries = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(Country.class);
        if (countries.size() > 0) {
            data.clear();
            for (Country country : countries) {
                data.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = Common.LEVEL_COUNTRY;
        } else {
            // 如果数据库查询不到则从服务器查询
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http:/guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "country");
        }
    }

    /**
     * 从服务器请求数据
     *
     * @param address 请求地址
     * @param type    请求数据类型
     */
    private void queryFromServer(String address, final String type) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage("正在加载...");
                    progressDialog.setCanceledOnTouchOutside(false);
                }
                progressDialog.show();
                String json = response.body().string();
                Boolean result = false; // 标识解析是否成功
                // 判断是请求的是哪个类型的JSON数据，并调用相应解析方法
                if (type.equals("province")) {
                    result = JsonParse.handleProvinceResponse(json);
                } else if (type.equals("city")) {
                    result = JsonParse.handleCityResponse(json, selectedProvince.getId());
                } else if (type.equals("country")) {
                    result = JsonParse.handleCountryResponse(json, selectedCity.getId());
                }
                // 如果result为true，说明已经解析完成JSON数据并写入数据库中，则再次查询数据库，获取省市县列表
                if (result) {
                    // 回到主线程处理逻辑
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            if (type.equals("province")) {
                                queryProvinces();
                            } else if (type.equals("city")) {
                                queryCities();
                            } else if (type.equals("country")) {
                                queryCountries();
                            }
                        }
                    });
                } else {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            }
        });
    }

}
