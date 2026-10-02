package com.example.clubdeportivo.ui.acceso

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivo.R
import java.util.Locale

object AccessAppearance {
    var english = false
    private var initialized = false

    fun restore(state: Bundle?) {
        if (!initialized) {
            english = state?.getBoolean("access.english") ?: false
            initialized = true
        }
    }

    fun save(state: Bundle) {
        state.putBoolean("access.english", english)
    }

    fun text(activity: AppCompatActivity, id: Int): String {
        val configuration = Configuration(activity.resources.configuration)
        configuration.setLocale(Locale(if (english) "en" else "es"))
        return activity.createConfigurationContext(configuration).getString(id)
    }

    fun label(activity: AppCompatActivity, view: Int, string: Int) {
        activity.findViewById<TextView>(view)?.text = text(activity, string)
    }

    fun installInsets(activity: AppCompatActivity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val root = activity.findViewById<View>(R.id.accessRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    fun bind(activity: AppCompatActivity, staff: Boolean = false, changed: () -> Unit) {
        fun color(id: Int) = ContextCompat.getColor(activity, id)
        val primary = color(R.color.access_text_primary)
        val secondary = color(R.color.access_text_secondary)
        val backgroundColor = color(R.color.access_navy)
        val surface = color(R.color.access_surface)
        val border = color(R.color.access_border)
        val accent = color(if (staff) R.color.access_staff else R.color.access_member)
        val link = accent
        val root = activity.findViewById<View>(R.id.accessRoot)
        root.setBackgroundColor(backgroundColor)

        fun tintText(view: View) {
            if (view is TextView) {
                view.setTextColor(secondary)
                if (view is EditText) {
                    view.setHintTextColor(secondary)
                }
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    tintText(view.getChildAt(i))
                }
            }
        }

        tintText(root)
        listOf(
            R.id.tvBrand, R.id.tvWelcome, R.id.tvTitle, R.id.tvMember, R.id.tvStaff,
            R.id.etDni, R.id.etPassword, R.id.btnQr
        ).forEach {
            activity.findViewById<TextView>(it)?.setTextColor(primary)
        }
        listOf(R.id.iconSocio, R.id.iconProfesor, R.id.tvRoleBadge, R.id.btnSubmit).forEach {
            activity.findViewById<TextView>(it)?.setTextColor(color(R.color.access_on_accent))
        }
        listOf(R.id.btnRegister, R.id.tvTogglePassword).forEach {
            activity.findViewById<TextView>(it)?.setTextColor(link)
        }
        activity.findViewById<TextView>(R.id.accessBanner)?.setTextColor(color(R.color.access_error))
        listOf(R.id.btnSocio, R.id.btnProfesor).forEach {
            activity.findViewById<View>(it)?.setBackgroundResource(R.drawable.access_card)
        }

        fun shape(stroke: Int, radius: Float = 14f) = GradientDrawable().apply {
            setColor(surface)
            cornerRadius = radius * activity.resources.displayMetrics.density
            setStroke((activity.resources.displayMetrics.density + 0.5f).toInt(), stroke)
        }

        listOf(R.id.etDni, R.id.layoutPassword).forEach {
            val view = activity.findViewById<View>(it) ?: return@forEach
            view.background = ContextCompat.getDrawable(
                activity,
                if (staff) R.drawable.access_input_staff else R.drawable.access_input_member
            )
        }
        activity.findViewById<View>(R.id.btnQr)?.background = shape(border, 16f)
        activity.findViewById<View>(R.id.tvRoleBadge)?.setBackgroundResource(
            if (staff) R.drawable.access_badge_staff else R.drawable.access_badge_member
        )
        activity.findViewById<View>(R.id.btnSubmit)?.setBackgroundResource(
            if (staff) R.drawable.access_button_staff else R.drawable.access_button_member
        )
        val common = mapOf(
            R.id.tvClub to R.string.access_club,
            R.id.tvBrand to R.string.access_algorithm,
            R.id.tvWelcome to R.string.access_welcome,
            R.id.tvMember to R.string.access_member,
            R.id.tvMemberHelp to R.string.access_member_help,
            R.id.tvStaff to R.string.access_staff,
            R.id.tvStaffHelp to R.string.access_staff_help,
            R.id.tvNoAccount to R.string.access_no_account,
            R.id.btnRegister to R.string.access_register,
            R.id.btnBack to R.string.access_back,
            R.id.lblDni to R.string.access_dni,
            R.id.lblPassword to R.string.access_password,
            R.id.btnForgotPassword to R.string.access_forgot,
            R.id.btnSubmit to R.string.access_submit,
            R.id.tvDivider to R.string.access_or
        )
        common.forEach { (view, string) -> label(activity, view, string) }
        listOf(R.id.btnEs, R.id.btnEn).forEach {
            activity.findViewById<TextView>(it).background = shape(border, 24f)
        }
        val es = activity.findViewById<TextView>(R.id.btnEs)
        val en = activity.findViewById<TextView>(R.id.btnEn)
        es.isSelected = !english
        en.isSelected = english
        (if (english) en else es).apply {
            setTextColor(primary)
            background = shape(border, 24f).apply { setColor(border) }
        }
        es.contentDescription = text(activity, R.string.access_spanish)
        en.contentDescription = text(activity, R.string.access_english)
        es.setOnClickListener {
            english = false
            changed()
        }
        en.setOnClickListener {
            english = true
            changed()
        }
        WindowCompat.getInsetsController(activity.window, root).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        activity.window.statusBarColor = Color.TRANSPARENT
        activity.window.navigationBarColor = backgroundColor
    }
}
