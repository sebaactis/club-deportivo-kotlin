package com.example.clubdeportivo.ui.acceso

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.clubdeportivo.R
import com.example.clubdeportivo.ui.profesor.HomeProfesorActivity
import com.example.clubdeportivo.ui.socio.HomeSocioActivity

class LoginActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_OPEN_REGISTRATION = "OPEN_REGISTRATION"
    }

    private var isPasswordVisible = false
    private val role get() = DemoAccess.role(intent.getStringExtra("USER_TYPE"))
    private val staff get() = role == DemoAccess.Role.PROFESOR
    private lateinit var etDni: EditText
    private lateinit var etPassword: EditText
    private lateinit var dialogs: AccessDialogs
    private val handler = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null
    private var active = false
    private var navigated = false
    private var bannerId = 0
    private var dniErrorId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AccessAppearance.restore(savedInstanceState)
        setContentView(R.layout.activity_login)
        AccessAppearance.installInsets(this)
        etDni = findViewById(R.id.etDni)
        etPassword = findViewById(R.id.etPassword)
        isPasswordVisible = savedInstanceState?.getBoolean("access.passwordVisible") ?: false
        etPassword.setText(savedInstanceState?.getString("access.password") ?: "")
        if (savedInstanceState != null) {
            etDni.setText(savedInstanceState.getString("access.dni"))
        }
        bannerId = savedInstanceState?.getInt("access.banner") ?: 0
        dniErrorId = savedInstanceState?.getInt("access.dniError") ?: 0
        dialogs = AccessDialogs(this, role, {
            active && !isFinishing && !isDestroyed && pending == null && !navigated
        }) { profile, notice, dni ->
            if (active && !isFinishing && !isDestroyed && pending == null && !navigated) {
                if (dni != null) {
                    etDni.setText(dni)
                }
                if (profile != null) {
                    beginLocalSignIn(profile)
                } else {
                    bannerId = notice
                    dniErrorId = 0
                    bindAppearance()
                }
            }
        }
        dialogs.restore(savedInstanceState)
        findViewById<View>(R.id.btnBack).setOnClickListener {
            active = false
            cancelLoading()
            finish()
        }
        findViewById<View>(R.id.tvTogglePassword).setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            bindPasswordVisibility()
        }
        findViewById<View>(R.id.btnSubmit).setOnClickListener { submit() }
        findViewById<View>(R.id.btnForgotPassword).setOnClickListener {
            openDialog(AccessDialogs.Kind.RECOVER)
        }
        findViewById<View>(R.id.btnQr).setOnClickListener {
            openDialog(if (staff) AccessDialogs.Kind.CODE else AccessDialogs.Kind.CARD)
        }
        findViewById<View>(R.id.btnRegister).setOnClickListener {
            openDialog(AccessDialogs.Kind.REGISTER)
        }
        etPassword.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }
        bindAppearance()
        val requested = intent.getBooleanExtra(EXTRA_OPEN_REGISTRATION, false)
        intent.removeExtra(EXTRA_OPEN_REGISTRATION)
        if (savedInstanceState == null && requested && !staff) {
            dialogs.open(AccessDialogs.Kind.REGISTER, etDni.text.toString())
        }
    }

    private fun openDialog(kind: AccessDialogs.Kind) {
        if (active && pending == null && !navigated && !isFinishing) {
            dialogs.open(kind, etDni.text.toString())
        }
    }

    private fun bindAppearance() {
        AccessAppearance.bind(this, staff) {
            dialogs.suspend()
            bindAppearance()
            if (active) {
                dialogs.resume()
            }
        }
        AccessAppearance.label(
            this, R.id.tvRoleBadge,
            if (staff) R.string.access_staff_badge else R.string.access_member_badge
        )
        AccessAppearance.label(
            this, R.id.tvTitle,
            if (staff) R.string.access_staff_title else R.string.access_member_title
        )
        AccessAppearance.label(
            this, R.id.tvSubtitle,
            if (staff) R.string.access_staff_subtitle else R.string.access_member_subtitle
        )
        AccessAppearance.label(
            this, R.id.btnQr,
            if (staff) R.string.access_code else R.string.access_qr
        )
        AccessAppearance.label(
            this, R.id.tvTerms,
            if (staff) R.string.access_staff_notice else R.string.access_terms
        )
        findViewById<View>(R.id.accessRegistration).visibility = if (staff) View.GONE else View.VISIBLE
        etDni.hint = AccessAppearance.text(
            this,
            if (staff) R.string.access_staff_dni_hint else R.string.access_dni_hint
        )
        findViewById<View>(R.id.accessLoading).contentDescription = AccessAppearance.text(this, R.string.access_loading)
        bindPasswordVisibility()
        bindStatus()
    }

    private fun bindPasswordVisibility() {
        val start = etPassword.selectionStart
        val end = etPassword.selectionEnd
        etPassword.transformationMethod = if (isPasswordVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }
        etPassword.typeface = ResourcesCompat.getFont(this, R.font.inter_regular)
        if (start >= 0 && end >= 0) {
            etPassword.setSelection(
                start.coerceAtMost(etPassword.length()), end.coerceAtMost(etPassword.length())
            )
        }
        AccessAppearance.label(
            this, R.id.tvTogglePassword,
            if (isPasswordVisible) R.string.access_hide else R.string.access_show
        )
    }

    private fun bindStatus() {
        val messageId = when {
            dniErrorId == R.string.access_invalid_dni -> dniErrorId
            bannerId != 0 -> bannerId
            else -> dniErrorId
        }
        findViewById<TextView>(R.id.accessBanner).apply {
            visibility = if (messageId == 0) View.GONE else View.VISIBLE
            text = if (messageId == 0) "" else AccessAppearance.text(this@LoginActivity, messageId)
        }
        if (dniErrorId != 0) {
            etDni.background = GradientDrawable().apply {
                setColor(ContextCompat.getColor(this@LoginActivity, R.color.access_surface))
                cornerRadius = 14f * resources.displayMetrics.density
                setStroke(
                    (2f * resources.displayMetrics.density + 0.5f).toInt(),
                    ContextCompat.getColor(this@LoginActivity, R.color.access_error)
                )
            }
        }
        val loading = pending != null
        findViewById<View>(R.id.accessLoading).visibility = if (loading) View.VISIBLE else View.GONE
        AccessAppearance.label(
            this, R.id.btnSubmit,
            if (loading) R.string.access_loading else R.string.access_submit
        )
        listOf(
            R.id.btnSubmit, R.id.btnForgotPassword, R.id.btnQr, R.id.btnRegister,
            R.id.btnEs, R.id.btnEn, R.id.tvTogglePassword, R.id.etDni, R.id.etPassword
        ).forEach {
            findViewById<View>(it).isEnabled = !loading && !navigated
        }
    }

    private fun submit() {
        if (!active || pending != null || navigated || isFinishing) return
        val dni = etDni.text.toString()
        if (DemoAccess.normalizeDni(dni) == null) {
            bannerId = R.string.access_check_fields
            dniErrorId = R.string.access_invalid_dni
            bindAppearance()
            return
        }
        val profile = DemoAccess.authenticate(dni, etPassword.text.toString(), role)
        if (profile == null) {
            bannerId = R.string.access_bad_credentials
            dniErrorId = R.string.access_check_dni
            bindAppearance()
            return
        }
        beginLocalSignIn(profile)
    }

    private fun beginLocalSignIn(profile: DemoAccess.Profile) {
        if (!active || pending != null || navigated || isFinishing || profile.role != role) return
        bannerId = 0
        dniErrorId = 0
        val callback = Runnable {
            pending = null
            if (active && !navigated && !isFinishing && !isDestroyed && DemoAccess.activate(profile, role)) {
                navigated = true
                startActivity(
                    Intent(
                        this,
                        if (role == DemoAccess.Role.PROFESOR) {
                            HomeProfesorActivity::class.java
                        } else {
                            HomeSocioActivity::class.java
                        }
                    )
                )
                finish()
            } else {
                bindAppearance()
            }
        }
        pending = callback
        bindAppearance()
        handler.postDelayed(callback, 650L)
    }

    private fun cancelLoading() {
        pending?.let { handler.removeCallbacks(it) }
        pending = null
        if (::etDni.isInitialized) {
            bindAppearance()
        }
    }

    override fun onResume() {
        super.onResume()
        active = true
        dialogs.resume()
    }

    override fun onPause() {
        active = false
        cancelLoading()
        dialogs.suspend()
        super.onPause()
    }

    override fun onStop() {
        active = false
        cancelLoading()
        dialogs.suspend()
        super.onStop()
    }

    override fun onDestroy() {
        active = false
        pending?.let { handler.removeCallbacks(it) }
        pending = null
        dialogs.suspend()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        AccessAppearance.save(outState)
        outState.putBoolean("access.passwordVisible", isPasswordVisible)
        outState.putString("access.password", etPassword.text.toString())
        outState.putString("access.dni", etDni.text.toString())
        outState.putInt("access.banner", bannerId)
        outState.putInt("access.dniError", dniErrorId)
        dialogs.save(outState)
        super.onSaveInstanceState(outState)
    }
}
