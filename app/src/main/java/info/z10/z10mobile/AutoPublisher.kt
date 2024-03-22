package info.z10.z10mobile

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class AutoPublisher : Fragment() {

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

        // Image Selection

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { newUri ->
            if (newUri != null) {
                if (uri == null) {
                    publishBtn.isEnabled = true
                    publishBtn.setBackgroundColor(resources.getColor(R.color.accent, ContextThemeWrapper(requireContext(), R.style.Theme_Z10Mobile).theme))
                }

                uri = newUri

                val image = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().contentResolver, uri!!))

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
        selectImageBtn.setOnClickListener { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        publishBtn.setOnClickListener {


            val targetedShareIntents = ArrayList<Intent>()
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.setType("image/*")
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                val resInfo = requireActivity().packageManager.queryIntentActivities(shareIntent, 0)
                if (!resInfo.isEmpty()) {

                    val packageName = "com.instagram.android" //"com.facebook.pages.app"
                    val targetedShareIntent = Intent(Intent.ACTION_SEND)
                    targetedShareIntent.setType("image/*")
                    targetedShareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    targetedShareIntent.setPackage(packageName)
                    targetedShareIntents.add(targetedShareIntent)

                    val chooserIntent = Intent.createChooser(
                        targetedShareIntents.removeAt(0),
                        "Share"
                    )
                    chooserIntent.putExtra(
                        Intent.EXTRA_INITIAL_INTENTS,
                        targetedShareIntents.toArray(
                            arrayOfNulls<Parcelable>(
                                targetedShareIntents.size
                            )
                        )
                    )
                    startActivity(chooserIntent)
                }

        }

        /* for converting everything to jpeg


        val image = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().contentResolver, uri!!))

        val imagesFolder: File = File(requireContext().cacheDir, "images")
        var jpgUri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.flush()
            stream.close()
            jpgUri = FileProvider.getUriForFile(requireContext(), "info.z10.z10mobile.fileprovider", file)
        } catch (e: IOException) {
            Log.d("TAAAAAAAAAAG", "IOException while trying to write file for sharing: " + e.message)
        }
         */
        return view
    }
}


