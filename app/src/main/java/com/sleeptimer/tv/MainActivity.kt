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

        dpm       = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComp = ComponentName(this, TvDeviceAdminReceiver::class.java)

        setupUI()
        requestAdminIfNeeded()
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
    }

    private fun requestAdminIfNeeded() {
        if (!dpm.isAdminActive(adminComp)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComp)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Permiso requerido para apagar la pantalla automáticamente."
                )
            }
            startActivityForResult(intent, REQUEST_ADMIN)
        }
    }

    private fun startTimer(millis: Long, label: String) {
        countDownTimer?.cancel()

        timerButtons.forEach { it.isEnabled = false }
        cancelButton.visibility = View.VISIBLE
        subtitleText.text = "Presiona Cancelar para detener"

        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(remaining: Long) {
                val m = remaining / 1000 / 60
                val s = (remaining / 1000) % 60
                statusText.text = "%d:%02d".format(m, s)
            }
            override fun onFinish() {
                statusText.text = "¡Buenas noches!"
                apagaPantalla()
            }
        }.start()

        statusText.text = label
        subtitleText.text = "Iniciando…"
    }

    private fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        timerButtons.forEach { it.isEnabled = true }
        cancelButton.visibility = View.GONE
        statusText.text = "Sleep Timer"
        subtitleText.text = "Selecciona el tiempo de apagado"
    }

    private fun apagaPantalla() {
        if (dpm.isAdminActive(adminComp)) {
            dpm.lockNow()
        } else {
            requestAdminIfNeeded()
        }
    }

    // Al presionar Atrás mientras corre el timer, lo deja activo en segundo plano
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && countDownTimer != null) {
            moveTaskToBack(true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
