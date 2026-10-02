package com.example.clubdeportivo.ui.acceso

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.clubdeportivo.R

class AccessDialogs(
    private val activity: AppCompatActivity,
    private val role: DemoAccess.Role,
    private val canConfirm: () -> Boolean,
    private val confirmed: (DemoAccess.Profile?, Int, String?) -> Unit
) {
    enum class Kind { REGISTER, RECOVER, CARD, CODE }

    private var draft: Bundle? = null
    private var dialog: AlertDialog? = null
    private val fields = mutableMapOf<String, EditText>()
    private var choices: RadioGroup? = null
    private var errorId = 0

    private fun text(id: Int) = AccessAppearance.text(activity, id)

    fun restore(state: Bundle?) {
        draft = state?.getBundle("access.dialog")
    }

    fun save(state: Bundle) {
        capture()
        state.putBundle("access.dialog", draft)
    }

    fun open(kind: Kind, dni: String) {
        if (dialog != null || (kind == Kind.REGISTER && role != DemoAccess.Role.SOCIO)) return
        draft = Bundle().apply {
            putString("kind", kind.name)
            putString("dni", dni)
        }
        errorId = 0
        resume()
    }

    private fun capture() {
        val data = draft ?: return
        fields.forEach { (key, field) -> data.putString(key, field.text.toString()) }
        choices?.let { group ->
            val selected = group.findViewById<RadioButton>(group.checkedRadioButtonId)
            data.putString("selected", selected?.tag as? String)
        }
        data.putInt("error", errorId)
    }

    fun suspend() {
        capture()
        val old = dialog
        dialog = null
        old?.setOnDismissListener(null)
        old?.dismiss()
        fields.clear()
        choices = null
    }

    fun resume() {
        if (dialog != null) return
        val data = draft ?: return
        val kind = Kind.entries.firstOrNull { it.name == data.getString("kind") } ?: run {
            draft = null
            return
        }
        if ((kind == Kind.REGISTER || kind == Kind.CARD) && role != DemoAccess.Role.SOCIO) {
            draft = null
            return
        }
        if (kind == Kind.CODE && role != DemoAccess.Role.PROFESOR) {
            draft = null
            return
        }
        val primary = ContextCompat.getColor(activity, R.color.access_text_primary)
        val surface = ContextCompat.getColor(activity, R.color.access_surface)
        val font = ResourcesCompat.getFont(activity, R.font.inter_regular)
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (24 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            setBackgroundColor(surface)
        }

        fun label(value: String) = TextView(activity).apply {
            text = value
            typeface = font
            setTextColor(primary)
            setPadding(0, 12, 0, 12)
            content.addView(this)
        }

        val titleId = when (kind) {
            Kind.REGISTER -> R.string.access_demo_register
            Kind.RECOVER -> R.string.access_demo_recover
            Kind.CARD -> R.string.access_demo_card
            Kind.CODE -> R.string.access_demo_code
        }
        label(text(titleId)).typeface = ResourcesCompat.getFont(activity, R.font.inter_semibold)
        label(text(if (kind == Kind.RECOVER) R.string.access_recovery_notice else R.string.access_demo_notice))

        fun field(key: String, labelId: Int, password: Boolean = false, numeric: Boolean = false) {
            val caption = label(text(labelId))
            val edit = EditText(activity).apply {
                id = View.generateViewId()
                isSingleLine = true
                inputType = when {
                    password -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    numeric -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    else -> InputType.TYPE_CLASS_TEXT
                }
                typeface = font
                setTextColor(primary)
                setHintTextColor(primary)
                setText(data.getString(key).orEmpty())
                background = GradientDrawable().apply {
                    setColor(surface)
                    cornerRadius = 12 * resources.displayMetrics.density
                    setStroke(2, primary)
                }
                val pad = (12 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
                isSaveEnabled = false
            }
            caption.labelFor = edit.id
            fields[key] = edit
            content.addView(edit)
        }

        when (kind) {
            Kind.REGISTER -> {
                field("name", R.string.access_name)
                field("dni", R.string.access_dni, numeric = true)
                field("password", R.string.access_password, password = true)
            }
            Kind.RECOVER -> {
                field("dni", R.string.access_dni, numeric = true)
                field("password", R.string.access_replacement, password = true)
            }
            Kind.CODE -> field("code", R.string.access_club_code)
            Kind.CARD -> {
                choices = RadioGroup(activity).also { group ->
                    DemoAccess.members().forEach { profile ->
                        val button = RadioButton(activity).apply {
                            id = View.generateViewId()
                            tag = profile.id
                            text = "${profile.name} · ${profile.dni}"
                            typeface = font
                            setTextColor(primary)
                        }
                        group.addView(button)
                        if (data.getString("selected") == profile.id) {
                            group.check(button.id)
                        }
                    }
                    content.addView(group)
                }
            }
        }
        val error = label("").apply {
            setTextColor(ContextCompat.getColor(activity, R.color.access_error))
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        errorId = data.getInt("error")
        if (errorId != 0) {
            error.text = text(errorId)
        }
        val scroll = android.widget.ScrollView(activity).apply {
            addView(content)
        }
        val shown = AlertDialog.Builder(activity)
            .setView(scroll)
            .setPositiveButton(text(R.string.access_confirm), null)
            .setNegativeButton(text(R.string.access_cancel), null)
            .create()
        dialog = shown
        shown.setOnDismissListener {
            dialog = null
            draft = null
            fields.clear()
            choices = null
            errorId = 0
        }
        shown.setOnShowListener {
            shown.window?.setBackgroundDrawable(
                GradientDrawable().apply {
                    setColor(surface)
                }
            )
            listOf(AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEGATIVE).forEach {
                shown.getButton(it).apply {
                    typeface = font
                    setTextColor(primary)
                    setBackgroundColor(surface)
                    (parent as? View)?.setBackgroundColor(surface)
                }
            }
            shown.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                confirmDialog(kind, data, shown, error)
            }
        }
        shown.show()
    }

    private fun confirmDialog(kind: Kind, data: Bundle, shown: AlertDialog, error: TextView) {
        if (!canConfirm()) return
        capture()

        fun fail(id: Int) {
            errorId = id
            error.text = text(id)
        }

        val dni = data.getString("dni").orEmpty()
        val password = data.getString("password").orEmpty()
        if (kind == Kind.REGISTER || kind == Kind.RECOVER) {
            if (DemoAccess.normalizeDni(dni) == null) {
                fail(R.string.access_invalid_dni)
                return
            }
            if (password.length < 6) {
                fail(R.string.access_short_password)
                return
            }
        }
        when (kind) {
            Kind.REGISTER -> {
                if (data.getString("name").orEmpty().trim().isEmpty()) {
                    fail(R.string.access_invalid_name)
                } else if (DemoAccess.exists(dni)) {
                    fail(R.string.access_duplicate)
                } else {
                    val profile = DemoAccess.register(data.getString("name").orEmpty(), dni, password)
                    if (profile != null) {
                        shown.dismiss()
                        confirmed(null, R.string.access_registered, profile.dni)
                    } else {
                        fail(R.string.access_invalid_dni)
                    }
                }
            }
            Kind.RECOVER -> {
                if (DemoAccess.reset(dni, role, password)) {
                    shown.dismiss()
                    confirmed(null, R.string.access_recovered, DemoAccess.normalizeDni(dni))
                } else {
                    fail(R.string.access_unknown_account)
                }
            }
            Kind.CARD -> {
                val profile = DemoAccess.memberCard(data.getString("selected").orEmpty())
                if (profile == null) {
                    fail(R.string.access_choose_card)
                } else {
                    shown.dismiss()
                    confirmed(profile, 0, null)
                }
            }
            Kind.CODE -> {
                val profile = DemoAccess.clubCode(data.getString("code").orEmpty())
                if (profile == null) {
                    fail(R.string.access_wrong_code)
                } else {
                    shown.dismiss()
                    confirmed(profile, 0, null)
                }
            }
        }
    }
}
