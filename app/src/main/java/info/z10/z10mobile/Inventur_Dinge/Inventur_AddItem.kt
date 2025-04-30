package info.z10.z10mobile.Inventur_Dinge

import android.os.Bundle
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
import info.z10.z10mobile.Inventur
import info.z10.z10mobile.R
import info.z10.z10mobile.bundleVariants
import info.z10.z10mobile.create_scanner
import info.z10.z10mobile.get_article_name_from_ean
import info.z10.z10mobile.infoDialogue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.properties.Delegates
import info.z10.z10mobile.DatabaseApplication.Companion.database as db


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

        lifecycleScope.launch {
            val knownItems = db.itemDao().getAllKnownItems()

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

        name_tv.setOnItemClickListener { _, _, position, _ ->
            lifecycleScope.launch {
                val text = name_tv.adapter.getItem(position)
                try {
                    ean_tv.setText(db.itemDao().findKnownItemByName(text.toString()).ean.toString())
                } catch (_: NullPointerException) {}
            }
        }

        ean_tv.setOnItemClickListener { _, _, position, _ ->
            lifecycleScope.launch {
                val text = ean_tv.adapter.getItem(position)
                try {
                    name_tv.setText(db.itemDao().findKnownItemByEAN(text.toString().toLong()).name)
                } catch (_: IllegalStateException) {}
            }
        }

        bundleVariant_tv.setAdapter(
            ArrayAdapter<String>(view.context, android.R.layout.select_dialog_item, bundleVariants.toTypedArray())
        )

        setThresholdZero(name_tv)
        setThresholdZero(ean_tv)
        setThresholdZero(bundleVariant_tv)

        fun runScan() {
            create_scanner(view.context).startScan()
                .addOnSuccessListener { barcode ->
                    ean_tv.setText(barcode.displayValue)
                    if (barcode.displayValue?.isNotBlank() == true) {
                        lifecycleScope.launch {
                            try {
                                name_tv.setText(db.itemDao().findKnownItemByEAN(barcode.displayValue!!.toLong()).name)
                            } catch (_: IllegalStateException) {
                                get_article_name_from_ean(
                                    view.context,
                                    barcode.displayValue.orEmpty()
                                ) { response ->
                                    try {
                                        val json = JSONObject(response)
                                        name_tv.setText(json.getString("title"))
                                    } catch (_: Exception) {
                                        Toast.makeText(requireContext(), "EAN ${barcode.displayValue} konnte nicht in Onlinedatenbank gefunden werden", Toast.LENGTH_LONG).show()
                                    }
                                }
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

        val args =  Inventur_AddItemArgs.fromBundle(requireArguments())
        if (args.triggerScanningImmediately) {
            runScan()
        }

        view.findViewById<Button>(R.id.scanbtn).setOnClickListener { runScan() }

        fun save(name: String, ean: Long, bundleVariant: String, count: String) {
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val already_existing_knownItems = db.itemDao().getAllKnownItems()

                        var knownItemId by Delegates.notNull<Long>()
                        var existsalready = false
                        for (item in already_existing_knownItems) {
                            if ((item.name == name) and (item.ean == ean)) {
                                existsalready = true
                                knownItemId = item.knownItemId
                                break
                            }
                        }
                        if (!existsalready) {
                            knownItemId = db.itemDao().insertKnownItem(Inventur.KnownItem(0, name, ean))
                        }

                        try {
                            db.itemDao().insertAddedItem(
                                Inventur.AddedItem(
                                    0,
                                    knownItemId,
                                    bundleVariant,
                                    count.toInt()
                                )
                            )
                        } catch (_: Exception) { }

                        if (isAdded) {
                            withContext(Dispatchers.Main) {
                                name_tv.text.clear()
                                ean_tv.text.clear()
                                bundleVariant_tv.text.clear()
                                count_et.text.clear()
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }

        fun checkIfExceptionThrows(exceptionCause: Throwable?, blockToTest: () -> Unit = {}): Boolean{
            try {
                blockToTest()
                return false
            } catch (e: Exception) {
                return e.cause == exceptionCause
            }
        }

        fun checkInputFieldsAndSave(doIfSuccessful: () -> Unit = {}) {
            val name = name_tv.text.toString()
            val ean = ean_tv.text.toString()
            val bundleVariant = bundleVariant_tv.text.toString()
            val count = count_et.text.toString()

            if (name.length < 5) {
                infoDialogue(view.context, "Name muss mindestens fünf Zeichen lang sein").show()
            } else if (bundleVariant.length < 3) {
                infoDialogue(view.context, "Einheitsbezeichnung muss mindestens drei Zeichen lang sein").show()
            } else if (ean.length < 4) {
                infoDialogue(view.context, "EAN muss mindestens vier Ziffern groß sein").show()
            } else if (count.isBlank()) {
                infoDialogue(view.context, "Bitte gib eine Anzahl an").show()
            } else if (checkIfExceptionThrows(NumberFormatException().cause) { ean.toLong() }) {
                infoDialogue(view.context, "EAN \"$ean\" ist zu groß oder keine Zahl").show()
            } else if (checkIfExceptionThrows(NumberFormatException().cause) { count.toInt() }) {
                infoDialogue(view.context, "Anzahl \"$count\" ist zu groß oder keine Zahl").show()
            } else {
                    save(name, ean.toLong(), bundleVariant, count)
                    doIfSuccessful()
            }
        }

        view.findViewById<Button>(R.id.cancelAndBackbtn).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        view.findViewById<Button>(R.id.saveAndBackbtn).setOnClickListener {
            checkInputFieldsAndSave {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        view.findViewById<Button>(R.id.saveAndNewbtn).setOnClickListener {
            checkInputFieldsAndSave()
        }
        view.findViewById<Button>(R.id.saveAndScanbtn).setOnClickListener {
            checkInputFieldsAndSave {
                runScan()
            }
        }

        return view
    }
}