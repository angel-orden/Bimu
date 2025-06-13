package com.example.bimu.data.models

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.example.bimu.R

class RouteFilterDialog(
    context: Context,
    private val onFilterApplied: (RouteSearchParams) -> Unit
) : Dialog(context) {

    private lateinit var editTextLatitude: EditText
    private lateinit var editTextLongitude: EditText
    private lateinit var editTextRadius: EditText
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var buttonApply: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_route_filter)

        editTextLatitude = findViewById(R.id.editTextLatitude)
        editTextLongitude = findViewById(R.id.editTextLongitude)
        editTextRadius = findViewById(R.id.editTextRadius)
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)
        buttonApply = findViewById(R.id.buttonApply)

        // Dificultades como string
        val difficulties = listOf("Cualquiera", "Novato", "Principiante", "Intermedio", "Avanzado")
        spinnerDifficulty.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, difficulties)

        buttonApply.setOnClickListener {
            // Procesa los campos para el filtro
            val lat = editTextLatitude.text.toString().toDoubleOrNull()
            val lon = editTextLongitude.text.toString().toDoubleOrNull()
            val radius = editTextRadius.text.toString().toDoubleOrNull()
            val difficultyIndex = spinnerDifficulty.selectedItemPosition

            val difficulty = if (difficultyIndex == 0) null else difficulties[difficultyIndex] // null si "Cualquiera"

            val params = RouteSearchParams(
                location = if (lat != null && lon != null) GeoPoint(lat, lon) else null,
                radiusKm = radius,
                difficulty = difficulty
            )
            onFilterApplied(params)
            dismiss()
        }
    }
}
