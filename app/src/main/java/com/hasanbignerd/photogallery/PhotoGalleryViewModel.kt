package com.hasanbignerd.photogallery

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.hasanbignerd.photogallery.api.FlickrFetch
import com.hasanbignerd.photogallery.model.GalleryItem
import com.hasanbignerd.photogallery.utilities.QueryPreferences

private const val TAG = "VIEW_MODEL"

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {

    private val flickrRepository = FlickrFetch(app)
    val galleryItemLiveData: LiveData<List<GalleryItem>>

    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)

//        galleryItemLiveData = flickrRepository.fetchPhotos()
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isBlank()||searchTerm==""){
                Log.d(TAG, "ViewModel: fetching photos")
                flickrRepository.fetchPhotos()
            }else{
                flickrRepository.searchPhotos(searchTerm)
            }
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app,query)
        mutableSearchTerm.value = query
    }

    //use this to cancel in-flick web request to avoid memory leak or handle this request in DB
    override fun onCleared() {
        super.onCleared()
        flickrRepository.cancelRequestInFlight()
    }

}