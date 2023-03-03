package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

object PlaceDao {

    /**
     * 通过 GSON 将 Place 对象转换成一个 JSON 字符串
     * 并将字符串数据存储到 SharedPreferences 文件中
     */
    fun savePlace(place: Place) {
        sharedPreferences().edit {
            putString("place", Gson().toJson(place))
        }
    }

    /**
     * 先将 JSON 字符串从 SharedPreferences 文件中读取出来
     * 再通过 GSON 将 JSON 字符串解析成 Place 对象并返回
     */
    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    /**
     * 判断数据是否已被存储
     */
    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() = SunnyWeatherApplication.context.
    getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)
}