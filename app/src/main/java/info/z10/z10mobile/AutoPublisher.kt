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

        // Registers a photo picker activity launcher in single-select mode.
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { newUri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (newUri != null) {
                if (uri == null) {
                    publishBtn.isEnabled = true
                    publishBtn.setBackgroundColor(resources.getColor(R.color.accent, ContextThemeWrapper(requireContext(), R.style.Theme_Z10Mobile).theme))
                }

                uri = newUri

                val image = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().contentResolver, uri!!))

                if (image.byteCount >= 100 * 1024 * 1024) { // 10MB
                    val w = image.width
                    val h = image.height
                    val aspRat = w / h
                    val w2 = Resources.getSystem().displayMetrics.widthPixels
                    val h2 = w2 * aspRat

                    contentIV.setImageBitmap(Bitmap.createScaledBitmap(image, w2, h2, false))
                } else {
                    contentIV.setImageURI(uri)
                }
            }
        }

        selectImageBtn.setOnClickListener { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }



        // Include only one of the following calls to launch(), depending on the types
        // of media that you want to let the user choose from.

        // Launch the photo picker and let the user choose images and videos.

        /*

        // getExternalFilesDir() + "/Pictures" should match the declaration in fileprovider.xml paths
        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png")

        // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
        val bmpUri = context?.let { FileProvider.getUriForFile(it, "com.codepath.fileprovider", file) }

        val intent = Intent().apply {
            this.action = Intent.ACTION_SEND
            this.putExtra(Intent.EXTRA_STREAM, bmpUri)
            this.type = "image/jpeg"
        }
        requireContext().startActivity(Intent.createChooser(intent, "awdawd"))

        Log.d("afewfawsf", bmpUri.toString())

         */












        /*
        // Sharing to Instagram via Intent:
        // Docs: https://developers.facebook.com/docs/instagram/sharing-to-stories/

        // Instantiate an intent
        val intent = Intent("com.instagram.share.ADD_TO_STORY")

        // Attach your App ID to the intent
        val sourceApplication = R.string.facebook_app_ID // This is your application's FB ID

        intent.putExtra("source_application", sourceApplication)

        // Attach your image to the intent from a URI
        val backgroundAssetUri = Uri.parse("your-image-asset-uri-goes-here")
        intent.setDataAndType(backgroundAssetUri, MEDIA_TYPE_JPEG)

        // Grant URI permissions for the image
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Instantiate an activity
        val activity: Activity? = activity

        // Verify that the activity resolves the intent and start it
        if (activity!!.packageManager.resolveActivity(intent, 0) != null) {
            activity!!.startActivityForResult(intent, 0)
        }*/

        return view
    }
}


