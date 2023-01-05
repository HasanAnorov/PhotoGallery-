package com.hasanbignerd.photogallery

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

//Don't pass context into ViewModels
//this will cause Memory Leak
//Instead, use
// 1.Dependency injection (manually or with frameworks)
// 2.Instead of passing the context to ViewModel, pass in the dependency itself. ex: https://iamgideon.medium.com/stop-passing-context-into-viewmodels-bb11b3f432fb

class PhotoGalleryViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PhotoGalleryViewModel::class.java))
                return PhotoGalleryViewModel(app) as T
            throw IllegalArgumentException("Unknown ViewModel")
        }
    }