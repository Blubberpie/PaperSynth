package com.example.papersynth.fragments

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.example.papersynth.PlaybackEngine
import com.example.papersynth.R
import com.example.papersynth.utils.FileUtil.readCanvasFromPath
import com.example.papersynth.utils.FileUtil.writeCanvasToFile
import com.example.papersynth.views.CanvasView
import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class CanvasFragment : Fragment(R.layout.fragment_canvas), View.OnClickListener {

    private lateinit var canvasView: CanvasView

    private var waveButton: Button? = null
    private var clearButton: Button? = null
    private var isOn: Boolean = false

    val loadCanvas = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        handleReadCanvas(uri)
    }

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
                    waveButton?.let {
                        it.setBackgroundColor(resources.getColor(R.color.orangeD))
                        it.text = "STOP"
                    }
                    clearButton?.let {
                        it.isEnabled = false
                        it.alpha = 0.5f
                    }
                } else {
                    waveButton?.let {
                        it.setBackgroundColor(resources.getColor(R.color.tealD))
                        it.text = "PLAY"
                    }
                    clearButton?.let {
                        it.isEnabled = true
                        it.alpha = 1f
                    }
                }
                canvasView.sweep(this.isOn)
                PlaybackEngine.setToneOn(this.isOn)
            }
            R.id.btn_save_canvas -> {
                val bitmap = canvasView.getBitmap()
                activity?.let { fragmentActivity ->
                    val date: Date = Calendar.getInstance().time
                    var now = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.US).format(date)
                    now = now.replace("[:,\\s]+".toRegex(), "_")
                    writeCanvasToFile(fragmentActivity, "$now.png", bitmap)
                }
            }
            R.id.btn_load_canvas -> {
                loadCanvas.launch("image/png")
            }
            R.id.btn_toggle_grid -> {
                canvasView.toggleGrid()
            }
        }
    }

    private fun initializeButtons() {
        clearButton = activity?.findViewById(R.id.btn_clear)
        clearButton?.setOnClickListener(this)
        waveButton = activity?.findViewById(R.id.btn_play_wave)
        waveButton?.setOnClickListener(this)
        val saveButton = activity?.findViewById<Button>(R.id.btn_save_canvas)
        saveButton?.setOnClickListener(this)
        val loadButton = activity?.findViewById<Button>(R.id.btn_load_canvas)
        loadButton?.setOnClickListener(this)
        val toggleGridButton = activity?.findViewById<Button>(R.id.btn_toggle_grid)
        toggleGridButton?.setOnClickListener(this)
    }

    private fun handleReadCanvas(uri: Uri?) {
        activity?.let { fragmentActivity ->
            val loadedBitmap = readCanvasFromPath(fragmentActivity, uri?.path)
            canvasView.applyLoadedBitmap(loadedBitmap)
        }
    }
}