package com.example.bimu.data.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bimu.R
import com.example.bimu.data.dao.OutingDAO
import com.example.bimu.data.dao.RouteDAO
import com.example.bimu.data.models.AuxClass
import com.example.bimu.data.models.Outing
import com.example.bimu.data.models.Route
import com.example.bimu.data.network.ApiClient
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RouteDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RouteDetailFragment : Fragment() {

    private lateinit var textName: TextView
    private lateinit var textDescription: TextView
    private lateinit var textDifficulty: TextView
    private lateinit var textDate: TextView
    private lateinit var textCreator: TextView
    private lateinit var buttonEdit: Button
    private lateinit var buttonDelete: Button
    private lateinit var buttonJoin: Button

    private val routeDao = RouteDAO(ApiClient.routeApi)
    private val outingDao = OutingDAO(ApiClient.outingApi)
    private val aux = AuxClass()
    private var route: Route? = null
    private var userId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_route_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textName = view.findViewById(R.id.textViewRouteName)
        textDescription = view.findViewById(R.id.textViewRouteDescription)
        textDifficulty = view.findViewById(R.id.textViewRouteDifficulty)
        textDate = view.findViewById(R.id.textViewRouteDate)
        textCreator = view.findViewById(R.id.textViewRouteCreator)
        buttonEdit = view.findViewById(R.id.buttonEdit)
        buttonDelete = view.findViewById(R.id.buttonDelete)
        buttonJoin = view.findViewById(R.id.buttonJoin)

        userId = aux.getUserIdFromPrefs(requireContext())

        val routeId = arguments?.getString("routeId")
        if (routeId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Ruta no encontrada", Toast.LENGTH_SHORT).show()
            return
        }

        loadRoute(routeId)
    }

    private fun loadRoute(routeId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            route = routeDao.getRouteById(routeId)
            route?.let { routeData ->
                textName.text = routeData.title
                textDescription.text = routeData.description
                textDifficulty.text = routeData.difficulty
                textDate.text = routeData.timeStart
                textCreator.text = "Creador: Ángel"
                val isCreator = routeData.creatorId == userId
                buttonEdit.visibility = if (isCreator) View.VISIBLE else View.GONE
                buttonDelete.visibility = if (isCreator) View.VISIBLE else View.GONE
                buttonJoin.visibility = if (!isCreator) View.VISIBLE else View.GONE

                buttonEdit.setOnClickListener { editRoute(routeData) }
                buttonDelete.setOnClickListener { deleteRoute(routeData) }
                buttonJoin.setOnClickListener { joinRoute(routeData) }
            }
        }
    }

    private fun editRoute(route: Route) {
        val fragment = RouteEditFragment()
        fragment.arguments = Bundle().apply { putString("routeId", route._id) }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun deleteRoute(route: Route) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = routeDao.deleteRoute(route._id.toString())
            if (result) {
                Toast.makeText(requireContext(), "Ruta eliminada", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "No se pudo eliminar la ruta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun joinRoute(route: Route) {
        val userIdVal = userId
        if (userIdVal.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Error: usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }
        val outing = Outing(
            userId = userIdVal,
            routeId = route._id.toString(),
            // Añade aquí más campos si tu modelo Outing los requiere (fecha, estado, etc)
        )
        viewLifecycleOwner.lifecycleScope.launch {
            val result = outingDao.addOuting(outing)
            if (result != null) {
                Toast.makeText(requireContext(), "¡Te has unido a la ruta!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "No se pudo unir a la ruta", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
