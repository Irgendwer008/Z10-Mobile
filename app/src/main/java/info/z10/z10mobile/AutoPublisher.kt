package info.z10.z10mobile

import GoogleDriveUpload
import android.Manifest
import android.content.pm.PackageManager

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


class AutoPublisher : Fragment() {

    lateinit var image: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_autopublisher, container, false)

        val contentIV = view.findViewById<ImageView>(R.id.imageView)
        val selectImageBtn = view.findViewById<Button>(R.id.select_image_btn)
        val publishBtn = view.findViewById<Button>(R.id.publish_btn)

        var uri: Uri? = null

        //
        // Image Selection
        //

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { newUri ->
            if (newUri != null) {
                if (uri == null) {
                    publishBtn.isEnabled = true
                    publishBtn.setBackgroundColor(resources.getColor(R.color.accent, ContextThemeWrapper(requireContext(), R.style.Theme_Z10Mobile).theme))
                }

                uri = newUri

                image = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().contentResolver, uri!!))

                if (image.byteCount < 100 * 1024 * 1024) { // 100MB, otherwise RuntimeError occurs with large images
                    contentIV.setImageURI(uri)
                } else {
                    val aspRat = image.width / image.height
                    val w2 = Resources.getSystem().displayMetrics.widthPixels
                    val h2 = w2 * aspRat
                    contentIV.setImageBitmap(Bitmap.createScaledBitmap(image, w2, h2, false))
                }
            }
        }

        requestWriteExternalStoragePermission()

        // Select Image onClickListener
        selectImageBtn.setOnClickListener { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        publishBtn.setOnClickListener { GoogleDriveUpload(requireContext(), image) }


        return view
    }


    private fun requestWriteExternalStoragePermission() {
        var REQUEST_WRITE_EXTERNAL_STORAGE = 1;

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL_STORAGE
            )
        }
    }
}

