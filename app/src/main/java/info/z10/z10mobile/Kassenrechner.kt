package info.z10.z10mobile

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import info.z10.z10mobile.R.*
import java.lang.Exception


class Kassenrechner : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(layout.fragment_kassenrechner, container, false)

        for (instance in money_array) {
            instance.lateInit(view)
        }

        // get all other Elements
        val result: TextView = view.findViewById(R.id.totalnum)
        val after145: TextView = view.findViewById(R.id.after145num)
        val after145txt: TextView = view.findViewById(R.id.after145tv)
        val change: TextView = view.findViewById(R.id.changenum)
        val clear: Button = view.findViewById(R.id.clear)
        val clear2: Button = view.findViewById(R.id.clear2)
        val grundbestand_btn: Button = view.findViewById(R.id.grundbestand_btn)

        val sollnum: TextView = view.findViewById(R.id.sollnum)

        val switch1 = view.findViewById<Switch>(R.id.switch1)
        val switch2 = view.findViewById<Switch>(R.id.switch2)

        // get previous switch states from androids persistent storage

        val persistingStorage: SharedPreferences? = this.activity?.getPreferences(Context.MODE_PRIVATE)

        val showSmall = persistingStorage?.getBoolean("showSmall", true) == true
        val showBig = persistingStorage?.getBoolean("showBig", true) == true

        switch1.isChecked = showSmall
        switch2.isChecked = showBig

        var grundbestand: Double = try {
            persistingStorage?.getString("grundbestand", "")!!.toDouble()
        } catch (e: Exception) {
            145.0
        }

        var soll = 0.0

        // set Switch States from sharedPreferences
        if (showSmall) {
            for (instance in money_array.subList(0, 3)) {
                instance.box.visibility = View.VISIBLE
            }
        } else {
            for (instance in money_array.subList(0, 3)) {
                instance.box.visibility = View.GONE
            }
        }

        if (showBig) {
            for (instance in money_array.subList(12, 15)) {
                instance.box.visibility = View.VISIBLE
            }
        } else {
            for (instance in money_array.subList(12, 15)) {
                instance.box.visibility = View.GONE
            }
        }


        // add Switch onChangeListeners
        switch1?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                for (instance in money_array.subList(0, 3)) {
                    instance.box.visibility = View.VISIBLE
                }
            } else {
                for (instance in money_array.subList(0, 3)) {
                    instance.box.visibility = View.GONE
                    instance.set(0)
                }
            }

            with (persistingStorage!!.edit()) {
                putBoolean("showSmall", isChecked)
                apply()
            }
        }

        switch2?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                for (instance in money_array.subList(12, 15)) {
                    instance.box.visibility = View.VISIBLE
                }
            } else {
                for (instance in money_array.subList(12, 15)) {
                    instance.box.visibility = View.GONE
                    instance.set(0)
                }
            }

            with (persistingStorage!!.edit()) {
                putBoolean("showBig", isChecked)
                apply()
            }
        }

        var total = 0.0

        fun calcTotal() {
            total = 0.0

            for (instance in money_array){
                total += instance.totalCount * instance.value
            }

            result.text = resources.getString(string.totalString, formatMoney(total))
        }

        fun calcAfter145() {
            after145txt.text = resources.getString(string.minusBaseString, formatMoney(grundbestand))

            try {
                soll = sollnum.text.toString().toDouble()
            } catch (exception: Exception) {
                soll = 0.0
            }

            if (total < grundbestand) {
                after145.text = resources.getString(string.not_enough_for_base_level)
            } else {
                after145.text = resources.getString(string.after145String, formatMoney(total - grundbestand)) + (if (soll == 0.0) "" else (if (total - grundbestand == soll) " (= Sollwert)" else " (" + formatMoney(if (soll + grundbestand - total > 0) soll + grundbestand - total else -1 * (soll + grundbestand - total)) + (if ((soll - (total - grundbestand)) > 0) " weniger " else " mehr ") + " als Sollwert)"))
            }
        }

        fun getCurrentTotalChange(): Double {
            var currentTotal = 0.0

            for (instance in money_array) {
                currentTotal += instance.value * instance.currentCount
            }

            return currentTotal
        }

        fun calcChange() {

            if (money_array[0].totalCount == 69 && money_array[1].totalCount == 420) {

                money_array[0].set(0)
                money_array[1].set(0)

                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:dQw4w9WgXcQ"))
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=dQw4w9WgXcQ")
                )
                try {
                    context?.startActivity(appIntent)
                } catch (ex: ActivityNotFoundException) {
                    context?.startActivity(webIntent)
                }
            }

            // add one 20eur
            if (money_array[10].totalCount > 0) {
                money_array[10].currentCount++
            }

            // add two 10eur
            for (i in 0..1) {
                if (money_array[9].totalCount > i) {
                    money_array[9].currentCount++
                }
            }

            // add four 5eur
            for (i in 0..3) {
                if (money_array[8].totalCount > i) {
                    money_array[8].currentCount++
                }
            }

            // add ten of 50ct-2eur
            for (instance in money_array.subList(5, 8).reversed()) {
                for (i in 0..9) {
                    if (instance.totalCount > i) {
                        instance.currentCount++
                    }
                }
            }

            // try add ten more of 50ct-2eur
            for (i in 0..9) {
                for (instance in money_array.subList(5, 8).reversed()) {
                    if (instance.stillSomeLeft() && getCurrentTotalChange() <= grundbestand - instance.value) {
                        instance.currentCount++
                    }
                }
            }

            // Fill all
            for (instance in money_array.reversed()) {
                while (getCurrentTotalChange() <= grundbestand - instance.value && instance.stillSomeLeft()) {
                    instance.currentCount++
                }
            }

            // Generate changeString

            var changeStr = ""

            for (instance in money_array) {
                if (instance.currentCount != 0) {
                    changeStr += instance.currentCount.toString() + "×" + instance.value.toString() + "€ + "
                }
            }

            changeStr = changeStr.dropLast(3)

            changeStr += " = ${formatMoney(getCurrentTotalChange())}€"

            if (getCurrentTotalChange() != grundbestand) {changeStr += " \u2260 ${formatMoney(grundbestand)}€!!"}

            change.text = changeStr
        }

        fun clearAll(tempObject: TextWatcher){
            for (instance in money_array){
                instance.num.removeTextChangedListener(tempObject)
                instance.set(0)
                instance.num.addTextChangedListener(tempObject)
            }

            sollnum.text = ""
        }

        val tempObject = object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {

                with (persistingStorage!!.edit()) {

                    for (instance in money_array) {

                        // update new counts
                        instance.updateTotalCount()
                        instance.currentCount = 0

                        //Safe values to Android's persisting Storage
                        putString(instance.value.toString(), instance.totalCount.toString())
                    }

                    apply()
                }

                calcTotal()
                calcAfter145()
                calcChange()
            }
        }

        for (instance in money_array){
            instance.num.addTextChangedListener(tempObject)
        }
        sollnum.addTextChangedListener(tempObject)

        clear.setOnClickListener{ clearAll(tempObject) }

        clear2.setOnClickListener{ clearAll(tempObject) }

        tempObject.afterTextChanged(p0 = null)

        fun showGrundbestandUpdateDialog() {
            val builder = AlertDialog.Builder(context, style.AlertDialogCustom)
            val view = layoutInflater.inflate(layout.grundbestand_dialog, null);

            builder.setView(view)
                .setPositiveButton(Html.fromHtml("<font color='#ffffff'>Übernehmen</font>"),
                    DialogInterface.OnClickListener { _, _ ->
                        try {
                            grundbestand = view.findViewById<TextView>(R.id.newGrundbestand).text.toString().toDouble()
                            persistingStorage?.edit()?.putString("grundbestand", grundbestand.toString())?.apply()
                            calcAfter145()
                            calcChange()
                        } catch (_: Exception) {  }
                    })
            builder.create().show()
        }

        grundbestand_btn.setOnClickListener { showGrundbestandUpdateDialog() }

        return view
    }
}
