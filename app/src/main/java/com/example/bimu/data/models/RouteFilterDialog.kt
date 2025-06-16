import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import com.example.bimu.R
import com.example.bimu.data.models.GeoPoint
import com.example.bimu.data.models.RouteSearchParams
import java.text.SimpleDateFormat
import java.util.*

class RouteFilterDialog(
    context: Context,
    private var initialParams: RouteSearchParams?,
    private val onFilterApplied: (RouteSearchParams) -> Unit
) : Dialog(context) {

    private lateinit var editTextLatitude: EditText
    private lateinit var editTextLongitude: EditText
    private lateinit var editTextRadius: EditText
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var editTextFromDate: EditText
    private lateinit var editTextToDate: EditText
    private lateinit var editTextHour: EditText
    private lateinit var buttonApply: Button

    private val difficulties = listOf("Cualquiera", "Novato", "Principiante", "Intermedio", "Avanzado")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_route_filter)

        editTextLatitude = findViewById(R.id.editTextLatitude)
        editTextLongitude = findViewById(R.id.editTextLongitude)
        editTextRadius = findViewById(R.id.editTextRadius)
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)
        editTextFromDate = findViewById(R.id.editTextFromDate)
        editTextToDate = findViewById(R.id.editTextToDate)
        editTextHour = findViewById(R.id.editTextHour)
        buttonApply = findViewById(R.id.buttonApply)

        spinnerDifficulty.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, difficulties)

        setFieldsFromParams(initialParams)

        // DatePicker para "Desde"
        editTextFromDate.setOnClickListener {
            showDatePicker(editTextFromDate)
        }
        // DatePicker para "Hasta"
        editTextToDate.setOnClickListener {
            showDatePicker(editTextToDate)
        }
        // TimePicker para la hora
        editTextHour.setOnClickListener {
            showTimePicker(editTextHour)
        }

        buttonApply.setOnClickListener {
            val lat = editTextLatitude.text.toString().toDoubleOrNull()
            val lon = editTextLongitude.text.toString().toDoubleOrNull()
            val radius = editTextRadius.text.toString().toDoubleOrNull()
            val difficultyIndex = spinnerDifficulty.selectedItemPosition
            val difficulty = if (difficultyIndex == 0) null else difficulties[difficultyIndex]

            val fromDate = editTextFromDate.text?.toString()?.takeIf { it.isNotBlank() }
            val toDate = editTextToDate.text?.toString()?.takeIf { it.isNotBlank() }
            // Si quieres unir la hora a fromDate, puedes hacerlo aquí

            val params = RouteSearchParams(
                fromDate = fromDate,
                toDate = toDate,
                location = if (lat != null && lon != null) GeoPoint(lat, lon) else initialParams?.location,
                radiusKm = radius ?: initialParams?.radiusKm,
                difficulty = difficulty
            )
            onFilterApplied(params)
            dismiss()
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            editText.setText(dateFormat.format(calendar.time))
        }
        DatePickerDialog(context, listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            editText.setText(timeFormat.format(calendar.time))
        }
        TimePickerDialog(context, listener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), true
        ).show()
    }

    fun setInitialParams(params: RouteSearchParams?) {
        initialParams = params
        setFieldsFromParams(params)
    }

    private fun setFieldsFromParams(params: RouteSearchParams?) {
        params?.let {
            it.location?.let { geo ->
                editTextLatitude.setText(geo.latitude.toString())
                editTextLongitude.setText(geo.longitude.toString())
            }
            it.radiusKm?.let { r -> editTextRadius.setText(r.toString()) }
            it.difficulty?.let { diff ->
                val index = difficulties.indexOf(diff).takeIf { it >= 0 } ?: 0
                spinnerDifficulty.setSelection(index)
            }
            it.fromDate?.let { date -> editTextFromDate.setText(date) }
            it.toDate?.let { date -> editTextToDate.setText(date) }
        }
    }
}
