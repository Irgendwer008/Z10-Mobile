package info.z10.z10mobile

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.volley.Request
import com.android.volley.Response.Listener
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import info.z10.z10mobile.R.*
import info.z10.z10mobile.DatabaseApplication.Companion.database as db
import info.z10.z10mobile.Inventur_Dinge.FileHelper
import kotlinx.coroutines.launch
import androidx.navigation.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


var bundleVariants = mutableListOf(
    "Stück",
    "Fass",
    "Flasche",
    "6er Kasten",
    "20er Kasten",
    "24er Kasten"
)

fun create_scanner(context: Context): GmsBarcodeScanner {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_EAN_13)
        .build()

    // val scanner = GmsBarcodeScanning.getClient(this)
    // Or with a configured options
    return GmsBarcodeScanning.getClient(context, options)
}

fun get_article_name_from_ean(context: Context, ean: String, listener: Listener<String>) {
    // Instantiate the RequestQueue.
    val queue = Volley.newRequestQueue(context)
    val url =
        "https://api.upcdatabase.org/product/${ean}?apikey=2E94E7D439F2D38A6EE53676D295C1A0"

    // Request a string response from the provided URL.
    val stringRequest = StringRequest(
        Request.Method.GET, url, listener
    ) { Log.w("EAN_LOOKUP_RESPONSE", "That didn't work!") }

    // Add the request to the RequestQueue.
    queue.add(stringRequest)
}

class Inventur : Fragment() {

    @Entity
    data class KnownItem(
        @PrimaryKey(autoGenerate = true) val knownItemId: Long = 0,
        val name: String,
        val ean: Long?,
    )


    @Entity
    data class AddedItem(
        @PrimaryKey(autoGenerate = true) val addedItemId: Long = 0,
        val knownItemId: Long,
        val bundleVariant: String,
        val count: Int
    )

    data class KnownItemWithAddedItems(
        @Embedded val knownItem: KnownItem,
        @Relation(
            parentColumn = "knownItemId",
            entityColumn = "knownItemId"
        )
        val addedItems: List<AddedItem>,
    )


    @Dao
    interface ItemDao {
        @Query("SELECT * FROM KnownItem")
        suspend fun getAllKnownItems(): List<KnownItem>

        @Query("SELECT * FROM AddedItem")
        suspend fun getAllAddedItems(): List<AddedItem>

        @Query("SELECT * FROM KnownItem WHERE knownItemId = :knownItemId")
        suspend fun getKnownItem(knownItemId: Long): KnownItem

        @Insert
        suspend fun insertKnownItem(knownItem: KnownItem): Long

        @Insert
        suspend fun insertAddedItem(addedItem: AddedItem): Long

        @Delete
        suspend fun delete(user: KnownItem)

        @Query("DELETE FROM KnownItem")
        suspend fun deleteAllKnownItems()
        @Query("DELETE FROM AddedItem")
        suspend fun deleteAllAddedItems()
        @Query("DELETE FROM sqlite_sequence WHERE name IN ('KnownItem', 'AddedItem')")
        suspend fun deletePrimaryKeyIndex()
    }

    @Database(entities = [KnownItem::class, AddedItem::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun itemDao(): ItemDao

        companion object {
            @Volatile
            private var INSTANCE: AppDatabase? = null

            fun getDatabase(context: Context): AppDatabase {
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "AppDatabase"
                    ).build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(layout.fragment_inventur, container, false)

        refresh_table(view)

        view.findViewById<Button>(R.id.addItembtn).setOnClickListener{ view.findNavController().navigate(R.id.action_inventur_to_inventur_addItem)}
        view.findViewById<Button>(R.id.export_btn).setOnClickListener{
            lifecycleScope.launch {
                FileHelper().export_db(requireContext(), db)
            }
        }
        view.findViewById<Button>(R.id.clear_db).setOnClickListener{
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setMessage("Willst du wirklich alle Einträge unwiderruflich löschen?")
            builder.setTitle("Alles Löschen")
            builder.setPositiveButton("Ok", { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        db.clearAllTables()
                        db.itemDao().deleteAllKnownItems()
                        db.itemDao().deleteAllAddedItems()
                        db.itemDao().deletePrimaryKeyIndex()
                    }
                    refresh_table(view)
                }
            })
            builder.setNeutralButton("Cancel", {_, _ -> })
            builder.create().show()
        }
        return view
    }

    fun refresh_table(view: View) {

        val table = view.findViewById<TableLayout>(R.id.addedItems_tableview)

        table.removeAllViews()

        lifecycleScope.launch {
            val addedItems = db.itemDao().getAllAddedItems()
            Log.i("DEBUG", "Added items: ${addedItems.size}")

            for (addedItem in addedItems) {
                val knownItem = db.itemDao().getKnownItem(addedItem.knownItemId)
                Log.i("DEBUG", "Name: ${knownItem.name}, EAN: ${knownItem.ean}, KIID: ${knownItem.knownItemId}, Bundle: ${addedItem.bundleVariant}, Count: ${addedItem.count}")

                withContext(Dispatchers.Main) {
                    val row = TableRow(requireContext()).apply {
                        layoutParams = TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    row.addView(TextView(requireContext()).apply {
                        text = knownItem.name
                        textSize = 20.0F
                        setBackgroundResource(R.drawable.table_border)
                        setPadding(10, 5, 5, 5)
                    })
                    row.addView(TextView(requireContext()).apply {
                        text = addedItem.bundleVariant
                        textSize = 20.0F
                        setBackgroundResource(R.drawable.table_border)
                        setPadding(10, 5, 5, 5)
                    })
                    row.addView(TextView(requireContext()).apply {
                        text = addedItem.count.toString()
                        textSize = 20.0F
                        setBackgroundResource(R.drawable.table_border)
                        setPadding(10, 5, 5, 5)
                    })

                    table.addView(row)
                }
            }
        }
    }
}
