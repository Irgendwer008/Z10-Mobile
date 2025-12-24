package info.z10.z10mobile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.lang.Exception
import java.text.DecimalFormat
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.insets.ColorProtection
import androidx.core.view.insets.ProtectionLayout


class Money(val value: Double, private val numInt: Int, private val boxInt: Int, private val persistingStorage: SharedPreferences?){

    lateinit var num: TextView
    lateinit var box: ConstraintLayout

    var totalCount: Int = 0
    var currentCount: Int = 0

    init {
        money_array.add(this)
    }

    fun lateInit(viewParam: View?) {
        if (viewParam != null) {
            num = viewParam.findViewById(numInt)!!
            box = viewParam.findViewById(boxInt)!!
        }

        try {
            set(persistingStorage?.getString(this.value.toString(), "")!!.toInt())
        } catch (_: Exception) {
            set(0)
        }
    }

    fun updateTotalCount() {
        val str = this.num.text.toString()

        totalCount = if (str == "") {
            0
        } else {
            str.toInt()
        }
    }

    fun set(input: Int) {
        this.totalCount = input

        if (input <= 0) {
            this.num.text = ""
        } else {
            this.num.text = input.toString()
        }
    }

    fun stillSomeLeft(): Boolean {
        return this.currentCount < this.totalCount
    }
}

fun formatMoney(double: Double): String {
    return DecimalFormat("0.00").format(double).replace(",", ".")
}

var money_array = ArrayList<Money>()

class MainActivity : AppCompatActivity() {
    @SuppressLint("UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        findViewById<ProtectionLayout>(R.id.list_protection)
            .setProtections(
                listOf(
                    ColorProtection(
                        WindowInsetsCompat.Side.TOP,
                        getColor(R.color.accent)
                    )
                )
            )

        val persistingStorage: SharedPreferences? = this.getPreferences(MODE_PRIVATE)

        if (persistingStorage?.getBoolean("firstrun", true) == true) {
            showMaintenanceWarning()
            persistingStorage.edit()?.putBoolean("firstrun", false)?.apply()
        }

        Money(0.01, R.id.ct1num, R.id.ct1box, persistingStorage)
        Money(0.02, R.id.ct2num, R.id.ct2box, persistingStorage)
        Money(0.05, R.id.ct5num, R.id.ct5box, persistingStorage)
        Money(0.1, R.id.ct10num, R.id.ct10box, persistingStorage)
        Money(0.2, R.id.ct20num, R.id.ct20box, persistingStorage)
        Money(0.5, R.id.ct50num, R.id.ct50box, persistingStorage)
        Money(1.0, R.id.eur1num, R.id.eur1box, persistingStorage)
        Money(2.0, R.id.eur2num, R.id.eur2box, persistingStorage)
        Money(5.0, R.id.eur5num, R.id.eur5box, persistingStorage)
        Money(10.0, R.id.eur10num, R.id.eur10box, persistingStorage)
        Money(20.0, R.id.eur20num, R.id.eur20box, persistingStorage)
        Money(50.0, R.id.eur50num, R.id.eur50box, persistingStorage)
        Money(100.0, R.id.eur100num, R.id.eur100box, persistingStorage)
        Money(200.0, R.id.eur200num, R.id.eur200box, persistingStorage)
        Money(500.0, R.id.eur500num, R.id.eur500box, persistingStorage)
    }

    private fun showMaintenanceWarning() {
        @Suppress("DEPRECATION")
        AlertDialog.Builder(this, R.style.AlertDialogCustom)
            .setTitle(Html.fromHtml("<font color='#FF7600'>Info</font>"))
            .setMessage("Diese \"App\" befindet sich noch in der Entwicklung, es ist also ganz sicher noch nicht alles Perfekt. Solltest du beim Benutzen einen Fehler bemerken sag mir trotzdem gerne bescheid :)")
            .setPositiveButton(Html.fromHtml("<font color='#FF7600'>Kapisch</font>")) { _, _ -> }
            .show()
    }
}