package com.reverseregistry

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import coil.load
import com.reverseregistry.databinding.ActivityImageSubmitBinding

class ImageSubmitActivity : AppCompatActivity() {
    companion object {
        const val IMAGE_URI_KEY = "image_uri_key"
    }
    private lateinit var binding: ActivityImageSubmitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageSubmitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getParcelableExtra<Uri>(IMAGE_URI_KEY)
        binding.imageView.load(imageUri)
    }
}