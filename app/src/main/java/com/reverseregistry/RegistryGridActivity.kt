package com.reverseregistry

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reverseregistry.databinding.ActivityRegistryGridBinding
import com.reverseregistry.databinding.ItemGridPhotoBinding

class RegistryGridActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistryGridBinding
    private lateinit var gridAdapter: PhotoGridAdapter
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // Camera activity was launched successfully and photo was taken
            // You can now process the photo and update your UI
        } else {
            // Camera activity was launched but photo was not taken
            // You can show an error message or handle it in any other way
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistryGridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gridAdapter = PhotoGridAdapter(this)
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
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
        )
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("com.reverseregistry")
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

            val photos = ArrayList<Photo>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateTaken = cursor.getLong(dateTakenColumn)
                photos.add(Photo(id, name, dateTaken))
            }
            gridAdapter.submitList(photos)
        }
    }
}
data class Photo(val id: Long, val name: String, val dateTaken: Long)

class PhotoGridAdapter(private val context: Context) : ListAdapter<Photo, PhotoGridAdapter.ViewHolder>(DiffCallback) {
    private var photoUris: List<Uri> = emptyList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGridPhotoBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photoUris[position])
    }

    override fun getItemCount(): Int {
        return photoUris.size
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

