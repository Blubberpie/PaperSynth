package com.example.papersynth.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.papersynth.fragments.CanvasFragment
import com.example.papersynth.PlaybackEngine.create
import com.example.papersynth.PlaybackEngine.delete
import com.example.papersynth.PlaybackEngine.setChannelCount
import com.example.papersynth.PlaybackEngine.start
import com.example.papersynth.PlaybackEngine.stop
import com.example.papersynth.R
import com.example.papersynth.dataclasses.FourierSeries
import com.example.papersynth.utils.CurveFittingUtil
import com.example.papersynth.utils.FileUtil
import kotlin.math.PI

private const val NUM_SAMPLES = 256
private const val HALF_WAVE_CYCLE = NUM_SAMPLES / (2 * PI.toFloat())

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var fourierSeries: ArrayList<FourierSeries>

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add canvas fragment
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<CanvasFragment>(R.id.fragment_container_view)
            }
        }

        setContentView(R.layout.activity_main)

        window.decorView.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.mainD,
                null
            )
        )

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container_view, CanvasFragment())
            .commit()

        applyWave()

        val oscillatorActivityButton = findViewById<Button>(R.id.btn_oscillator_activity)
        oscillatorActivityButton.setOnClickListener(this)
        val applyWaveButton = findViewById<Button>(R.id.btn_apply_wave)
        applyWaveButton.setOnClickListener(this)
    }

    /*
    * Creating engine in onResume() and destroying in onPause() so the stream retains exclusive
    * mode only while in focus. This allows other apps to reclaim exclusive stream mode.
    */
    @Override
    override fun onResume() {
        super.onResume()
        createAndStartEngine()
    }
    @Override
    override fun onPause() {
        stopAndDeleteEngine()
        super.onPause()
    }

    @Override
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_oscillator_activity -> {
                openOscillatorCanvas()
            }
            R.id.btn_apply_wave -> {
                restartAll()
            }
        }
    }

    private fun openOscillatorCanvas() {
        val oscillatorCanvasIntent = Intent(this@MainActivity, OscillatorCanvasActivity::class.java)
        startActivity(oscillatorCanvasIntent)
    }

    private fun createAndStartEngine() {
        create(this, fourierSeries)
        setChannelCount(2) // stereo
        val result = start()
        if (result != 0) {
            println("Error opening stream = $result")
        }
    }

    private fun stopAndDeleteEngine() {
        val result = stop()
        if (result != 0) {
            println("Error stopping stream = $result")
        }
        delete()
    }

    private fun applyWave() {
        // TODO: save coefficients in json
        fourierSeries = ArrayList()
        val oscs = FileUtil.readOscillatorFromFile(this as Activity, "my_oscillators.json")
        if (oscs == null) {
            val oscDataNew = FloatArray(NUM_SAMPLES)
            for (i in 0 until NUM_SAMPLES) {
                oscDataNew[i] = CurveFittingUtil.calculateSineSample(i, b = HALF_WAVE_CYCLE)
            }
            val series = CurveFittingUtil.fit(oscDataNew, NUM_SAMPLES)
            for (i in 0 until 3) {
                fourierSeries.add(series)
            }
        } else {
            for (osc in oscs) {
                osc.oscillator_data?.let {
                    fourierSeries.add(CurveFittingUtil.fit(it, NUM_SAMPLES))
                }
            }
        }
    }

    private fun restartAll() {
        applyWave()
        stopAndDeleteEngine()
        createAndStartEngine()
    }
}