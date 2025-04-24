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
import androidx.navigation.Navigation
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Junction
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
import java.sql.Timestamp
import java.util.Date
import java.util.Dictionary
import androidx.navigation.findNavController



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

class Inventur : Fragment() {class Size(val count: Int, val litersPerCount: Double) {
        val totalLiters: Double = count * litersPerCount
    }

    data class Bundle(val count: Int, val title: String)

    val sizes: Array<Size> = arrayOf(
        Size(1, 30.0),
        Size(1, 50.0),
        Size(1, 0.33),
        Size(1, 0.5),
        Size(1, 0.75),
        Size(1, 1.0),
    )

    val bundles: Array<Bundle> = arrayOf(
        Bundle(1,  "Fass"),
        Bundle(1,  "Flasche"),
        Bundle(20, "Kasten"),
        Bundle(24, "Kasten")
    )

    @Entity
    data class KnownItem(
        @PrimaryKey(autoGenerate = true) val knownItemId: Int,
        val name: String,
        val ean: Int?,
    )


    @Entity
    data class AddedItem(
        @PrimaryKey(autoGenerate = true) val addedItemId: Int,
        val knownItemId: Int,
        val sizeId: Int,
        val bundlesId: Int,
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
        fun getAll(): Flow<List<KnownItem>>

        @Transaction
        @Query("SELECT * FROM KnownItem")
        fun getAddedItemsfromKnownItem(): Flow<List<KnownItemWithAddedItems>>

        @Insert
        fun insertAll(vararg knownItems: KnownItem)

        @Delete
        fun delete(user: KnownItem)
    }

    @Database(entities = [KnownItem::class, AddedItem::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun knownItemDao(): ItemDao
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: android.os.Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(layout.fragment_inventur, container, false)

        view.findViewById<Button>(R.id.addItembtn).setOnClickListener{ view.findNavController().navigate(R.id.action_inventur_to_inventur_addItem)}

        val db = Room.databaseBuilder(
            view.context,
            AppDatabase::class.java, "item-db"
        ).build()

        val knownItemDao = db.knownItemDao()

        Log.e("TEST", "AWDDD")

        lifecycleScope.launch {
            knownItemDao.getAll().collect { knownItems ->
                for (item in knownItems) {
                    Log.e("TEST", item.name)
                }
            }
        }

        return view
    }
}
