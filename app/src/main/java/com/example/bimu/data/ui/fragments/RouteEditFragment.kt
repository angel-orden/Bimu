package com.example.bimu.data.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import androidx.lifecycle.lifecycleScope
import com.example.bimu.R
import com.example.bimu.data.dao.RouteDAO
import com.example.bimu.data.models.AuxClass
import com.example.bimu.data.models.GeoPoint
import com.example.bimu.data.models.Route
import com.example.bimu.data.network.ApiClient
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import com.example.bimu.data.dao.OutingDAO
import com.example.bimu.data.models.Outing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RouteEditFragment : Fragment() {

    private lateinit var editTextName: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var editTextDate: EditText
    private lateinit var buttonSave: Button

    private lateinit var osmMapView: MapView
    private var selectedPoint: GeoPoint? = null
    private var marker: Marker? = null

    private val routeDao = RouteDAO(ApiClient.routeApi)
    private val outingDao = OutingDAO(ApiClient.outingApi)
    private val aux = AuxClass()
    private var route: Route? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_route_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        editTextName = view.findViewById(R.id.editTextName)
        editTextDescription = view.findViewById(R.id.editTextDescription)
        spinnerDifficulty = view.findViewById(R.id.spinnerDifficulty)
        editTextDate = view.findViewById(R.id.editTextDate)
        buttonSave = view.findViewById(R.id.buttonSave)

        // Inicializar osmdroid al inicio para evitar errores
        org.osmdroid.config.Configuration.getInstance().load(
            requireContext(),
            android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        osmMapView = view.findViewById(R.id.osmMapView)
        osmMapView.setMultiTouchControls(true)
        val defaultPoint = OsmGeoPoint(40.4168, -3.7038) // Madrid por defecto
        osmMapView.controller.setZoom(12.0)
        osmMapView.controller.setCenter(defaultPoint)

        val calendar = Calendar.getInstance()

        // Cargar opciones de dificultad
        val difficulties = listOf("Novato", "Principiante", "Intermedio", "Avanzado")
        spinnerDifficulty.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, difficulties)

        // ¿Es edición?
        val routeId = arguments?.getString("routeId")
        if (!routeId.isNullOrEmpty()) {
            loadRoute(routeId)
        }

        //Picker para la fecha
        editTextDate.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    // Al elegir fecha, ahora lanza TimePicker
                    val timePicker = TimePickerDialog(requireContext(),
                        { _, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                            // Guarda fecha y hora en formato ISO o personalizado
                            val fechaHora = String.format("%04d-%02d-%02d %02d:%02d",
                                year, month + 1, dayOfMonth, hourOfDay, minute)
                            editTextDate.setText(fechaHora)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true)
                    timePicker.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        //Lógica de pulsación en el mapa
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: OsmGeoPoint): Boolean {
                putMarker(p)
                return true
            }
            override fun longPressHelper(p: OsmGeoPoint): Boolean = false
        }
        val overlay = MapEventsOverlay(mapEventsReceiver)
        osmMapView.overlays.add(overlay)

        buttonSave.setOnClickListener { saveRoute() }
    }

    private fun putMarker(point: OsmGeoPoint) {
        // Quitar marcador anterior si existe
        marker?.let { osmMapView.overlays.remove(it) }
        marker = Marker(osmMapView).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Punto de inicio"
        }
        osmMapView.overlays.add(marker)
        osmMapView.invalidate()
        selectedPoint = GeoPoint(point.latitude, point.longitude)
    }

    private fun loadRoute(routeId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                route = routeDao.getRouteById(routeId)
                route?.let {
                    editTextName.setText(it.title)
                    editTextDescription.setText(it.description)
                    val position = (spinnerDifficulty.adapter as ArrayAdapter<String>).getPosition(it.difficulty ?: "Intermedio")
                    spinnerDifficulty.setSelection(position)
                    editTextDate.setText(it.timeStart)

                    //Si tiene localización ya, centra el mapa en el punto
                    it.locationStart?.let { geo ->
                        val point = OsmGeoPoint(geo.latitude, geo.longitude)
                        putMarker(point)
                        osmMapView.controller.setCenter(point)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error cargando ruta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveRoute() {
        val name = editTextName.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val difficulty = spinnerDifficulty.selectedItem.toString()
        val date = editTextDate.text.toString().trim()
        val creatorId = aux.getUserIdFromPrefs(requireContext()) ?: return

        if (name.isEmpty() || difficulty.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor rellena los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Preparado para añadir más campos en caso de necesidad
        val routeToSave = route?.copy(
            title = name,
            description = description,
            difficulty = difficulty,
            timeStart = date
        ) ?: Route(
            title = name,
            description = description,
            difficulty = difficulty,
            timeStart = date,
            creatorId = creatorId,
            locationStart = GeoPoint(0.0, 0.0)
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val saved = if (route == null) {
                    val rutaCreada= routeDao.addRoute(routeToSave)
                    if(rutaCreada != null){
                        val outing = Outing(
                            _id = null,
                            completed = false,                // Puedes poner true si la quieres como finalizada de inicio
                            joinedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                            notes = null,                     // O alguna nota por defecto
                            routeId = rutaCreada._id.toString(),
                            userId = creatorId
                        )
                        outingDao.addOuting(outing)
                    }
                    Log.d("BIMU", "Ruta creada: $rutaCreada")
                } else {
                    Log.d("BIMU", "Editando ruta con id: ${routeToSave._id}")
                    val editedRoute = routeDao.editRoute(routeToSave._id.toString(), mapOf(
                        "title" to name,
                        "description" to description,
                        "difficulty" to difficulty,
                        "timeStart" to date
                    ))
                    if (editedRoute != null) {
                        val creatorOuting = outingDao.getUserOuting(creatorId, editedRoute._id.toString())
                        if (creatorOuting == null) {
                            val outing = Outing(
                                _id = null,
                                completed = false,
                                joinedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                                notes = null,
                                routeId = routeToSave._id.toString(),
                                userId = creatorId)
                            outingDao.addOuting(outing)
                        }else{
                           Log.d("BIMU", "Ya está apuntado a la rutaS")
                        }
                    }else{
                        Log.d("BIMU", "Ruta no encontrada")
                    }
                }
                if (saved != null) {
                    Toast.makeText(requireContext(), "Ruta guardada", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar la ruta", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error guardando ruta: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

