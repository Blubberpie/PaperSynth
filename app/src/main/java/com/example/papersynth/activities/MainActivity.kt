package com.example.papersynth.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.papersynth.jni.PlaybackEngine.create
import com.example.papersynth.jni.PlaybackEngine.delete
import com.example.papersynth.jni.PlaybackEngine.setChannelCount
import com.example.papersynth.jni.PlaybackEngine.start
import com.example.papersynth.jni.PlaybackEngine.stop
import com.example.papersynth.R
import com.example.papersynth.enums.MusicalScale
import com.example.papersynth.fragments.CanvasFragment
import com.example.papersynth.fragments.ScalesDialog
import com.example.papersynth.utils.FileUtil
import com.example.papersynth.viewmodels.ScaleViewModel

private const val NUM_SAMPLES = 256

class MainActivity : AppCompatActivity(), View.OnClickListener, ScalesDialog.ScalesDialogListener {

    private lateinit var waveForms: ArrayList<FloatArray>

    private val scaleViewModel: ScaleViewModel by viewModels()
    private var selectedScale = MusicalScale.CHROMATIC
    private var canvasHeight = 88

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
        val chooseScaleButton = findViewById<Button>(R.id.btn_choose_scale)
        chooseScaleButton.setOnClickListener(this)
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
                applyWave()
                restartAll()
            }
            R.id.btn_choose_scale -> {
                ScalesDialog().show(supportFragmentManager, "ScalesDialog")
            }
        }
    }

    /// DIALOG FUNCTIONS ///

    override fun onDialogCancelClick(dialog: DialogFragment) {
        println("cancelled")
    }

    override fun onSelectScale(dialog: DialogFragment, selectedScale: MusicalScale) {
        scaleViewModel.setScale(selectedScale)
        this.selectedScale = selectedScale
        recalculateCanvasHeight()
        restartAll()
    }

    /// MAIN ACTIVITY FUNCTIONS ///

    private fun openOscillatorCanvas() {
        val oscillatorCanvasIntent = Intent(this@MainActivity, OscillatorCanvasActivity::class.java)
        startActivity(oscillatorCanvasIntent)
    }

    private fun createAndStartEngine() {
        create(this, waveForms, selectedScale, canvasHeight)
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
        waveForms = ArrayList()
        val oscs = FileUtil.readOscillatorFromFile(this as Activity, "my_oscillators.json")
        if (oscs == null) {
            val oscDataNew = FloatArray(NUM_SAMPLES)
            for (i in 0 until 3) {
                waveForms.add(oscDataNew)
            }
        } else {
            for (osc in oscs) {
                osc.oscillator_data?.let {
                    waveForms.add(it)
                }
            }
        }
    }

    private fun restartAll() {
        stopAndDeleteEngine()
        createAndStartEngine()
    }

    private fun recalculateCanvasHeight() {
        canvasHeight = if (selectedScale.compareTo(MusicalScale.CHROMATIC) == 0) {
            88
        } else if (
            selectedScale.compareTo(MusicalScale.WHOLE_TONE) == 0
            || selectedScale.compareTo(MusicalScale.BLUES_HEXATONIC) == 0
        ) {
            42 // 6
        } else if (
            selectedScale.compareTo(MusicalScale.AKEBONO) == 0
            || selectedScale.compareTo(MusicalScale.PENTATONIC) == 0
        ) {
            35 // 5
        } else {
            50 // 7
        }
    }
}