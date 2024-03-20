package info.z10.z10mobile

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

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

            var file = File(filePath)

            file = java.io.File(filePath)

            val mediaContent = FileContent("image/jpeg", file)

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