package com.coolweather.android.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.Bean.Weather;
import com.coolweather.android.R;
import com.coolweather.android.Util.HttpUtil;
import com.coolweather.android.Util.JsonParse;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ImageView iv_background;
    private ScrollView scrollView;
    private TextView tv_title;
    private TextView tv_updatetime;
    private TextView tv_temperature;
    private TextView tv_weather;
    private LinearLayout ll_forecast;
    private TextView tv_aqi;
    private TextView tv_pm25;
    private TextView tv_comfort;
    private TextView tv_wash;
    private TextView tv_sport;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        initView();

        initData();

    }

    /**
     * 初始化界面
     */
    private void initView() {
        iv_background = (ImageView) this.findViewById(R.id.iv_backgroung);
        scrollView = (ScrollView) this.findViewById(R.id.scrollView);
        tv_title = (TextView) this.findViewById(R.id.tv_title);
        tv_updatetime = (TextView) this.findViewById(R.id.tv_updatetime);
        tv_temperature = (TextView) this.findViewById(R.id.tv_temperature);
        tv_weather = (TextView) this.findViewById(R.id.tv_weather);
        ll_forecast = (LinearLayout) this.findViewById(R.id.ll_forecast);
        tv_aqi = (TextView) this.findViewById(R.id.tv_aqi);
        tv_pm25 = (TextView) this.findViewById(R.id.tv_pm25);
        tv_comfort = (TextView) this.findViewById(R.id.tv_comfort);
        tv_wash = (TextView) this.findViewById(R.id.tv_wash);
        tv_sport = (TextView) this.findViewById(R.id.tv_sport);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String weatherText = sp.getString("weather", null);
        String bingPic = sp.getString("bingPic", null);
        if (weatherText != null) {
            // 有缓存时直接解析天气数据
            Weather weather = JsonParse.handleWeatherResponse(weatherText);
            showWeather(weather);
        } else {
            // 无缓存时去服务器请求天气数据
            scrollView.setVisibility(View.INVISIBLE); // 没有数据时先隐藏布局
            Intent intent = getIntent();
            String weatherId = intent.getStringExtra("weatherId");
            requestWeather(weatherId);
        }
        if (bingPic != null) {
            Glide.with(WeatherActivity.this)
                    .load(bingPic)
                    .into(iv_background);
        } else {
            loadBingPic();
        }
    }

    /**
     * 根据weatherId获取请求天气信息
     *
     * @param weatherId
     */
    private void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=e8660117e7094167b2394467c528eaf2";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = JsonParse.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && weather.getStatus().equals("ok")) {
                            // 请求数据成功，把数据缓存到SharedPreferences中
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeather(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        loadBingPic(); // 每次请求天气也要重新加载背景图片
    }

    /**
     * 把天气数据显示到相应控件上
     *
     * @param weather 天气对象
     */
    private void showWeather(Weather weather) {
        tv_title.setText(weather.getBasic().getCity());
        tv_updatetime.setText(weather.getBasic().getUpdate().getLoc().split(" ")[1]);
        tv_temperature.setText(weather.getNow().getTmp() + "℃");
        tv_weather.setText(weather.getNow().getCond().getTxt());
        ll_forecast.removeAllViews();
        for (Weather.DailyForecastBean forecast : weather.getDaily_forecast()) {
            View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.item_forecast, ll_forecast, false);
            TextView tv_date = (TextView) view.findViewById(R.id.tv_date);
            tv_date.setText(forecast.getDate());
            TextView tv_weather = (TextView) view.findViewById(R.id.tv_weather);
            tv_weather.setText(forecast.getCond().getTxt_d());
            TextView tv_max = (TextView) view.findViewById(R.id.tv_max);
            tv_max.setText(forecast.getTmp().getMax());
            TextView tv_min = (TextView) view.findViewById(R.id.tv_min);
            tv_min.setText(forecast.getTmp().getMin());
            ll_forecast.addView(view);
        }
        if (weather.getAqi() != null) {
            tv_aqi.setText(weather.getAqi().getCity().getAqi());
            tv_pm25.setText(weather.getAqi().getCity().getPm25());
        }
        tv_comfort.setText("舒适度：" + weather.getSuggestion().getComf().getTxt());
        tv_wash.setText("洗车指数：" + weather.getSuggestion().getCw().getTxt());
        tv_sport.setText("运动建议" + weather.getSuggestion().getSport().getTxt());

        scrollView.setVisibility(View.VISIBLE); // 让天气布局可见
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String bingUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(bingUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bingPic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this)
                                .load(bingPic)
                                .into(iv_background);
                    }
                });
            }
        });
    }

}
