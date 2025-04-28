package info.z10.z10mobile.Inventur_Dinge

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import info.z10.z10mobile.Inventur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets


class FileHelper {
    private suspend fun createCSV(db: Inventur.AppDatabase): String {
        val sb = StringBuilder()
        for (addedItem in db.itemDao().getAllAddedItems()) {
            val knownItem = db.itemDao().getKnownItem(addedItem.knownItemId)
            sb.append("${knownItem.name}; ${knownItem.ean}; ${addedItem.bundleVariant}; ${addedItem.count};\n")
        }
        return sb.toString()
    }

    private fun shareCsv(context: Context, csvContent: String, fileName: String = "export.csv") {
        // Step 1: Create inventory/ subfolder inside cacheDir
        val inventoryDir = File(context.cacheDir, "inventory")
        if (!inventoryDir.exists()) {
            inventoryDir.mkdirs()
        }

        // Step 1: Create a temporary CSV file
        val file = File(inventoryDir, fileName)
        val bom = "\uFEFF"
        file.writeText(bom + csvContent, charset = StandardCharsets.UTF_8)

        // Step 2: Get content URI using FileProvider
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Step 3: Create the share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Step 4: Launch the chooser
        context.startActivity(
            Intent.createChooser(shareIntent, "Share CSV via")
        )
    }

    suspend fun export_db(context: Context, db: Inventur.AppDatabase){
        val csvString = createCSV(db)
        Log.d("DEBUG", csvString)
        shareCsv(context, csvString)
    }
}