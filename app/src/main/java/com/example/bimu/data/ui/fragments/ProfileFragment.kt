package com.example.bimu.data.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.text
import androidx.compose.ui.tooling.data.position
import androidx.wear.compose.material.placeholder
// Importa tus clases UserDAO, AuxClass, GeoPoint, User
import com.bumptech.glide.Glide
import com.example.bimu.R
import com.example.bimu.data.dao.UserDAO
import com.example.bimu.data.models.AuxClass
import com.example.bimu.data.models.GeoPoint // Asegúrate que este es tu modelo GeoPoint
import com.example.bimu.data.models.User
import io.realm.kotlin.Realm
import io.realm.kotlin.mongodb.App // Si AuxClass lo necesita, o donde inicialices UserDAO
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.overlay.Marker // Para el marcador del mapa
import org.osmdroid.util.GeoPoint as OsmdroidGeoPoint // Alias para el GeoPoint de OSMdroid


class ProfileFragment : Fragment() {

    private lateinit var userDao: UserDAO
    private lateinit var aux: AuxClass

    private var user: User? = null
    private var selectedGeoPoint: GeoPoint? = null
    private var selectedProfileImageUri: Uri? = null

    //Inicializamos la variable realm para poder realizar operaciones fuera del hilo principal
    private lateinit var realm: Realm

    private lateinit var imageViewProfile: ImageView
    private lateinit var buttonSelectProfileImage: Button
    private lateinit var editTextUsername: EditText
    private lateinit var editTextBio: EditText
    private lateinit var editTextAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerLevel: Spinner
    private lateinit var editTextRadius: EditText
    private lateinit var mapView: org.osmdroid.views.MapView
    private lateinit var textViewLocation: TextView
    private lateinit var buttonSave: Button

    private val genders = listOf("Hombre", "Mujer", "Otro")
    private val levels = listOf("Novato", "Principiante", "Intermedio", "Avanzado")

    private val SELECT_IMAGE_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializamos userDao y aux
        val appId = "bimu-app-wyaznyg"
        val realmApp = App.create(appId)
        realm= Realm.open(SyncConfiguration.Builder(realmApp.currentUser!!, setOf(User::class)).build())
        userDao = UserDAO(realm)
        aux = AuxClass()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Buena práctica llamar a super

        imageViewProfile = view.findViewById(R.id.imageViewProfile)
        buttonSelectProfileImage = view.findViewById(R.id.buttonSelectProfileImage)
        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextBio = view.findViewById(R.id.editTextBio)
        editTextAge = view.findViewById(R.id.editTextAge)
        spinnerGender = view.findViewById(R.id.spinnerGender)
        spinnerLevel = view.findViewById(R.id.spinnerLevel)
        editTextRadius = view.findViewById(R.id.editTextRadius)
        mapView = view.findViewById(R.id.mapView)
        textViewLocation = view.findViewById(R.id.textViewLocation)
        buttonSave = view.findViewById(R.id.buttonSave)

