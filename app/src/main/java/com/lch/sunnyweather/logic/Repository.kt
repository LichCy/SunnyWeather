package com.lch.sunnyweather.logic

import androidx.lifecycle.liveData
import com.lch.sunnyweather.logic.model.Place
import com.lch.sunnyweather.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import okhttp3.Dispatcher
import java.lang.Exception
import java.lang.RuntimeException

object Repository {
    // 一般在仓库层中定义的方法，为了能将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个LiveData对象
    // 设置为Dispatchers.IO代码块中的代码都运行在子线程中了，Android的数据读写工作是不建议在主线程中的，所以在仓库层做一次线程切换是非常有必要的
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                Result.success(placeResponse.places)
            }else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        }catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        // 用emit将包装好的发射出去，类似于LiveData的setValue方法，但是我们这里无法直接获取返回的LiveData对象，所以用这个替代方法
        emit(result)
    }
}