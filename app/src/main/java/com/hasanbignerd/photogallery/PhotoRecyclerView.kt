package com.hasanbignerd.photogallery

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hasanbignerd.photogallery.databinding.PhotoGalleryItemBinding
import com.hasanbignerd.photogallery.model.GalleryItem

private const val TAG = "RECYCLERTTT"

class PhotoRecyclerView(var photos:List<GalleryItem>, val thumbnailDownloader: ThumbnailDownloader<PhotoViewHolder>,val itemClick: (GalleryItem) -> Unit):RecyclerView.Adapter<PhotoRecyclerView.PhotoViewHolder>() {


    inner class PhotoViewHolder(private val binding:PhotoGalleryItemBinding):RecyclerView.ViewHolder(binding.root){

        lateinit var  bindDrawable: (Drawable) -> Unit

        fun onBind(galleryItem: GalleryItem){
            Log.d(TAG, "onBind: is on")
            val placeHolder : Drawable = ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.ic_launcher_foreground,
            )?: ColorDrawable()

            thumbnailDownloader.queueThumbnail(this,galleryItem.url)

            bindDrawable = binding.imageView::setImageDrawable
            bindDrawable(placeHolder)

            binding.root.setOnClickListener {
                itemClick(photos[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(PhotoGalleryItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.onBind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

}