package com.lch.sunnyweather.logic.model

import com.google.gson.annotations.SerializedName

data class PlaceResponse(val status: String, val places: List<Place>)

//由于json中一些字段的命名与Kotlin中命名规范不太一致，因此这里使用了SerializedName来建立映射关系
data class Place(val name: String, val location: Location,
                 @SerializedName("formatted_address") val address: String)

data class Location(val lng: String, val lat: String)