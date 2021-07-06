package com.example.papersynth.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.papersynth.R
import com.example.papersynth.utils.FileUtil.writeOscillatorToFile
import com.example.papersynth.views.OscillatorCanvasView
import java.io.*

class OscillatorCanvasFragment : Fragment(R.layout.fragment_oscillator_canvas), View.OnClickListener {

    private lateinit var oscillatorCanvasView: OscillatorCanvasView

    @Override
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_oscillator_canvas, container, false)
        oscillatorCanvasView = view.findViewById(R.id.oscillator_canvas_view)

        initializeButtons()

        return view
    }

    @Override
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_save_oscillator -> {
                val waves = oscillatorCanvasView.getWaves()
                activity?.let { fragmentActivity ->
                    try {
                        writeOscillatorToFile(fragmentActivity, "my_oscillators.json", waves)
                    } catch (e: IOException) {
                        Log.e("Error", "Error occurred while trying to save oscillator as JSON file!")
                    }
                }
            }
            R.id.btn_reset_oscillator -> {
                oscillatorCanvasView.resetOscillator()
            }
            R.id.btn_wave_1 -> {
                oscillatorCanvasView.setCurrentWave(OscillatorCanvasView.WaveEnum.WAVE_1)
            }
            R.id.btn_wave_2 -> {
                oscillatorCanvasView.setCurrentWave(OscillatorCanvasView.WaveEnum.WAVE_2)
            }
            R.id.btn_wave_3 -> {
                oscillatorCanvasView.setCurrentWave(OscillatorCanvasView.WaveEnum.WAVE_3)
            }
        }
    }

    private fun initializeButtons() {
        val saveOscButton = activity?.findViewById<Button>(R.id.btn_save_oscillator)
        saveOscButton?.setOnClickListener(this)
        val testFitButton = activity?.findViewById<Button>(R.id.btn_reset_oscillator)
        testFitButton?.setOnClickListener(this)
        val wave1Button = activity?.findViewById<Button>(R.id.btn_wave_1)
        wave1Button?.setOnClickListener(this)
        val wave2Button = activity?.findViewById<Button>(R.id.btn_wave_2)
        wave2Button?.setOnClickListener(this)
        val wave3Button = activity?.findViewById<Button>(R.id.btn_wave_3)
        wave3Button?.setOnClickListener(this)
    }
}