package info.z10.z10mobile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageHelper {
    companion object {
        fun requestWriteExternalStoragePermission(activity: Activity, context: Context) {

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1 // REQUEST_WRITE_EXTERNAL_STORAGE
                )
            }
        }

        // converting the bitmap to a File
        private fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String, fileSuffix: String): File {
            //create a file to write bitmap data
            val uri = kotlin.io.path.createTempFile(fileNameToSave, fileSuffix).toUri()
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
}
