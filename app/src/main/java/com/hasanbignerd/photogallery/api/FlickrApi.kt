package com.hasanbignerd.photogallery.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrApi {

    //you no longer need this as long as you provide interceptor which abstract the same share pair-values
//    @GET(
//        "services/rest/?method=flickr.interestingness.getList" +
//                "&api_key=0efd375d183d8b4ef20dce91e7200b60" +
//                "&format=json" +
//                "&nojsoncallback=1" +
//                "&extras=url_s"
//    )

    @GET("services/rest?method=flickr.interestingness.getList")
    fun fetchPhotos(): Call<FlickrResponse>

    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>

    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<FlickrResponse>

}