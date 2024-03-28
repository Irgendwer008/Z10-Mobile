package info.z10.z10mobile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.mybusinessplaceactions.v1.MyBusinessPlaceActions
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.concurrent.Executors


class AutoPublisher_GoogleHelper(context: Context, activity: Activity, fragment: Fragment) : Activity() {

    private lateinit var driveServiceHelper: DriveServiceHelper
    private lateinit var gmbServiceHelper: GMBServiceHelper

    private val context = context
    private val activity = activity
    private val fragment = fragment

    private val launcher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e("AAAAAA", result.toString())
            if (result.resultCode == RESULT_OK) {
                handleSignInIntent(result.data!!)

            }
        }

    fun requestSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1055682838681-tj3fjteamkhnqbu970jsndrgj0ftk91t.apps.googleusercontent.com")
            .requestEmail()
            //.requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        val client = GoogleSignIn.getClient(activity, signInOptions)

        val signInIntent = client.signInIntent
        launcher.launch(signInIntent)
    }

    private fun handleSignInIntent(data: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener {
            val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
                this,
                Collections.singleton(DriveScopes.DRIVE_FILE)
            )

            credential.setSelectedAccount(it.account)

            val drive = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            )
                .setApplicationName("My Drive Tutorial")
                .build()

            val gmb = MyBusinessPlaceActions.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential)
                .setApplicationName("AutoPublisher")
                .build()

            driveServiceHelper = DriveServiceHelper(drive)
            gmbServiceHelper = GMBServiceHelper(gmb)

            Log.e("AAAAAAAAAAA", "B")
        }.addOnFailureListener {
            Log.e("asefawsesf", "het ned geklappt")
        }
    }

    fun uploadFile(filePath: String) {
        //val progressDialog = ProgressDialog(context)
        //progressDialog.setTitle("Uploading to Google Drive")
        //progressDialog.setMessage("Please Wait...")
        //progressDialog.show()

        driveServiceHelper.createFile(filePath)
            .addOnSuccessListener {
                //progressDialog.dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(
                    applicationContext,
                    "Check your Google drive api key",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun uploadEvent() {
        gmbServiceHelper.createEvent()
    }
}

class DriveServiceHelper(mdriveService: Drive) {

    private val mExecutor = Executors.newSingleThreadExecutor()
    private lateinit var mDriveService: Drive

    init  {
        var mDriveService: Drive = mdriveService
    }

    fun createFile(filePath: String): Task<String> {
        return Tasks.call(mExecutor) {
            val fileMetaData = com.google.api.services.drive.model.File()
            fileMetaData.name = "MyPDF-File"

            val mediaContent = FileContent("image/jpeg", File(filePath))

            var myFile: com.google.api.services.drive.model.File? = null

            try {
                myFile = mDriveService.files().create(fileMetaData, mediaContent).execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (myFile == null) {
                throw IOException("")
            } else (String())
        }
    }
}

class GMBServiceHelper(gmbservice: MyBusinessPlaceActions) {

    private val mExecutor = Executors.newSingleThreadExecutor()
    private lateinit var gmbservice: MyBusinessPlaceActions

    init  {
        var gmbservice: MyBusinessPlaceActions = gmbservice
    }

    fun createEvent(): Task<String> {
        return Tasks.call(mExecutor) {

            Log.d("AWDAWD", gmbservice.Locations().toString() + "    " + gmbservice.baseUrl)



            String()
        }
    }
}