package info.z10.z10mobile

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
import androidx.fragment.app.Fragment
import info.z10.z10mobile.ImageHelper.Companion.requestWriteExternalStoragePermission


class AutoPublisher() : Fragment() {

    private lateinit var image: Bitmap
    private var uri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val drive = GoogleDriveUpload(requireContext(), requireActivity(), this)
        drive.requestSignIn()

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_autopublisher, container, false)

        val contentIV = view.findViewById<ImageView>(R.id.imageView)
        val selectImageBtn = view.findViewById<Button>(R.id.select_image_btn)
        val publishBtn = view.findViewById<Button>(R.id.publish_btn)

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

        requestWriteExternalStoragePermission(requireActivity(), requireContext())



        // Select Image onClickListener
        selectImageBtn.setOnClickListener { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        publishBtn.setOnClickListener {
            drive.uploadFile(uri.toString())
        }

        return view
    }
}

