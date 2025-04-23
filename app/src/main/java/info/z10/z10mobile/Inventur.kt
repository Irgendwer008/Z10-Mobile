package info.z10.z10mobile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response.Listener
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import info.z10.z10mobile.R.*


class Inventur : Fragment() {

    private fun get_article_name_from_ean(ean: String, listener: Listener<String>) {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this.context)
        val url = "https://api.upcdatabase.org/product/${ean}?apikey=2E94E7D439F2D38A6EE53676D295C1A0"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url, listener
        ) { Log.w("EAN_LOOKUP_RESPONSE", "That didn't work!") }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        val view = inflater.inflate(layout.fragment_inventur, container, false)

        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_EAN_13)
            .build()

        // val scanner = GmsBarcodeScanning.getClient(this)
        // Or with a configured options
        val scanner = GmsBarcodeScanning.getClient(view.context, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                get_article_name_from_ean(barcode.displayValue.orEmpty()) { response ->
                    Log.i(
                        "EAN_LOOKUP_RESPONSE",
                        response
                    )
                }
            }
            .addOnCanceledListener {
                // Task canceled
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
            }

        return view
    }
}
