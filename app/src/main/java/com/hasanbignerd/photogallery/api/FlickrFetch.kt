package com.hasanbignerd.photogallery.api

import android.content.Context
import android.content.SyncRequest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.hasanbignerd.photogallery.model.GalleryItem
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

const val TAG = "FlickrFetch"

//this serves as a very basic repository

class FlickrFetch(context: Context) {

    private val flickrApi:FlickrApi
    private lateinit var flickrRequest: Call<FlickrResponse>

    init {
        // OkHttp client
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                ChuckerInterceptor.Builder(context)
                    .collector(ChuckerCollector(context))
                    .maxContentLength(250000L)
                    .redactHeaders(emptySet())
                    .alwaysReadResponseBody(true)
                    .build()
            )
            .addInterceptor(PhotoInterceptor())
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        flickrApi =  retrofit.create(FlickrApi::class.java)
    }

    //i think we delegate this logic to worker class

//    fun fetchPhotos(): LiveData<List<GalleryItem>> {
//        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
//        //this flickRequest serves as web request and return Call object
//        flickrRequest = flickrApi.fetchPhotos()
//        flickrRequest.enqueue(object : Callback<FlickrResponse> {
//            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
//                Log.e(TAG, "Failed to fetch photos", t)
//            }
//            override fun onResponse(
//                call: Call<FlickrResponse>,
//                response: Response<FlickrResponse>
//            ) {
//                Log.d(TAG, "Response received  -   ${response.body()}")
//                val flickrResponse = response.body()
//                val photoResponse = flickrResponse?.photos
//                var galleryItems = photoResponse?.galleryItems ?: mutableListOf()
//                galleryItems = galleryItems.filterNot {
//                    it.url.isBlank()
//                }
//                responseLiveData.value = galleryItems
//            }
//        })
//        return responseLiveData
//    }

    fun fetchPhotosRequest(): Call<FlickrResponse> {
        return flickrApi.fetchPhotos()
    }
    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        //return fetchPhotoMetadata(flickrApi.fetchPhotos())
        return fetchPhotoMetadata(fetchPhotosRequest())
    }
    fun searchPhotosRequest(query: String): Call<FlickrResponse> {
        return flickrApi.searchPhotos(query)
    }
    fun searchPhotos(query: String): LiveData<List<GalleryItem>> {
//        return fetchPhotoMetadata(flickrApi.searchPhotos(query))
        return fetchPhotoMetadata(searchPhotosRequest(query))
    }

    fun cancelRequestInFlight() {
        //can't get this , think about it later
        //if (::someCall.isInitialized) {
            flickrRequest.cancel()
        //}
    }

//    fun searchPhotos(query: String): LiveData<List<GalleryItem>> {
//        return fetchPhotoMetadata(flickrApi.searchPhotos(query))
//    }

    private fun fetchPhotoMetadata(flickrRequest:Call<FlickrResponse>):LiveData<List<GalleryItem>>{
        val responseLiveData : MutableLiveData<List<GalleryItem>> = MutableLiveData()
        flickrRequest.enqueue(object :Callback<FlickrResponse>{
            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {
                responseLiveData.value = response.body()?.photos?.galleryItems
            }

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                //Not yet implemented
            }

        })
        return responseLiveData
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
    
}