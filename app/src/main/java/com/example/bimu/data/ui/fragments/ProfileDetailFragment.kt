package com.example.bimu.data.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bimu.R
import com.example.bimu.data.models.User
import com.example.bimu.data.models.Route
import com.example.bimu.data.models.Outing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.example.bimu.data.dao.OutingDAO
import com.example.bimu.data.dao.RouteDAO
import com.example.bimu.data.dao.UserDAO
import com.example.bimu.data.models.AuxClass
import com.example.bimu.data.network.ApiClient
import com.example.bimu.data.ui.LoginActivity


class ProfileDetailFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var imageAvatar: ImageView
    private lateinit var textUsername: TextView
    private lateinit var textLevel: TextView
    private lateinit var textAge: TextView
    private lateinit var textGender: TextView
    private lateinit var textCountry: TextView
    private lateinit var textZone: TextView
    private lateinit var textBio: TextView
    private lateinit var pieChart: PieChart
    private lateinit var buttonEdit: Button
    private lateinit var buttonLogout: Button

    // DAOs
    private val routeDao = RouteDAO(ApiClient.routeApi)
    private val outingDao = OutingDAO(ApiClient.outingApi)
    private val userDao = UserDAO(ApiClient.userApi)
    private val aux = AuxClass()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBarProfile)
        imageAvatar = view.findViewById(R.id.imageAvatar)
        textUsername = view.findViewById(R.id.textUsername)
        textLevel = view.findViewById(R.id.textLevel)
        textAge = view.findViewById(R.id.textAge)
        textGender = view.findViewById(R.id.textGender)
        textCountry = view.findViewById(R.id.textCountry)
        textZone = view.findViewById(R.id.textZone)
        textBio = view.findViewById(R.id.textBio)
        pieChart = view.findViewById(R.id.profilePieChart)
        buttonEdit = view.findViewById(R.id.buttonEdit)
        buttonLogout = view.findViewById(R.id.buttonLogout)

        loadProfile()
    }

    private fun loadProfile() {
        progressBar.visibility = View.VISIBLE
        val userId = aux.getUserIdFromPrefs(requireContext())
        if (userId == null) {
            Toast.makeText(context, "No se ha iniciado sesión", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }
        lifecycleScope.launch {
            val user = userDao.getUserById(userId)
            if (user != null) {
                updateUI(user)
                // Cargar estadísticas del usuario (por ejemplo, rutas completadas por dificultad)
                loadStats(userId)
            } else {
                Toast.makeText(context, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show()
            }
            progressBar.visibility = View.GONE
        }
    }

    private fun updateUI(user: User) {
        // Cargar avatar con Glide (si tienes URL, si no usa imagen por defecto)
        if (!user.avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(imageAvatar)
        }
        textUsername.text = user.username
        textLevel.text = user.level ?: ""
        textAge.text = "Edad: ${user.age ?: "?"}"
        textGender.text = "Género: ${user.gender ?: "?"}"
        textCountry.text = "País: ${user.country ?: "?"}"
        val radius = user.radius ?: 25
        textZone.text = "Zona favorita: ${radius} km"
        textBio.text = user.bio ?: ""

        // Botones
        buttonEdit.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
        buttonLogout.setOnClickListener {
            // Borra los datos de sesión
            aux.clearUserIdFromPrefs(requireContext())
            // Navega a la pantalla de inicio/login y cierra la actividad actual
            val intent = Intent(requireContext(), LoginActivity::class.java) // O StartActivity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadStats(userId: String) {
        lifecycleScope.launch {
            val outingRouteIds = outingDao.getRoutesByUserId(userId)
            val routeList = outingRouteIds.mapNotNull { routeDao.getRouteById(it) }
            // Agrupa rutas por dificultad
            val dificultadMap = routeList.groupingBy { it.difficulty ?: "?" }.eachCount()
            showPieChart(dificultadMap)
        }
    }

    private fun showPieChart(dificultadMap: Map<String, Int>) {
        val entries = dificultadMap.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "Rutas por dificultad")
        dataSet.setDrawValues(true)
        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.centerText = "Tus rutas"
        pieChart.invalidate()
    }
}
