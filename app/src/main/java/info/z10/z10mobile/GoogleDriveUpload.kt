package info.z10.z10mobile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader


class GoogleDriveUpload (context: Context?, activity: Activity, bitmap: Bitmap) :
    ComponentActivity() {

    val context = context!!
    val activity = activity!!

    private val fields = "nextPageToken, files(id, name)"

    private val accessDriveScope: Scope = Scope(Scopes.DRIVE_FILE)
    private val scopeEmail: Scope = Scope(Scopes.EMAIL)

    private var isFileRead = false
    private var googleSignInClient: GoogleSignInClient? = null
    private var recievedText: String = "Hello World"


    private lateinit var uploadButton: Button
    private lateinit var readButton: Button

    private var launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                task.getResult(ApiException::class.java)
                checkForGooglePermissions()
            } catch (e: ApiException) {

            }
        }
    }
    private fun checkForGooglePermissions() {
        if (!GoogleSignIn.hasPermissions(
            GoogleSignIn.getLastSignedInAccount(context),
            accessDriveScope,
            scopeEmail)) {
            GoogleSignIn.requestPermissions(
                activity,
                1,
                GoogleSignIn.getLastSignedInAccount(context),
                accessDriveScope,
                scopeEmail
            )
        } else {
            lyfecycle.coroutineScope.launch { driveSetUp() }
        }
    }

    private fun createTempFileInInternalStorage(): File {
        var privateDir = filesDir
        privateDir = File(privateDir, MY_RECOVERED_DIR)
        privateDir.mkdir()
        privateDir = File(privateDir, MY_RECOVERED_TEXT_FILE)

        return privateDir
    }

    private fun readDataFromFile(file: File): String? {
        try {
            val br = BufferedReader(FileReader(file))
            val line = br.readLines().joinToString()
            br.close()
            return line
        } catch (_: Exception) {

        }
        return null
    }

    private fun uploadFile(
        mDriveService: Drive,
        localFile: File,
        mimeType: String?,
        folderId: String?
    ): GoogleDriveFileHolder {
        val root = folderId?.let { listOf(it) } ?: listOf(ROOT)

        val metadata = com.google.api.services.drive.model.File()
            .setParents(root)
            .setMimeType(mimeType)
            .setName(localFile.name)

        val  fileContent = FileContent(mimeType, localFile)

        val fileMeta = mDriveService.files().create(
            metadata,
            fileContent
        ).execute()

        val googleDriveFileHolder = GoogleDriveFileHolder()
        googleDriveFileHolder.id = fileMeta.id
        googleDriveFileHolder.name = fileMeta.name

        return googleDriveFileHolder
    }

    private fun createFileInInternalStorage(text: String): File? {
        var privateDir = filesDir
        privateDir = File(privateDir, MY_APP)
        privateDir.mkdir()
        privateDir = File(privateDir, "$MY_APP_LOWER.txt")

        try {
            val fileOutputStream = FileOutputStream(privateDir)
            fileOutputStream.write(text.toByteArray())
            fileOutputStream.close()
            return privateDir
        } catch (_: FileNotFoundException) { }

        return null
    }

    private fun downloadFile(
        mDriveService:Drive,
        targetFile: File?
    )







    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(Scopes.DRIVE_FULL))
            .build()

        return GoogleSignIn.getClient(context, signInOptions)
    }









    // converting the bitmap to a File
    private fun bitmapToFile(context: Context?, bitmap: Bitmap, fileNameToSave: String): File { // File name like "image.png"
        //create a file to write bitmap data
        val uri = kotlin.io.path.createTempFile("temp", ".jpg").toUri()
        val file = File(uri)



        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, bos) // YOU can also save it in JPEG
        val bitmapdata = bos.toByteArray()

        //write the bytes in file
        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
        return file
    }
}