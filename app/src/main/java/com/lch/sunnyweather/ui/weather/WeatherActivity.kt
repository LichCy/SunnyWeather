package com.lch.sunnyweather.ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.lch.sunnyweather.R
import com.lch.sunnyweather.logic.model.Weather
import com.sunnyweather.android.logic.model.Sky
import com.sunnyweather.android.logic.model.getSky
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.forecast.*
import kotlinx.android.synthetic.main.life_index.*
import kotlinx.android.synthetic.main.now.*
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProviders.of(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置状态栏为透明并将布局显示在状态栏上面
        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_weather)

        // 在placeAdapter中传入intent中的数据
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        // 设置观察
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                // 调用下面的方法来渲染结果
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }

        })
        // 修改值触发观察
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
    }


    private fun showWeatherInfo(weather: Weather) {
        placeName.text = viewModel.placeName
        // 先拿出weather的两个主体
        val realtime = weather.realtime
        val daily = weather.daily

        // 填充now.xml的布局
        currentTemp.text = "${realtime.temperature.toInt()} ℃"
        currentSky.text = getSky(realtime.skycon).info
        currentAQI.text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)


        // 填充forecast.xml布局中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val temperature = daily.temperature[i]
            val skycon = daily.skycon[i]
            // 获取当前布局
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            dateInfo.text = simpleDateFormat.format(skycon.date)
            skyIcon.setImageResource(getSky(skycon.value).icon)
            skyInfo.text = getSky(skycon.value).info
            temperatureInfo.text = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"

            // 重新加载布局
            forecastLayout.addView(view)
        }
        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout.visibility = View.VISIBLE
    }
}


