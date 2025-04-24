package info.z10.z10mobile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import androidx.room.Transaction
import com.android.volley.Request
import com.android.volley.Response.Listener
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import info.z10.z10mobile.R.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.navigation.findNavController
import kotlinx.coroutines.Dispatchers


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
        fun getAllKnownItems(): Flow<List<KnownItem>>

        @Query("SELECT * FROM AddedItem")
        fun getAllAddedItems(): Flow<List<AddedItem>>

        @Transaction
        @Query("SELECT * FROM KnownItem")
        fun getAddedItemsfromKnownItem(): Flow<List<KnownItemWithAddedItems>>

        @Insert
        fun insertKnownItem(knownItem: KnownItem): Long

        @Insert
        fun insertAddedItem(addedItem: AddedItem): Long

        @Delete
        fun delete(user: KnownItem)

        @Query("DELETE FROM KnownItem")
        fun deleteAllKnownItems()
        @Query("DELETE FROM AddedItem")
        fun deleteAllAddedItems()
        @Query("DELETE FROM sqlite_sequence WHERE name IN ('KnownItem', 'AddedItem')")
        fun deletePrimaryKeyIndex()
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

        val db = Room.databaseBuilder(
            view.context,
            AppDatabase::class.java, "item-db"
        ).build()

        view.findViewById<Button>(R.id.addItembtn).setOnClickListener{ view.findNavController().navigate(R.id.action_inventur_to_inventur_addItem)}
        view.findViewById<Button>(R.id.clear_db).setOnClickListener{

            Log.i("TEST", "Still Synchronous")
            lifecycleScope.launch(Dispatchers.IO) {
                db.itemDao().getAllKnownItems().collect{ knownItems ->
                    for (item in knownItems) {
                        Log.e("TEST", "${item.name}: ${item.ean}")
                    }
                }

                Log.i("TEST", "Starting Deletion")

                db.clearAllTables()
                db.itemDao().deleteAllKnownItems()
                db.itemDao().deleteAllAddedItems()
                db.itemDao().deletePrimaryKeyIndex()

                db.itemDao().getAllKnownItems().collect{ knownItems ->
                    for (item in knownItems) {
                        Log.e("TEST", "${item.name}: ${item.ean}")
                    }
                }
                Log.i("TEST", "Done")
            }
        }

        return view
    }
}
