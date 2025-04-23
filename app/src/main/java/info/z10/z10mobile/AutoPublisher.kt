package info.z10.z10mobile

import AutoPublisher_GMB_Helper
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task

class AutoPublisher : Fragment() {


    companion object { var uri: Uri? = null }

    private lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope("https://www.googleapis.com/auth/business.manage"))
            .requestEmail()
            .requestIdToken(getString(R.string.googleClientID))
            .build()

        val signInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.e("AAAAAA", result.toString())
                if (result.resultCode == RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    handleSignInResult(task)
                } else {
                    Log.e("AWWWWWWW", "Not successful, Sign in failed")
                }
            }

        val googleSignInClient = GoogleSignIn.getClient(this.requireActivity(), signInOptions)
        val signInIntent = googleSignInClient.signInIntent

        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            // Signed in successfully, get the access token
            val account = completedTask.getResult(ApiException::class.java)
            accessToken = account?.idToken!!
            println(accessToken)

        } catch (e: ApiException) {
            Log.e("WAAAAAAAAAAAA", "Not successful, Sign in failed: \n\n" + e.printStackTrace())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_autopublisher, container, false)

        val publishButtonArray = arrayOf<Button>(
            view.findViewById(R.id.instaStory_btn),
            view.findViewById(R.id.instaFeed_btn),
            view.findViewById(R.id.googleMyBusiness_btn)
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

        // Insta Story
        view.findViewById<Button>(R.id.instaStory_btn).setOnClickListener { shareViaIntent() } // "com.instagram.share.ADD_TO_STORY"

        // Insta Feed
        view.findViewById<Button>(R.id.instaFeed_btn).setOnClickListener { shareViaIntent() } // "com.instagram.share.ADD_TO_FEED"

        // Google My Business Post
        view.findViewById<Button>(R.id.googleMyBusiness_btn).setOnClickListener {
            val helper = AutoPublisher_GMB_Helper(accessToken, getString(R.string.googleAccountID))
            //helper.listAccounts()
            helper.createPost(getString(R.string.googleLocationID), "TestEvent", "Thisisatestevent", "https://raw.githubusercontent.com/test-images/png/main/202105/cs-black-000.png")//https://cloud.z10.whka.de/s/YNwB3JNtRWa4cpt/download?path=&files=")
        }



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