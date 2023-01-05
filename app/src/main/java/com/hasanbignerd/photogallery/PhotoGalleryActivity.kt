package com.hasanbignerd.photogallery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hasanbignerd.photogallery.databinding.ActivityMainBinding
import com.hasanbignerd.photogallery.databinding.ActivityPhotoGalleryBinding

class PhotoGalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    companion object{
        fun newIntent(context: Context):Intent{
            return Intent(context,PhotoGalleryActivity::class.java)
        }
    }

}