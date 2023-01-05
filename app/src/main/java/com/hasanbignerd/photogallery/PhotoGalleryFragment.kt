package com.hasanbignerd.photogallery

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.hasanbignerd.photogallery.databinding.FragmentPhotoGalleryBinding
import com.hasanbignerd.photogallery.utilities.QueryPreferences
import java.util.concurrent.TimeUnit
import kotlin.math.log

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment : VisibleFragment() {

    private var _binding : FragmentPhotoGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PhotoGalleryViewModel
    private lateinit var viewModelFactory: PhotoGalleryViewModelFactory
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoRecyclerView.PhotoViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: is On")

//        //this is how we create One Time Request
//        val constraints = Constraints
//            .Builder()
//            .setRequiredNetworkType(NetworkType.UNMETERED)
//            .build()
//        val workRequest = OneTimeWorkRequest
//            .Builder(PollWorker::class.java)
//            .setConstraints(constraints)
//            .build()
//        WorkManager.getInstance()
//            .enqueue(workRequest)

        val responseHandler = Handler()

        thumbnailDownloader = ThumbnailDownloader(requireContext(),responseHandler){photoHolder, bitmap ->
            Log.d(TAG, "onCreate: thumb")
            val drawable = BitmapDrawable(resources,bitmap)
            photoHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoGalleryBinding.inflate(inflater,container,false)
        Log.d(TAG, "onCreateView: is on")

        viewLifecycleOwner.lifecycle.addObserver(
            thumbnailDownloader.viewLifecycleObserver
        )

        retainInstance = true
        setHasOptionsMenu(true)

        viewModelFactory = PhotoGalleryViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this,viewModelFactory)[PhotoGalleryViewModel::class.java]

        Log.d(TAG, "onCreateView: ${viewModel.galleryItemLiveData.value}")
        viewModel.galleryItemLiveData.observe(viewLifecycleOwner){galleryItems ->
            Log.d(TAG, "onCreateView: passing values to adapter")
            binding.recyclerView.adapter = PhotoRecyclerView(galleryItems,thumbnailDownloader){galleryItem ->

                //to open on web page
//                val intent = Intent(Intent.ACTION_VIEW, galleryItem.photoPageUri)
                //in-app web content
                val intent = PhotoPageActivity
                    .newIntent(requireContext(), galleryItem.photoPageUri)
                startActivity(intent)

                //chrome custom tabs
                CustomTabsIntent.Builder()
                    .setToolbarColor(
                        ContextCompat.getColor(
                        requireContext(), R.color.purple_700))
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), galleryItem.photoPageUri)
            }
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(
            thumbnailDownloader.fragmentLifecycleObserver
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar,menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $queryText")
                    viewModel.fetchPhotos(queryText)
                    return true
                }
                override fun onQueryTextChange(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextChange: $queryText")
                    return false
                }
            })

            setOnSearchClickListener {
                searchView.setQuery(viewModel.searchTerm,false)
            }
        }

        val toggleItem =  menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if (isPolling){
            R.string.stop_polling
        }else{
            R.string.start_polling
        }

        toggleItem.setTitle(toggleItemTitle)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_item_search ->{
                viewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling ->{

                val isPolling = QueryPreferences.isPolling(requireContext())
                if(isPolling){
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    QueryPreferences.setPolling(requireContext(),false)
                }else{
                    val constraint = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicWorkRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java,15,TimeUnit.MINUTES)
                        .setConstraints(constraint)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(
                        POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicWorkRequest)
                    QueryPreferences.setPolling(requireContext(),true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}