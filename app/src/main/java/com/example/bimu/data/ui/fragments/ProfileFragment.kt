package com.example.bimu.data.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bimu.R
import com.example.bimu.data.dao.UserDAO
import com.example.bimu.data.models.AuxClass
import com.example.bimu.data.models.GeoPoint
import com.example.bimu.data.models.User
import com.example.bimu.data.network.ApiClient
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint as OsmdroidGeoPoint

class ProfileFragment : Fragment() {
    private lateinit var userDao: UserDAO
    private lateinit var aux: AuxClass
    private var user: User? = null
    private var selectedGeoPoint: GeoPoint? = null
    private var selectedProfileImageUri: Uri? = null

    // UI components
    private lateinit var imageViewProfile: ImageView
    private lateinit var buttonSelectProfileImage: Button
    private lateinit var editTextUsername: EditText
    private lateinit var editTextBio: EditText
    private lateinit var editTextAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerLevel: Spinner
    private lateinit var editTextRadius: EditText
    private lateinit var buttonUseCurrentLocation: Button
    private lateinit var mapView: org.osmdroid.views.MapView
    private lateinit var textViewLocation: TextView
    private lateinit var buttonSave: Button

    private val genders = listOf("Hombre", "Mujer", "Otro")
    private val levels = listOf("Novato", "Principiante", "Intermedio", "Avanzado")

    private val SELECT_IMAGE_REQUEST_CODE = 101

    // === NUEVO: Launcher para permisos de localización ===
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        // Comprobar explícitamente de nuevo (por si acaso) ANTES de llamar a la función
        val context = requireContext()
        if (fineLocationGranted || coarseLocationGranted) {
            if (
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                centerMapOnUserLocation()
            } else {
                Toast.makeText(context, "Permiso de ubicación no concedido.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aux = AuxClass()
        userDao = UserDAO(ApiClient.userApi)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageViewProfile = view.findViewById(R.id.imageViewProfile)
        buttonSelectProfileImage = view.findViewById(R.id.buttonSelectProfileImage)
        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextBio = view.findViewById(R.id.editTextBio)
        editTextAge = view.findViewById(R.id.editTextAge)
        spinnerGender = view.findViewById(R.id.spinnerGender)
        spinnerLevel = view.findViewById(R.id.spinnerLevel)
        editTextRadius = view.findViewById(R.id.editTextRadius)
        buttonUseCurrentLocation = view.findViewById(R.id.buttonUseCurrentLocation)
        mapView = view.findViewById(R.id.osmMapView)
        textViewLocation = view.findViewById(R.id.textViewLocation)
        buttonSave = view.findViewById(R.id.buttonSave)

        spinnerGender.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genders)
        spinnerLevel.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, levels)

        // Configuración de osmdroid
        val ctx = requireContext().applicationContext
        Configuration.getInstance().userAgentValue = requireContext().packageName
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        setupMapTouchOverlay()

        buttonSelectProfileImage.setOnClickListener { selectImageFromGallery() }
        buttonSave.setOnClickListener { guardarCambios() }
        buttonUseCurrentLocation.setOnClickListener { requestLocationPermissionAndCenterMap() }

        cargarUsuario()
    }

