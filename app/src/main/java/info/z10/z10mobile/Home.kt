package info.z10.z10mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.core.net.toUri

class Home : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<Button>(R.id.navButtonRechner).setOnClickListener{ view.findNavController().navigate(R.id.action_home2_to_kassenrechner)}
        view.findViewById<Button>(R.id.navButtonIdkYet).setOnClickListener{Toast.makeText(view.context, "69\nhaha i am so funny",Toast.LENGTH_LONG).show()}
        view.findViewById<Button>(R.id.madeWithLoveButton).setOnClickListener{startActivity(Intent(Intent.ACTION_VIEW, "https://wiki.z10.info/w/Benutzer:Ob".toUri()))}

        return view
    }
}