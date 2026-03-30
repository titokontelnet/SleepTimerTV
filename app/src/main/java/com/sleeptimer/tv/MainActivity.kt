package com.sleeptimer.tv

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComp: ComponentName
    private var countDownTimer: CountDownTimer? = null

    private lateinit var statusText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var cancelButton: Button
    private val timerButtons = mutableListOf<Button>()

    companion object {
        const val REQUEST_ADMIN = 1
        val OPTIONS = listOf(
            Pair("15 min",  15 * 60 * 1000L),
            Pair("30 min",  30 * 60 * 1000L),
            Pair("45 min",  45 * 60 * 1000L),
            Pair("60 min",  60 * 60 * 1000L),
            Pair("90 min",  90 * 60 * 1000L),
            Pair("2 horas", 120 * 60 * 1000L)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            dpm       = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            adminComp = ComponentName(this, TvDeviceAdminReceiver::class.java)
        } catch (e: Exception) {
            // Continuar sin admin
        }

        setupUI()
    }

    private fun setupUI() {
        statusText   = findViewById(R.id.tv_status)
        subtitleText = findViewById(R.id.tv_subtitle)
        cancelButton = findViewById(R.id.btn_cancel)

        val ids = listOf(
            R.id.btn_15, R.id.btn_30, R.id.btn_45,
            R.id.btn_60, R.id.btn_90, R.id.btn_120
        )

        ids.forEachIndexed { i, id ->
            val btn = findViewById<Button>(id)
            btn.text = OPTIONS[i].first
            btn.setOnClickListener { startTimer(OPTIONS[i].second, OPTIONS[i].first) }
            timerButtons.add(btn)
        }

        cancelButton.setOnClickListener { cancelTimer() }
        cancelButton.visibility = View.GONE
        timerButtons.firstOrNull()?.requestFocus()
    }

    private fun startTimer(millis: Long, label: String) {
        if (::dpm.isInitialized && !dpm.isAdminActive(adminComp)) {
            try {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComp)
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Requerido para apagar la pantalla automáticamente.")
                }
                startActivityForResult(intent, REQUEST_ADMIN)
            } catch (e: Exception) {
                Toast.makeText(this, "Activa el permiso de administrador", Toast.LENGTH_LONG).show()
            }
            return
        }

        countDownTimer?.cancel()
        timerButtons.forEach { it.isEnabled = false }
        cancelButton.visibility = View.VISIBLE
        cancelButton.requestFocus()

        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(remaining: Long) {
                val m = remaining / 1000 / 60
                val s = (remaining / 1000) % 60
                statusText.text = "%d:%02d".format(m, s)
                subtitleText.text = "Presiona Cancelar para detener"
            }
            override fun onFinish() {
                statusText.text = "Buenas noches"
                subtitleText.text = "Apagando pantalla..."
                apagaPantalla()
            }
        }.start()

        statusText.text = label
        subtitleText.text = "Iniciando..."
    }

    private fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        timerButtons.forEach { it.isEnabled = true }
        cancelButton.visibility = View.GONE
        statusText.text = "Sleep Timer"
        subtitleText.text = "Selecciona el tiempo de apagado"
        timerButtons.firstOrNull()?.requestFocus()
    }

    private fun apagaPantalla() {
        try {
            if (::dpm.isInitialized && dpm.isAdminActive(adminComp)) {
                dpm.lockNow()
            } else {
                moveTaskToBack(true)
            }
        } catch (e: Exception) {
            moveTaskToBack(true)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && countDownTimer != null) {
            moveTaskToBack(true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
