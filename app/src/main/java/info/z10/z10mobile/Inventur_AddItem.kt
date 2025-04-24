package info.z10.z10mobile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class Inventur_AddItem : Fragment() {
    private fun setThresholdZero(autoCompleteTextView: AutoCompleteTextView) {
        autoCompleteTextView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                autoCompleteTextView.showDropDown()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val name_tv = requireView().findViewById<AutoCompleteTextView>(R.id.nameAutoCompletetv)
        val ean_tv = requireView().findViewById<AutoCompleteTextView>(R.id.eanAutoCompletetv)

        val db = Inventur.AppDatabase.getDatabase(requireView().context)

        lifecycleScope.launch {
            db.itemDao().getAllKnownItems().collect { knownItems ->
                var names = mutableListOf<String>()
                var eans = mutableListOf<Long>()

                for (item in knownItems) {
                    names.add(item.name)
                    item.ean?.let { eans.add(it) }
                }

                name_tv.setAdapter(
                    ArrayAdapter<String>(requireView().context, android.R.layout.select_dialog_item, names)
                )
                ean_tv.setAdapter(
                    ArrayAdapter<Long>(requireView().context, android.R.layout.select_dialog_item, eans)
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_inventur_additem, container, false)

        val name_tv = view.findViewById<AutoCompleteTextView>(R.id.nameAutoCompletetv)
        val ean_tv = view.findViewById<AutoCompleteTextView>(R.id.eanAutoCompletetv)
        val bundleVariant_tv = view.findViewById<AutoCompleteTextView>(R.id.bundlevariantAutoCompletetv)
        val count_et = view.findViewById<EditText>(R.id.countet)

        Log.d("TEST", bundleVariants.toTypedArray()[0])

        bundleVariant_tv.setAdapter(
            ArrayAdapter<String>(view.context, android.R.layout.select_dialog_item, bundleVariants.toTypedArray())
        )

        setThresholdZero(name_tv)
        setThresholdZero(ean_tv)
        setThresholdZero(bundleVariant_tv)

        view.findViewById<Button>(R.id.scanbtn).setOnClickListener {
            create_scanner(view.context).startScan()
                .addOnSuccessListener { barcode ->
                    val ean_tv = view.findViewById<AutoCompleteTextView>(R.id.eanAutoCompletetv)
                    ean_tv.setText(barcode.displayValue)

                    if (barcode.displayValue?.isNotEmpty() == true && barcode.displayValue?.isNotBlank() == true) {
                        get_article_name_from_ean(
                            view.context,
                            barcode.displayValue.orEmpty()
                        ) { response ->
                            try {
                                val json = JSONObject(response) // String instance holding the above json
                                name_tv.setText(json.getString("title"))
                            } catch (_: Exception) {
                                Toast.makeText(this.context, "EAN barcode.displayValue konnte nicht in Onlinedatenbank gefunden werden", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                .addOnCanceledListener {
                    // Task canceled
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                }
        }

        view.findViewById<Button>(R.id.continuebtn).setOnClickListener {
            val name = name_tv.text.toString()
            val ean = ean_tv.text.toString()
            val bundleVariant = bundleVariant_tv.text.toString()
            val count = count_et.text.toString()

            if (name.length < 5) {
                infoDialogue(view.context, "Name muss mindestens fünf Zeichen lang sein").show()
            } else if (bundleVariant.length < 3) {
                infoDialogue(view.context, "Einheitsbezeichnung muss mindestens drei Zeichen lang sein").show()
            } else if (count.isBlank()) {
                infoDialogue(view.context, "Bitte gib eine Anzahl an").show()
            } else {

                val db = Inventur.AppDatabase.getDatabase(view.context)
                lifecycleScope.launch(Dispatchers.IO) {
                    db.itemDao().getAllKnownItems().collect{ already_existing_knownItems ->
                        var knownItemId: Long = 0
                        var existsalready = false
                        for (item in already_existing_knownItems) {
                            if ((item.name == name) and (item.ean == ean.toLong())) {
                                existsalready = true
                                knownItemId = item.knownItemId
                                break
                            }
                        }
                        if (!existsalready) {
                            knownItemId = db.itemDao()
                                .insertKnownItem(Inventur.KnownItem(0, name, ean.toLong()))
                        }
                        db.itemDao().insertAddedItem(Inventur.AddedItem(0, knownItemId, bundleVariant))

                        name_tv.text.clear()
                        ean_tv.text.clear()
                        bundleVariant_tv.text.clear()
                        count_et.text.clear()
                    }
                }
            }
        }

        return view
    }
}