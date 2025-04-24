package info.z10.z10mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.room.Room
import info.z10.z10mobile.Inventur.AppDatabase
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.text.RegexOption

class Inventur_AddItem : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_inventur_additem, container, false)

        view.findViewById<Button>(R.id.continuebtn).setOnClickListener {
            try {
                create_scanner(view.context).startScan()
                    .addOnSuccessListener { barcode ->

                        val ean_tv = view.findViewById<AutoCompleteTextView>(R.id.eanAutoCompletetv)
                        ean_tv.setText(barcode.displayValue)

                        get_article_name_from_ean(
                            view.context,
                            barcode.displayValue.orEmpty()
                        ) { response ->
                            Log.i(
                                "EAN_LOOKUP_RESPONSE",
                                response
                            )

                            val json =
                                JSONObject(response) // String instance holding the above json

                            val name_tv =
                                view.findViewById<AutoCompleteTextView>(R.id.nameAutoCompletetv)
                            name_tv.setText(json.getString("title"))
                        }
                    }
                    .addOnCanceledListener {
                        // Task canceled
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                    }
            } finally {}
        }

        return view
    }
}