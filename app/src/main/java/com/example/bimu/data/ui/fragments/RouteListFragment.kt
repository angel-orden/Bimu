package com.example.bimu.data.ui.fragments

import RouteFilterDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bimu.R
import com.example.bimu.data.dao.RouteDAO
import com.example.bimu.data.dao.UserDAO
import com.example.bimu.data.models.AuxClass
import com.example.bimu.data.models.GeoPoint
import com.example.bimu.data.models.Route
import com.example.bimu.data.models.RouteAdapter
import com.example.bimu.data.models.RouteSearchParams
import com.example.bimu.data.models.User
import com.example.bimu.data.network.ApiClient
import kotlinx.coroutines.launch

class RouteListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var routeAdapter: RouteAdapter
    private lateinit var filterButton: Button
    private lateinit var addButton: Button
    private var filterDialog: RouteFilterDialog? = null
    private var lastSearchParams: RouteSearchParams? = null

    private val routeDao = RouteDAO(ApiClient.routeApi)
    private var routeList: List<Route> = emptyList()
    private val aux = AuxClass()
    private val userDao = UserDAO(ApiClient.userApi)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_route_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewRoutes)
        filterButton = view.findViewById(R.id.buttonFilter)
        addButton = view.findViewById(R.id.buttonAddRoute)

        routeAdapter = RouteAdapter { route -> onRouteSelected(route) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = routeAdapter

        filterButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                if (filterDialog == null) {
                    // Si no hay filtro anterior, se carga del perfil del usuario
                    val userId = aux.getUserIdFromPrefs(requireContext())
                    val user: User? = userDao.getUserById(userId ?: "")
                    lastSearchParams = lastSearchParams ?: if (user != null && user.centralPoint != null) {
                        RouteSearchParams(
                            location = user.centralPoint,
                            radiusKm = user.radius ?: 25.0,
                            difficulty = user.level,
                            fromDate = null,
                            toDate = null
                        )
                    } else {
                        RouteSearchParams(
                            location = GeoPoint(40.4168, -3.7038),
                            radiusKm = 25.0,
                            difficulty = "Intermedio",
                            fromDate = null,
                            toDate = null
                        )
                    }

                    filterDialog = RouteFilterDialog(
                        requireContext(),
                        lastSearchParams
                    ) { filterParams ->
                        lastSearchParams = filterParams
                        loadRoutesWithFilters(filterParams)
                    }
                } else {
                    filterDialog?.setInitialParams(lastSearchParams)
                }
                filterDialog?.show()
            }
        }
        addButton.setOnClickListener {
            val fragment = RouteEditFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment) // Usa el ID de tu FrameLayout principal
                .addToBackStack(null)
                .commit()
        }

        loadRoutesWithUserPrefs()
    }

    fun loadRoutesWithFilters(params: RouteSearchParams) {
        lastSearchParams = params
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                routeList = routeDao.searchRoutes(params)
                routeAdapter.submitList(routeList)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al buscar rutas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadRoutesWithUserPrefs() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userId = aux.getUserIdFromPrefs(requireContext()).toString()
                val user: User? = userDao.getUserById(userId)
                lastSearchParams = if (user != null && user.centralPoint != null && !user.level.isNullOrEmpty()) {
                    RouteSearchParams(
                        location = user.centralPoint,
                        radiusKm = user.radius ?: 25.0,
                        difficulty = user.level // debe ser un String: "Novato", "Intermedio", etc.
                    )
                } else {
                    RouteSearchParams(
                        location = GeoPoint(40.4168, -3.7038),
                        radiusKm = 25.0,
                        difficulty = "Intermedio"
                    )
                }
                Log.d("BIMU", "Lanzando búsqueda automática de rutas con params: $lastSearchParams")
                routeList = routeDao.searchRoutes(lastSearchParams!!)
                Log.d("BIMU", "Rutas encontradas: ${routeList.size}")
                routeAdapter.submitList(routeList)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error al cargar rutas: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun onRouteSelected(route: Route) {
        val fragment = RouteDetailFragment()
        fragment.arguments = Bundle().apply { putString("routeId", route._id) }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Usa el ID de tu FrameLayout principal
            .addToBackStack(null)
            .commit()
    }
}
