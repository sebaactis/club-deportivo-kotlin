package com.example.clubdeportivo.ui.reservas

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivo.R
import com.example.clubdeportivo.data.reservas.BookedClass
import com.example.clubdeportivo.data.reservas.ReservationCatalog
import com.example.clubdeportivo.data.reservas.ReservationResult
import com.example.clubdeportivo.data.reservas.ReservationStore
import com.example.clubdeportivo.ui.acceso.DemoAccess
import com.example.clubdeportivo.ui.inicio.MainActivity
import com.example.clubdeportivo.ui.socio.HomeSocioActivity

class ReservationConfirmationActivity : AppCompatActivity() {
    private var editing = false
    private var reservationId: String? = null
    private var className: String? = null
    private var day: String? = null
    private var time: String? = null
    private var saved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireMember()) {
            return
        }
        setContentView(R.layout.activity_reservation_confirmation)
        installInsets()

        val state = savedInstanceState ?: intent.extras ?: Bundle()
        editing = state.containsKey(ReservationActivity.EXTRA_ID)
        reservationId = state.get(ReservationActivity.EXTRA_ID) as? String
        className = state.get(ReservationActivity.EXTRA_CLASS) as? String
        day = state.get(ReservationActivity.EXTRA_DAY) as? String
        time = state.get(ReservationActivity.EXTRA_TIME) as? String
        saved = savedInstanceState?.getBoolean(STATE_SAVED) ?: false

        findViewById<TextView>(R.id.confirmationTitle).text =
            getString(if (editing) R.string.reservas_edit_confirm else R.string.reservas_confirm_title)
        val summary = findViewById<TextView>(R.id.reservationSummary)
        val confirmButton = findViewById<AppCompatButton>(R.id.confirmButton)
        confirmButton.setText(if (editing) R.string.reservas_save_changes else R.string.reservas_confirm)
        val initialError = validationError()
        summary.text = initialError?.message
        summary.visibility = if (initialError == null) View.GONE else View.VISIBLE
        findViewById<View>(R.id.confirmationCard).visibility = if (initialError == null) View.VISIBLE else View.GONE
        if (initialError == null) {
            showDetails()
        }
        confirmButton.isEnabled = initialError == null && !saved
        confirmButton.alpha = if (confirmButton.isEnabled) 1f else 0.5f

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }
        findViewById<AppCompatButton>(R.id.modifyButton).setOnClickListener {
            finish()
        }
        confirmButton.setOnClickListener {
            if (!requireMember() || saved) {
                return@setOnClickListener
            }
            val error = validationError()
            if (error != null) {
                summary.visibility = View.VISIBLE
                summary.text = error.message
                Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                confirmButton.isEnabled = false
                return@setOnClickListener
            }
            val booking = BookedClass(className = className!!, day = day!!, time = time!!)
            val result = if (editing) {
                ReservationStore.update(reservationId!!, booking)
            } else {
                ReservationStore.add(booking)
            }
            if (result != ReservationResult.SUCCESS) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            saved = true
            confirmButton.isEnabled = false
            Toast.makeText(
                this,
                if (editing) "Reserva actualizada." else "¡Reserva confirmada!",
                Toast.LENGTH_LONG
            ).show()
            val home = Intent(this, HomeSocioActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(home)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!requireMember()) {
            return
        }
        if (ReservationCatalog.isValid(className, day, time)) {
            showDetails()
        }
    }

    private fun requireMember(): Boolean {
        if (DemoAccess.currentProfile?.role == DemoAccess.Role.SOCIO) {
            return true
        }
        if (!isFinishing) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        return false
    }

    private fun installInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val root = findViewById<View>(R.id.reservasRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun showDetails() {
        val details = ReservationCatalog.demoDetails[className] ?: return
        val profile = DemoAccess.currentProfile ?: return
        findViewById<TextView>(R.id.teacherInitials).apply {
            text = details.initials
            setBackgroundResource(
                if (className == "Musculación") {
                    R.drawable.access_circle_member
                } else {
                    R.drawable.reservas_avatar
                }
            )
        }
        findViewById<TextView>(R.id.confirmationClass).text = className
        findViewById<TextView>(R.id.confirmationTeacher).text = getString(R.string.reservas_teacher, details.teacher)
        val rows = findViewById<LinearLayout>(R.id.confirmationDetails)
        rows.removeAllViews()
        addDetail(rows, R.string.reservas_day_time, getString(R.string.reservas_weekly, day, time))
        addDetail(rows, R.string.reservas_room, details.room)
        addDetail(rows, R.string.reservas_duration, getString(R.string.reservas_minutes, details.durationMinutes))
        addDetail(rows, R.string.reservas_capacity, details.capacitySample)
        findViewById<TextView>(R.id.confirmationMember).text = getString(R.string.reservas_member, profile.name)
        findViewById<TextView>(R.id.confirmationQuota).setText(
            if (profile.id == "demo-member-1") R.string.reservas_quota_example else R.string.reservas_quota_unknown
        )
    }

    private fun addDetail(container: LinearLayout, label: Int, value: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        row.addView(
            TextView(this).apply {
                text = getString(label)
                textSize = 12f
                setTextColor(0xFF93A9C8.toInt())
                typeface = ResourcesCompat.getFont(context, R.font.inter_regular)
            },
            LinearLayout.LayoutParams(0, -2, 1f)
        )
        row.addView(
            TextView(this).apply {
                text = value
                textSize = 13f
                gravity = Gravity.END
                setTextColor(0xFFF2F6FC.toInt())
                typeface = ResourcesCompat.getFont(context, R.font.inter_semibold)
            },
            LinearLayout.LayoutParams(0, -2, 1.2f)
        )
        container.addView(row)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun validationError(): ReservationResult? {
        if (editing && reservationId?.let { ReservationStore.getById(it) } == null) {
            return ReservationResult.UNKNOWN_ID
        }
        if (!ReservationCatalog.isValid(className, day, time)) {
            return ReservationResult.INVALID
        }
        return null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (editing) {
            outState.putString(ReservationActivity.EXTRA_ID, reservationId)
        }
        outState.putString(ReservationActivity.EXTRA_CLASS, className)
        outState.putString(ReservationActivity.EXTRA_DAY, day)
        outState.putString(ReservationActivity.EXTRA_TIME, time)
        outState.putBoolean(STATE_SAVED, saved)
    }

    companion object {
        private const val STATE_SAVED = "reservation_saved"
    }
}