        spinnerGender.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genders)
        spinnerLevel.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, levels)

        val ctx = requireContext().applicationContext
        Configuration.getInstance().userAgentValue = requireContext().packageName
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        mapView.overlays.clear()
        val mapEventsOverlay = object : org.osmdroid.views.overlay.Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: org.osmdroid.views.MapView): Boolean {
                val projection = mapView.projection
                val iGeoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as OsmdroidGeoPoint
                selectedGeoPoint = GeoPoint(iGeoPoint.latitude, iGeoPoint.longitude) // Usar tu clase GeoPoint
                mapView.overlays.clear() // Limpia marcadores anteriores antes de añadir uno nuevo
                val marker = Marker(mapView)
                marker.position = iGeoPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(marker)
                mapView.invalidate()
                textViewLocation.text = "Latitud: ${"%.5f".format(iGeoPoint.latitude)}, Longitud: ${"%.5f".format(iGeoPoint.longitude)}"
                return true
            }
        }
        mapView.overlays.add(mapEventsOverlay)

        buttonSelectProfileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE)
        }

        // **AÑADIDO: Listener para el botón de guardar**
        buttonSave.setOnClickListener {
            guardarCambios()
        }

        // **AÑADIDO: Cargar datos del usuario al crear la vista**
        if (::userDao.isInitialized && ::aux.isInitialized) { // Comprobación adicional
            cargarUsuario()
        } else {
            Toast.makeText(requireContext(), "No se pudieron cargar los datos del usuario. DAO no inicializado.", Toast.LENGTH_LONG).show()
        }
    }

    private fun cargarUsuario() {
        // Asegurarse de que aux y userDao están disponibles
        if (!::aux.isInitialized || !::userDao.isInitialized) {
            Toast.makeText(requireContext(), "Error: AuxClass o UserDAO no están inicializados.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = aux.getUserIdFromPrefs(requireContext())
        if (userId == null) {
            Toast.makeText(requireContext(), "No se pudo obtener el ID del usuario.", Toast.LENGTH_SHORT).show()
            // Podrías querer redirigir al login o manejar este caso de otra forma

            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                user = userDao.getById(userId)
                user?.let { u ->
                    editTextUsername.setText(u.username)
                    editTextBio.setText(u.bio ?: "")
                    editTextAge.setText(u.age?.toString() ?: "")

                    val genderIndex = genders.indexOf(u.gender)
                    spinnerGender.setSelection(if (genderIndex != -1) genderIndex else genders.indexOf("Otro"))

                    val levelIndex = levels.indexOf(u.level)
                    spinnerLevel.setSelection(if (levelIndex != -1) levelIndex else levels.indexOf("Novato"))

                    editTextRadius.setText(u.radius?.toString() ?: "5.0")

                    u.centralPoint?.let { gp ->
                        selectedGeoPoint = gp // Actualiza el GeoPoint seleccionado
                        val osmGeo = OsmdroidGeoPoint(gp.latitude, gp.longitude)
                        mapView.controller.setCenter(osmGeo)
                        mapView.controller.animateTo(osmGeo, 17.0, 1000L) // Zoom y animación

                        // Añadir marcador para la ubicación cargada
                        mapView.overlays.clear() // Limpiar marcadores existentes
                        val marker = Marker(mapView)
                        marker.position = osmGeo
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(marker)
                        mapView.invalidate()
                        textViewLocation.text = "Latitud: ${"%.5f".format(gp.latitude)}, Longitud: ${"%.5f".format(gp.longitude)}"
                    } ?: run {
                        // Si no hay punto central guardado, podrías centrar en una ubicación por defecto
                        // o intentar obtener la ubicación actual del usuario aquí.
                        textViewLocation.text = "Toca el mapa para seleccionar tu ubicación"
                    }

                    u.avatarUrl?.let { uriStr ->
                        if (uriStr.isNotBlank()) {
                            try {
                                Glide.with(this@ProfileFragment)
                                    .load(Uri.parse(uriStr))
                                    .placeholder(R.drawable.ic_profile_placeholder) // Añade un placeholder
                                    .error(R.drawable.ic_profile_error) // Añade una imagen de error
                                    .into(imageViewProfile)
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Error al cargar imagen de perfil", Toast.LENGTH_SHORT).show()
                                imageViewProfile.setImageResource(R.drawable.ic_profile_error) // Imagen de error por defecto
                            }
                        } else {
                            imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder) // Placeholder si la URL está vacía
                        }
                    } ?: imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder) // Placeholder si la URL es nula

                } ?: run {
                    Toast.makeText(requireContext(), "Usuario no encontrado.", Toast.LENGTH_SHORT).show()
                    // Podrías querer deshabilitar campos o manejar este caso
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar datos del usuario: ${e.message}", Toast.LENGTH_LONG).show()
                // Manejar el error, quizás mostrar valores por defecto o deshabilitar la UI.
            }
        }
    }

    private fun guardarCambios() {
        // Validación básica (puedes añadir más validaciones)
        val username = editTextUsername.text.toString()
        val ageStr = editTextAge.text.toString()

        if (username.isBlank()) {
            editTextUsername.error = "El nombre de usuario no puede estar vacío"
            Toast.makeText(requireContext(), "El nombre de usuario es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageStr.toIntOrNull()
        if (ageStr.isNotBlank() && age == null) {
            editTextAge.error = "Edad inválida"
            Toast.makeText(requireContext(), "Por favor, introduce una edad válida", Toast.LENGTH_SHORT).show()
            return
        }
        if (age != null && (age < 0 || age > 150)) { // Ejemplo de rango de edad
            editTextAge.error = "Edad fuera de rango"
            Toast.makeText(requireContext(), "Por favor, introduce una edad realista", Toast.LENGTH_SHORT).show()
            return
        }

        user?.let { u ->
            u.username = username
            u.bio = editTextBio.text.toString().trim()
            if (age != null) {
                u.age = age
            }
            u.gender = genders[spinnerGender.selectedItemPosition]
            u.level = levels[spinnerLevel.selectedItemPosition]
            u.radius = editTextRadius.text.toString().toDoubleOrNull() ?: 5.0 // Valor por defecto si es inválido
            u.centralPoint = selectedGeoPoint // Se actualiza al tocar el mapa

            selectedProfileImageUri?.let {
                u.avatarUrl = it.toString()
            }

            CoroutineScope(Dispatchers.Main).launch { // Usar Main para iniciar, IO se usa dentro de UserDAO.update si es suspend
                try {
                    val success = userDao.update(u)
                    if (success) {
                        Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error: No se pudo actualizar el perfil (usuario no encontrado o error de BD)", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Excepción al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } ?: run {
            Toast.makeText(requireContext(), "No hay datos de usuario para guardar.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedProfileImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_profile_placeholder) // Placeholder
                    .error(R.drawable.ic_profile_error) // Error
                    .circleCrop() // Opcional: para hacer la imagen circular
                    .into(imageViewProfile)
            }
        }
    }

    // Es buena práctica liberar recursos del mapa en onPause y recargarlos en onResume
    override fun onResume() {
        super.onResume()
        mapView.onResume() // Importante para el ciclo de vida del mapa
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause() // Importante para el ciclo de vida del mapa
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach() // Limpiar el mapa completamente para evitar memory leaks
        realm.close()
    }
}