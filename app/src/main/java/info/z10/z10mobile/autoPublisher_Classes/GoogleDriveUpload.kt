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
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.Collections


class GoogleDriveUpload(context: Context, activity: Activity, fragment: Fragment) : Activity() {

    private lateinit var driveServiceHelper: DriveServiceHelper

    private val context = context
    private val activity = activity
    private val fragment = fragment

    fun requestSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1055682838681-tj3fjteamkhnqbu970jsndrgj0ftk91t.apps.googleusercontent.com")
            .requestEmail()
            //.requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        Log.d("asefse", signInOptions.toString())

        val client = GoogleSignIn.getClient(activity, signInOptions)

        val launcher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.e("AAAAAA", result.toString())
                if (result.resultCode == RESULT_OK) {
                    handleSignInIntent(result.data!!)

                }
            }

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

            driveServiceHelper = DriveServiceHelper(drive)
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
}