    private fun setupMapTouchOverlay() {
        mapView.overlays.clear()
        val mapEventsOverlay = object : org.osmdroid.views.overlay.Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: org.osmdroid.views.MapView): Boolean {
                val projection = mapView.projection
                val iGeoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as OsmdroidGeoPoint
                selectedGeoPoint = GeoPoint(iGeoPoint.latitude, iGeoPoint.longitude)
                mapView.overlays.clear()
                val marker = Marker(mapView)
                marker.position = iGeoPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(marker)
                mapView.invalidate()
                textViewLocation.text = "Latitud: %.5f, Longitud: %.5f".format(iGeoPoint.latitude, iGeoPoint.longitude)
                return true
            }
        }
        mapView.overlays.add(mapEventsOverlay)
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE)
    }

    private fun cargarUsuario() {
        val userId = aux.getUserIdFromPrefs(requireContext())
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No hay usuario logueado. Haz login de nuevo.", Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val fetchedUser = userDao.getUserById(userId)
                user = fetchedUser
                if (fetchedUser != null) {
                    populateUIWithUserData(fetchedUser)
                } else {
                    Toast.makeText(requireContext(), "Perfil de usuario no encontrado.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar perfil: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateUIWithUserData(currentUserData: User) {
        editTextUsername.setText(currentUserData.username ?: "")
        editTextBio.setText(currentUserData.bio ?: "")
        editTextAge.setText(currentUserData.age?.toString() ?: "")

        val genderIndex = genders.indexOf(currentUserData.gender)
        spinnerGender.setSelection(if (genderIndex != -1) genderIndex else genders.indexOf("Otro"))

        val levelIndex = levels.indexOf(currentUserData.level)
        spinnerLevel.setSelection(if (levelIndex != -1) levelIndex else levels.indexOf("Novato"))

        editTextRadius.setText(currentUserData.radius?.toString() ?: "5.0")

        currentUserData.centralPoint?.let { gp ->
            selectedGeoPoint = gp
            val osmGeo = OsmdroidGeoPoint(gp.latitude, gp.longitude)
            mapView.controller.setCenter(osmGeo)
            mapView.overlays.clear()
            val marker = Marker(mapView)
            marker.position = osmGeo
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
            mapView.invalidate()
            textViewLocation.text = "Latitud: %.5f, Longitud: %.5f".format(gp.latitude, gp.longitude)
        } ?: run {
            centerMapOnDefault()
        }

        currentUserData.avatarUrl?.let { uriStr ->
            if (uriStr.isNotBlank()) {
                try {
                    Glide.with(this)
                        .load(Uri.parse(uriStr))
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_error)
                        .circleCrop()
                        .into(imageViewProfile)
                } catch (e: Exception) {
                    imageViewProfile.setImageResource(R.drawable.ic_profile_error)
                }
            } else {
                imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder)
            }
        } ?: imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder)
    }

    private fun centerMapOnDefault() {
        val defaultLat = 40.4168
        val defaultLon = -3.7038
        mapView.controller.setCenter(OsmdroidGeoPoint(defaultLat, defaultLon))
        textViewLocation.text = "Toca el mapa o usa tu ubicación actual"
    }

    private fun requestLocationPermissionAndCenterMap() {
        val context = requireContext()
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            centerMapOnUserLocation()
        }
    }

    private fun centerMapOnUserLocation() {
        val context = requireContext()
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Permiso de ubicación no concedido.", Toast.LENGTH_SHORT).show()
            return
        }

        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager

        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        val lat = location?.latitude
        val lon = location?.longitude

        if (lat != null && lon != null) {
            val geoPoint = OsmdroidGeoPoint(lat, lon)
            mapView.controller.setCenter(geoPoint)
            mapView.overlays.clear()
            val marker = Marker(mapView)
            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
            mapView.invalidate()
            selectedGeoPoint = GeoPoint(lat, lon)
            textViewLocation.text = "Latitud: %.5f, Longitud: %.5f".format(lat, lon)
        } else {
            Toast.makeText(context, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarCambios() {
        val userId = aux.getUserIdFromPrefs(requireContext())
        if (userId.isNullOrEmpty() || user == null) {
            Toast.makeText(requireContext(), "No hay usuario cargado para guardar.", Toast.LENGTH_SHORT).show()
            return
        }

        val username = editTextUsername.text.toString().trim()
        val bio = editTextBio.text.toString().trim()
        val ageStr = editTextAge.text.toString()
        val radiusStr = editTextRadius.text.toString()

        if (username.isBlank()) {
            editTextUsername.error = "El nombre de usuario no puede estar vacío"
            Toast.makeText(requireContext(), "El nombre de usuario es obligatorio.", Toast.LENGTH_SHORT).show()
            return
        }
        val age = ageStr.toIntOrNull()
        if (ageStr.isNotBlank() && age == null) {
            editTextAge.error = "Edad inválida"
            Toast.makeText(requireContext(), "Por favor, introduce una edad válida.", Toast.LENGTH_SHORT).show()
            return
        }
        if (age != null && (age < 0 || age > 120)) {
            editTextAge.error = "Edad fuera de rango (0-120)"
            Toast.makeText(requireContext(), "Por favor, introduce una edad realista.", Toast.LENGTH_SHORT).show()
            return
        }
        val radius = radiusStr.toDoubleOrNull() ?: 5.0
        if (radius <= 0) {
            editTextRadius.error = "El radio debe ser positivo"
            Toast.makeText(requireContext(), "El radio de búsqueda debe ser un valor positivo.", Toast.LENGTH_SHORT).show()
            return
        }

        val fields = mutableMapOf<String, Any>(
            "username" to username,
            "bio" to bio,
            "age" to (age ?: 0),
            "gender" to genders[spinnerGender.selectedItemPosition],
            "level" to levels[spinnerLevel.selectedItemPosition],
            "radius" to radius,
        )
        selectedGeoPoint?.let {
            fields["centralPoint"] = it
        }
        // Si tienes subida de imagen a backend, añade "avatarUrl" al map.

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val updatedUser = userDao.editUser(userId, fields)
                if (updatedUser != null) {
                    Toast.makeText(requireContext(), "Perfil actualizado correctamente.", Toast.LENGTH_SHORT).show()
                    user = updatedUser
                    populateUIWithUserData(updatedUser)
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar el perfil.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al guardar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                Log.e("ProfileFragment", "Error al guardar perfil", e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedProfileImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_error)
                    .circleCrop()
                    .into(imageViewProfile)
                // Para guardar la imagen realmente, deberías subirla al backend y guardar la URL.
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
    }
}