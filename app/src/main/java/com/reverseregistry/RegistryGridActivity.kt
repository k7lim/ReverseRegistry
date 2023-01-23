package com.reverseregistry

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reverseregistry.databinding.ActivityRegistryGridBinding
import com.reverseregistry.databinding.ItemGridPhotoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistryGridActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistryGridBinding
    private lateinit var gridAdapter: PhotoGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistryGridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gridAdapter = PhotoGridAdapter(this)
        binding.photosGrid.layoutManager = GridLayoutManager(this@RegistryGridActivity, 3)
        binding.photosGrid.adapter = gridAdapter

        loadPhotos()

        binding.addFromCameraButton.setOnClickListener {
            startActivity(Intent(this@RegistryGridActivity, CameraActivity::class.java))
        }

        binding.addFromGalleryButton.setOnClickListener {
            // Open gallery to choose photo
        }
    }

    private fun loadPhotos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

            val cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null, //we want everything
                null,
                sortOrder
            )

            if (cursor != null) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

                val photos = ArrayList<Photo>()
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    photos.add(Photo(id, name, dateTaken, uri))
                }
                cursor.close()
                withContext(Dispatchers.Main) {
                    gridAdapter.submitList(photos)
                }
            }
        }
    }

}
data class Photo(val id: Long, val name: String, val dateTaken: Long, val uri: Uri)

class PhotoGridAdapter(private val context: Context) : ListAdapter<Photo, PhotoGridAdapter.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGridPhotoBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position].uri)
    }

    inner class ViewHolder(private val binding: ItemGridPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            binding.photoGridItemImage.setImageURI(uri)
        }
    }
    object DiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }
    }
}

