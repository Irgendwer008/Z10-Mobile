package info.z10.z10mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class AutoPublisher : Fragment() {


    companion object { var uri: Uri? = null }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_autopublisher, container, false)

        val publishButtonArray = arrayOf<Button>(
            view.findViewById(R.id.instaStory_btn),
            view.findViewById(R.id.instaFeed_btn)
        )


        //
        // Image Selection
        //

        val imageHelper = AutoPublisher_ImageHelper(this, view, publishButtonArray)
        view.findViewById<Button>(R.id.select_btn).setOnClickListener {
            imageHelper.launchImagePicker()
        }

        //
        // Publishing
        //

        // Insta Story Intent
        view.findViewById<Button>(R.id.instaStory_btn).setOnClickListener { shareViaIntent() } // "com.instagram.share.ADD_TO_STORY"

        // Insta Feed Intent
        view.findViewById<Button>(R.id.instaFeed_btn).setOnClickListener { shareViaIntent() } // "com.instagram.share.ADD_TO_FEED"

        return view
    }

    private fun shareViaIntent() {

        val intentAction = Intent.ACTION_SEND

        val targetedShareIntents = ArrayList<Intent>()
        val shareIntent = Intent(intentAction)//Intent.ACTION_SEND)
        shareIntent.setType("image/*")
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        val resInfo = requireActivity().packageManager.queryIntentActivities(shareIntent, 0)
        if (resInfo.isNotEmpty()) {

            val packageName = "com.instagram.android" //"com.facebook.pages.app"
            val targetedShareIntent = Intent(intentAction)
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
}