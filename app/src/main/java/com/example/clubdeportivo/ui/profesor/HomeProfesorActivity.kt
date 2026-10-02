package com.example.clubdeportivo.ui.profesor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivo.R
import com.example.clubdeportivo.data.personas.PersonaStore
import com.example.clubdeportivo.data.profesor.ProfesorStore
import com.example.clubdeportivo.ui.acceso.DemoAccess
import com.example.clubdeportivo.ui.inicio.MainActivity
import com.example.clubdeportivo.ui.recepcion.CobroActivity
import com.example.clubdeportivo.ui.recepcion.PersonaFormActivity
import com.example.clubdeportivo.ui.recepcion.ReceptionActivity
import com.example.clubdeportivo.ui.socio.MiRutinaActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeProfesorActivity : AppCompatActivity() {
    private var alumnosDialog: AlertDialog? = null
    private var recepcionSeleccionada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (perfilProfesor() == null) {
            return
        }
        setContentView(R.layout.activity_home_profesor)
        instalarInsets()
        recepcionSeleccionada = savedInstanceState?.getBoolean("recepcion", false) ?: false

        findViewById<View>(R.id.staffBack).setOnClickListener {
            finish()
        }
        findViewById<View>(R.id.btnSelectorClasesProfesor).setOnClickListener {
            seleccionarPanel(false)
        }
        findViewById<View>(R.id.btnReceptionProfesor).setOnClickListener {
            seleccionarPanel(true)
        }
        findViewById<View>(R.id.staffCharge).setOnClickListener {
            elegirPersonaParaCobro(PersonaStore.Tipo.SOCIO)
        }
        findViewById<View>(R.id.staffReceptionCharge).setOnClickListener {
            elegirPersonaParaCobro(PersonaStore.Tipo.SOCIO)
        }
        findViewById<View>(R.id.staffDailyPass).setOnClickListener {
            elegirPersonaParaCobro(PersonaStore.Tipo.NO_SOCIO)
        }
        findViewById<View>(R.id.staffNewMember).setOnClickListener {
            startActivity(Intent(this, PersonaFormActivity::class.java))
        }
        findViewById<View>(R.id.staffIssueCard).setOnClickListener {
            elegirSocioParaCarnet()
        }
        findViewById<View>(R.id.staffDueBanner).setOnClickListener {
            startActivity(
                Intent(this, ReceptionActivity::class.java)
                    .putExtra(ReceptionActivity.EXTRA_VENCE_HOY, true)
            )
        }
        findViewById<View>(R.id.staffAgendaLink).setOnClickListener {
            startActivity(Intent(this, ClasesProfesorActivity::class.java))
        }
        findViewById<View>(R.id.staffRoutines).setOnClickListener {
            elegirAlumno()
        }
        findViewById<View>(R.id.staffAttendance).setOnClickListener {
            abrirAsistencia(ProfesorStore.CLASE_PROXIMA_ID)
        }
    }

    override fun onResume() {
        super.onResume()
        val profile = perfilProfesor() ?: return
        findViewById<TextView>(R.id.tvProfesorName).text = getString(R.string.staff_greeting, profile.name)
        actualizarPanel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("recepcion", recepcionSeleccionada)
        super.onSaveInstanceState(outState)
    }

    private fun seleccionarPanel(recepcion: Boolean) {
        recepcionSeleccionada = recepcion
        actualizarPanel()
        findViewById<ScrollView>(R.id.staffScroll).smoothScrollTo(0, 0)
    }

    private fun actualizarPanel() {
        findViewById<View>(R.id.staffClassesPanel).visibility = if (recepcionSeleccionada) View.GONE else View.VISIBLE
        findViewById<View>(R.id.staffReceptionPanel).visibility = if (recepcionSeleccionada) View.VISIBLE else View.GONE
        findViewById<View>(R.id.staffAttendance).visibility = if (recepcionSeleccionada) View.GONE else View.VISIBLE
        actualizarTab(R.id.btnSelectorClasesProfesor, !recepcionSeleccionada)
        actualizarTab(R.id.btnReceptionProfesor, recepcionSeleccionada)
        if (recepcionSeleccionada) {
            actualizarRecepcion()
        } else {
            actualizarAgenda()
        }
    }

    private fun actualizarTab(id: Int, seleccionado: Boolean) {
        findViewById<AppCompatButton>(id).apply {
            isSelected = seleccionado
            setBackgroundResource(if (seleccionado) R.drawable.access_button_staff else android.R.color.transparent)
            setTextColor(getColor(if (seleccionado) R.color.access_on_accent else R.color.access_text_secondary))
        }
    }

    private fun actualizarRecepcion() {
        val hoy = System.currentTimeMillis()
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-AR")).format(Date(hoy))
        findViewById<TextView>(R.id.staffDate).text = getString(R.string.staff_reception_date, fecha)
        val cantidad = PersonaStore.getAll().count { PersonaStore.cuotaVenceHoy(it, hoy) }
        findViewById<TextView>(R.id.staffDueCount).text = getString(R.string.staff_due_count, cantidad)
    }

    override fun onPause() {
        alumnosDialog?.dismiss()
        alumnosDialog = null
        super.onPause()
    }

    private fun perfilProfesor(): DemoAccess.Profile? {
        val profile = DemoAccess.currentProfile
        if (profile == null || profile.role != DemoAccess.Role.PROFESOR) {
            if (!isFinishing) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            return null
        }
        return profile
    }

    private fun instalarInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val root = findViewById<View>(R.id.staffRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun actualizarAgenda() {
        val lista = findViewById<LinearLayout>(R.id.staffClassList)
        lista.removeAllViews()
        findViewById<TextView>(R.id.staffDate).text = getString(
            R.string.staff_demo_date,
            ProfesorStore.clases.firstOrNull()?.fecha ?: getString(R.string.staff_no_classes)
        )
        for (clase in ProfesorStore.clases) {
            val row = layoutInflater.inflate(R.layout.item_staff_class, lista, false)
            row.isSelected = clase.id == ProfesorStore.CLASE_PROXIMA_ID
            row.findViewById<TextView>(R.id.staffClassTime).text = clase.horario
            row.findViewById<TextView>(R.id.staffClassDetail).text = getString(
                R.string.staff_class_detail, clase.deporte, clase.lugar
            )
            row.findViewById<TextView>(R.id.staffClassCount).text = getString(
                R.string.staff_class_count, clase.socios.size
            )
            row.contentDescription = getString(
                R.string.staff_class_accessibility,
                clase.deporte,
                clase.fecha,
                clase.horario,
                clase.lugar,
                clase.socios.size
            )
            row.setOnClickListener {
                abrirAsistencia(clase.id)
            }
            lista.addView(row)
        }
        if (ProfesorStore.clases.isEmpty()) {
            val empty = TextView(this)
            empty.setText(R.string.staff_no_classes)
            empty.typeface = ResourcesCompat.getFont(this, R.font.inter_regular)
            empty.setTextColor(getColor(R.color.access_text_secondary))
            lista.addView(empty)
        }
        findViewById<TextView>(R.id.staffRoutineCount).text = getString(
            R.string.staff_student_count, alumnosUnicos().size
        )
        val clase = ProfesorStore.buscarClase(ProfesorStore.CLASE_PROXIMA_ID)
        val footer = findViewById<TextView>(R.id.staffAttendance)
        footer.isEnabled = clase != null
        footer.alpha = if (clase == null) 0.5f else 1f
        footer.text = if (clase == null) {
            getString(R.string.staff_no_classes)
        } else {
            getString(R.string.staff_attendance_time, clase.horario)
        }
    }

    private fun abrirAsistencia(id: String) {
        if (perfilProfesor() == null) {
            return
        }
        if (ProfesorStore.buscarClase(id) == null) {
            AlertDialog.Builder(this)
                .setMessage(R.string.staff_no_classes)
                .setPositiveButton(R.string.staff_close, null)
                .show()
            return
        }
        startActivity(
            Intent(this, AsistenciaProfesorActivity::class.java)
                .putExtra(AsistenciaProfesorActivity.EXTRA_CLASE_ID, id)
        )
    }

    private fun elegirPersonaParaCobro(tipo: PersonaStore.Tipo) {
        if (perfilProfesor() == null) {
            return
        }
        val personas = PersonaStore.getAll().filter { it.tipo == tipo }
        if (personas.isEmpty()) {
            mostrarSinPersonas()
            return
        }
        val titulo = if (tipo == PersonaStore.Tipo.SOCIO) R.string.staff_choose_quota else R.string.staff_choose_pass
        alumnosDialog = AlertDialog.Builder(this)
            .setTitle(titulo)
            .setItems(personas.map { "${it.nombre} ${it.apellido} · DNI ${it.dni}" }.toTypedArray()) { _, posicion ->
                val actual = PersonaStore.getById(personas[posicion].id)
                if (actual == null || actual.tipo != tipo) {
                    mostrarPersonaNoDisponible()
                } else if (PersonaStore.getTarifas() == null) {
                    explicarTarifas()
                } else {
                    startActivity(
                        Intent(this, CobroActivity::class.java)
                            .putExtra(CobroActivity.EXTRA_PERSONA_ID, actual.id)
                    )
                }
            }
            .setNegativeButton(R.string.staff_close, null)
            .show()
    }

    private fun explicarTarifas() {
        alumnosDialog = AlertDialog.Builder(this)
            .setTitle(R.string.staff_tariffs_title)
            .setMessage(R.string.staff_tariffs_instruction)
            .setPositiveButton(R.string.staff_configure) { _, _ ->
                startActivity(
                    Intent(this, ReceptionActivity::class.java)
                        .putExtra(ReceptionActivity.EXTRA_CONFIGURAR_TARIFAS, true)
                )
            }
            .setNegativeButton(R.string.staff_close, null)
            .show()
    }

    private fun mostrarSinPersonas() {
        alumnosDialog = AlertDialog.Builder(this)
            .setTitle(R.string.staff_no_eligible_title)
            .setMessage(R.string.staff_no_eligible_message)
            .setPositiveButton(R.string.staff_open_manager) { _, _ ->
                startActivity(Intent(this, ReceptionActivity::class.java))
            }
            .setNegativeButton(R.string.staff_close, null)
            .show()
    }

    private fun mostrarPersonaNoDisponible() {
        alumnosDialog = AlertDialog.Builder(this)
            .setMessage(R.string.staff_person_unavailable)
            .setPositiveButton(R.string.staff_close, null)
            .show()
    }

    private fun elegirSocioParaCarnet() {
        if (perfilProfesor() == null) {
            return
        }
        val socios = PersonaStore.getAll().filter { it.tipo == PersonaStore.Tipo.SOCIO }
        if (socios.isEmpty()) {
            mostrarSinPersonas()
            return
        }
        alumnosDialog = AlertDialog.Builder(this)
            .setTitle(R.string.staff_choose_card)
            .setItems(socios.map { "${it.nombre} ${it.apellido} · DNI ${it.dni}" }.toTypedArray()) { _, posicion ->
                mostrarCarnet(socios[posicion].id)
            }
            .setNegativeButton(R.string.staff_close, null)
            .show()
    }

    private fun mostrarCarnet(id: String) {
        val persona = PersonaStore.getById(id)
        if (persona == null || persona.tipo != PersonaStore.Tipo.SOCIO) {
            mostrarPersonaNoDisponible()
            return
        }
        val hoy = System.currentTimeMillis()
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-AR"))
        val fecha = persona.vencimientoNullable?.let { formato.format(Date(it)) }
            ?: getString(R.string.staff_no_date)
        val detalle = getString(
            R.string.staff_card_detail,
            persona.nombre,
            persona.apellido,
            persona.dni,
            persona.tipo.etiqueta,
            persona.id,
            fecha,
            persona.estado(hoy).etiqueta
        )
        alumnosDialog = AlertDialog.Builder(this)
            .setTitle(R.string.staff_card_title)
            .setMessage(detalle)
            .setPositiveButton(R.string.staff_close, null)
            .show()
    }

    private fun alumnosUnicos(): List<ProfesorStore.Socio> {
        val alumnos = mutableListOf<ProfesorStore.Socio>()
        val ids = mutableSetOf<String>()
        for (clase in ProfesorStore.clases) {
            for (socio in clase.socios) {
                if (ids.add(socio.id)) {
                    alumnos.add(socio)
                }
            }
        }
        return alumnos
    }

    private fun elegirAlumno() {
        if (perfilProfesor() == null) {
            return
        }
        val alumnos = alumnosUnicos()
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.staff_choose_student)
        if (alumnos.isEmpty()) {
            dialog.setMessage(R.string.staff_no_students)
                .setPositiveButton(R.string.staff_close, null)
        } else {
            val nombres = alumnos.map { "${it.nombre} · ${it.id}" }.toTypedArray()
            dialog.setItems(nombres) { _, position ->
                val alumno = alumnos[position]
                startActivity(
                    Intent(this, MiRutinaActivity::class.java)
                        .putExtra(MiRutinaActivity.EXTRA_CONSULTA_PROFESOR, true)
                        .putExtra(MiRutinaActivity.EXTRA_ALUMNO_ID, alumno.id)
                        .putExtra(MiRutinaActivity.EXTRA_ALUMNO_NOMBRE, alumno.nombre)
                )
            }
            dialog.setNegativeButton(R.string.staff_close, null)
        }
        alumnosDialog = dialog.show()
    }
}
