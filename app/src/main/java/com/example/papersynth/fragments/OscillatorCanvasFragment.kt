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

        val saveOscButton = activity?.findViewById<Button>(R.id.btn_save_oscillator)
        saveOscButton?.setOnClickListener(this)
        val testFitButton = activity?.findViewById<Button>(R.id.btn_reset_oscillator)
        testFitButton?.setOnClickListener(this)

        return view
    }

    @Override
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_save_oscillator -> {
                val oscArr = oscillatorCanvasView.getOscillator()
                activity?.let { fragmentActivity ->
                    try {
                        writeOscillatorToFile(fragmentActivity, "my_oscillators.json", oscArr)
                    } catch (e: IOException) {
                        Log.e("Error", "Error occurred while trying to save oscillator as JSON file!")
                    }
                }
            }
            R.id.btn_reset_oscillator -> {
                oscillatorCanvasView.resetOscillator()
            }
        }
    }
}