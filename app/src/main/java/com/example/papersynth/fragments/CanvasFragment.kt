package com.example.papersynth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.papersynth.PlaybackEngine
import com.example.papersynth.R
import com.example.papersynth.utils.FileUtil.writeCanvasToFile
import com.example.papersynth.views.CanvasView
import java.util.*

class CanvasFragment : Fragment(R.layout.fragment_canvas), View.OnClickListener {

    private lateinit var canvasView: CanvasView

    private var isOn: Boolean = false

    @Override
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_canvas, container, false)
        canvasView = view.findViewById(R.id.canvas_view)

        initializeButtons()

        return view
    }

    @Override
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_clear -> {
                canvasView.clearCanvas()
            }
            R.id.btn_play_wave -> {
                this.isOn = !this.isOn
                if (this.isOn) {
                    val alphaArray = canvasView.getAlphaArray()
                    PlaybackEngine.setAlphaArray(alphaArray)
                }
                canvasView.sweep(this.isOn)
                PlaybackEngine.setToneOn(this.isOn)
            }
            R.id.btn_save_canvas -> {
                val bitmap = canvasView.getBitmap()
                activity?.let { fragmentActivity ->
                    val now = Calendar.getInstance().time.toString()
                    writeCanvasToFile(fragmentActivity, "$now.png", bitmap)
                }
            }
        }
    }

    private fun initializeButtons() {
        val clearButton = activity?.findViewById<Button>(R.id.btn_clear)
        clearButton?.setOnClickListener(this)
        val waveButton = activity?.findViewById<Button>(R.id.btn_play_wave)
        waveButton?.setOnClickListener(this)
        val saveButton = activity?.findViewById<Button>(R.id.btn_save_canvas)
        saveButton?.setOnClickListener(this)
        val loadButton = activity?.findViewById<Button>(R.id.btn_load_canvas)
        loadButton?.setOnClickListener(this)
    }
}