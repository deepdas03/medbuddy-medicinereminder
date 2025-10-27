package com.example.medbuddy

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var medicinesContainer: LinearLayout
    private val medicines = mutableListOf<Medicine>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationPermission()
        requestScheduleAlarmPermission()

        medicinesContainer = findViewById(R.id.medicinesContainer)
        val btnAddMedicine = findViewById<Button>(R.id.btnAddMedicine)
        btnAddMedicine.setOnClickListener {
            addMedicineCard()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun requestScheduleAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun addMedicineCard() {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 38)
            }
            radius = 16F
            cardElevation = 12F
        }

        val innerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val etMedicineName = EditText(this).apply {
            hint = "Medicine Name"
        }
        innerLayout.addView(etMedicineName)

        val etDoseCount = EditText(this).apply {
            hint = "Number of doses per day"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        innerLayout.addView(etDoseCount)

        val doseTimesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        innerLayout.addView(doseTimesContainer)

        val etTotalDays = EditText(this).apply {
            hint = "Total days to take medicine"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        innerLayout.addView(etTotalDays)

        val doseTimes = mutableListOf<Pair<Int, Int>>()

        etDoseCount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val doseCount = s?.toString()?.toIntOrNull() ?: 0
                doseTimesContainer.removeAllViews()
                doseTimes.clear()
                for (i in 1..doseCount) {
                    val btnPickTime = Button(this@MainActivity).apply {
                        text = "Set Dose $i Time"
                    }
                    btnPickTime.setOnClickListener {
                        val cal = Calendar.getInstance()
                        val hour = cal.get(Calendar.HOUR_OF_DAY)
                        val minute = cal.get(Calendar.MINUTE)
                        val dialog = TimePickerDialog(
                            this@MainActivity,
                            { _, pickedHour, pickedMinute ->
                                val amPm = if (pickedHour < 12) "AM" else "PM"
                                val hourDisplay = if (pickedHour == 0 || pickedHour == 12) 12 else pickedHour % 12
                                btnPickTime.text = String.format("Dose %d: %02d:%02d %s", i, hourDisplay, pickedMinute, amPm)
                                if (doseTimes.size < i) {
                                    doseTimes.add(Pair(pickedHour, pickedMinute))
                                } else {
                                    doseTimes[i - 1] = Pair(pickedHour, pickedMinute)
                                }
                            },
                            hour,
                            minute,
                            false
                        )
                        dialog.show()
                    }
                    doseTimesContainer.addView(btnPickTime)
                    doseTimes.add(Pair(9, 0)) // Default time for every dose
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val btnSetReminder = Button(this).apply {
            text = "Set Reminder"
        }
        btnSetReminder.setOnClickListener {
            val name = etMedicineName.text.toString()
            val totalDays = etTotalDays.text.toString().toIntOrNull() ?: 1
            if (name.isNotBlank() && doseTimes.isNotEmpty()) {
                val medicine = Medicine(
                    name = name,
                    doseTimes = doseTimes.toList(),
                    totalDays = totalDays,
                    startDateMillis = System.currentTimeMillis(),
                    isReminderOn = true
                )
                medicines.add(medicine)
                scheduleMedicineAlarms(medicine)
                Toast.makeText(this@MainActivity, "Reminder set for $name", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Please fill all fields and set dose times", Toast.LENGTH_SHORT).show()
            }
        }
        innerLayout.addView(btnSetReminder)

        val btnStopReminder = Button(this).apply {
            text = "Stop Reminder"
        }
        btnStopReminder.setOnClickListener {
            val name = etMedicineName.text.toString()
            if (name.isNotBlank()) {
                stopMedicineAlarms(name)
                Toast.makeText(this@MainActivity, "Reminder stopped for $name", Toast.LENGTH_SHORT).show()
            }
        }
        innerLayout.addView(btnStopReminder)

        card.addView(innerLayout)
        medicinesContainer.addView(card)
    }

    private fun scheduleMedicineAlarms(medicine: Medicine) {
        for ((index, time) in medicine.doseTimes.withIndex()) {
            val (hour, minute) = time
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("medicineName", medicine.name)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val cal = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            if (cal.timeInMillis < System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun stopMedicineAlarms(medicineName: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        for (i in medicines.indices) {
            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}

// Data class should be outside MainActivity
data class Medicine(
    val name: String,
    val doseTimes: List<Pair<Int, Int>>,
    val totalDays: Int,
    val startDateMillis: Long,
    val isReminderOn: Boolean
)
