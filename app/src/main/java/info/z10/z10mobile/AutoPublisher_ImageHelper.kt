package info.z10.z10mobile

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AutoPublisher_ImageHelper(fragment: Fragment, view: View, buttonArray: Array<Button>) {

    private val fragment = fragment

    private val contentIV: ImageView = view.findViewById(R.id.imageView)

    private val pickMedia = fragment.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { newUri ->
        if (newUri != null) {
            if (AutoPublisher.uri == null) {
                for (btn in buttonArray) {
                    btn.isEnabled = true
                    btn.setBackgroundColor(fragment.resources.getColor(R.color.accent, ContextThemeWrapper(fragment.requireContext(), R.style.Theme_Z10Mobile).theme))
                }
            }

            AutoPublisher.uri = newUri

            val image = ImageDecoder.decodeBitmap(ImageDecoder.createSource(fragment.requireActivity().contentResolver, AutoPublisher.uri!!))

            if (image.byteCount < 100 * 1024 * 1024) { // 100MB, otherwise RuntimeError occurs with large images
                contentIV.setImageURI(AutoPublisher.uri)
            } else {
                val aspRat = image.width / image.height
                val w2 = Resources.getSystem().displayMetrics.widthPixels
                val h2 = w2 * aspRat
                contentIV.setImageBitmap(Bitmap.createScaledBitmap(image, w2, h2, false))
            }
        }
    }

    fun launchImagePicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun convertToJPG(): Uri? {
        // For converting everything to jpeg
        // From https://stackoverflow.com/questions/33222918/sharing-bitmap-via-android-intent

        val image = ImageDecoder.decodeBitmap(ImageDecoder.createSource(fragment.requireActivity().contentResolver, AutoPublisher.uri!!))

        val imagesFolder: File = File(fragment.requireContext().cacheDir, "images")
        var jpgUri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.flush()
            stream.close()
            jpgUri = FileProvider.getUriForFile(fragment.requireContext(), "info.z10.z10mobile.fileprovider", file)
        } catch (e: IOException) {
            Log.e("TAAAAAAAAAAG", "IOException while trying to write file for sharing: " + e.message)
        }

        return jpgUri
    }
}