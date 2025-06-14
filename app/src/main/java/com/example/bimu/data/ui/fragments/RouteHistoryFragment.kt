package com.example.bimu.data.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
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
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBarHistory)
        val emptyText = view?.findViewById<TextView>(R.id.emptyText)

        progressBar?.visibility = View.VISIBLE
        emptyText?.visibility = View.GONE

        if (userId.isNullOrEmpty()) {
            progressBar?.visibility = View.GONE
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
            progressBar?.visibility = View.GONE
            emptyText?.visibility = if (routeList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun onRouteSelected(route: Route) {
        val fragment = RouteDetailFragment().apply {
            arguments = Bundle().apply { putString("routeId", route._id) }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
