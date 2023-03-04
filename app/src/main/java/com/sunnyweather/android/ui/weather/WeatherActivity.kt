package com.sunnyweather.android.ui.weather


import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.databinding.ForecastBinding
import com.sunnyweather.android.databinding.ForecastItemBinding
import com.sunnyweather.android.databinding.LifeIndexBinding
import com.sunnyweather.android.databinding.NowBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    private val nowBinding = NowBinding.inflate(layoutInflater)
    private val forecastBinding = ForecastBinding.inflate(layoutInflater)
    private val forecastItemBinding = ForecastItemBinding.inflate(layoutInflater)
    private val lifeIndexBinding = LifeIndexBinding.inflate(layoutInflater)
    val activityWeatherBinding = ActivityWeatherBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_weather)
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this, Observer {
            val weather = it.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                it.exceptionOrNull()?.printStackTrace()
            }
            activityWeatherBinding.swipeRefresh.isRefreshing = false
        })
        activityWeatherBinding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        refreshWeather()
        activityWeatherBinding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        // 监听切换城市按钮的点击事件
        nowBinding.navBtn.setOnClickListener {
            // 打开滑动菜单
            activityWeatherBinding.drawerLayout.openDrawer(GravityCompat.START)
        }
        // 监听 DrawerLayout 的状态
        activityWeatherBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {
                TODO("Not yet implemented")
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                TODO("Not yet implemented")
            }

            override fun onDrawerOpened(drawerView: View) {
                TODO("Not yet implemented")
            }

            /**
             * 当滑动菜单被隐藏的时候
             * 输入法也要随之隐藏
             */
            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })
    }

    /**
     * 刷新天气信息
     */
    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        activityWeatherBinding.swipeRefresh.isRefreshing = true
    }

    private fun showWeatherInfo(weather: Weather) {
        nowBinding.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        // 填充 now.xml 布局中的数据
        nowBinding.apply {
            currentTemp.text = "${realtime.temperature.toInt()} ℃"
            currentSky.text = getSky(realtime.skycon).info
            currentAQI.text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
            nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        }
        // 填充 forecast.xml 布局中的数据
        forecastBinding.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                forecastBinding.forecastLayout, false)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sky = getSky(skycon.value)
            forecastItemBinding.apply {
                dateInfo.text = simpleDateFormat.format(skycon.date)
                skyIcon.setImageResource(sky.icon)
                skyInfo.text = sky.info
                temperatureInfo.text =
                    "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            }
            forecastBinding.forecastLayout.addView(view)
        }
        // 填充 life_index.xml 布局中的数据
        val lifeIndex = daily.lifeIndex
        lifeIndexBinding.apply {
            coldRiskText.text = lifeIndex.coldRisk[0].desc
            dressingText.text = lifeIndex.dressing[0].desc
            ultravioletText.text = lifeIndex.ultraviolet[0].desc
            carWashingText.text = lifeIndex.carWashing[0].desc
        }
        activityWeatherBinding.weatherLayout.visibility = View.VISIBLE
    }
}