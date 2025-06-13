package com.example.bimu.data.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bimu.R
import com.example.bimu.data.dao.OutingDAO
import com.example.bimu.data.dao.RouteDAO
import com.example.bimu.data.models.AuxClass
import com.example.bimu.data.models.Route
import com.example.bimu.data.models.RouteAdapter
import com.example.bimu.data.network.ApiClient
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RouteHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RouteHistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RouteAdapter
    private val outingDao = OutingDAO(ApiClient.outingApi)
    private val routeDao = RouteDAO(ApiClient.routeApi)
    private val aux = AuxClass()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_route_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewHistory)
        adapter = RouteAdapter { route -> onRouteSelected(route) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        loadHistory()
    }

    private fun loadHistory() {
        val userId = aux.getUserIdFromPrefs(requireContext())
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Haz login", Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val routes = outingDao.getRoutesByUserId(userId)
            val routeList = mutableListOf<Route>()
            for (id in routes) {
                val route = routeDao.getRouteById(id)
                if (route != null) routeList.add(route)
            }
            adapter.submitList(routeList)
        }
    }

    private fun onRouteSelected(route: Route) {
        val fragment = RouteDetailFragment().apply {
            arguments = Bundle().apply { putString("routeId", route._id) }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Usa el id real de tu contenedor de fragments
            .addToBackStack(null)
            .commit()
    }
}
