package com.example.papersynth.fragments

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.papersynth.PlaybackEngine
import com.example.papersynth.R
import com.example.papersynth.utils.FileUtil.readCanvasFromPath
import com.example.papersynth.utils.FileUtil.writeCanvasToFile
import com.example.papersynth.views.CanvasView
import com.thebluealliance.spectrum.SpectrumDialog
import java.text.DateFormat
import java.util.*


class CanvasFragment : Fragment(R.layout.fragment_canvas), View.OnClickListener {

    private lateinit var canvasView: CanvasView
    private lateinit var colors: IntArray

    private var waveButton: Button? = null
    private var clearButton: Button? = null
    private var colorPickerButton: Button? = null

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
        colors = intArrayOf(
            ResourcesCompat.getColor(resources, R.color.brushL, null),
            ResourcesCompat.getColor(resources, R.color.redD, null),
            ResourcesCompat.getColor(resources, R.color.orangeD, null),
            ResourcesCompat.getColor(resources, R.color.yellowD, null),
            ResourcesCompat.getColor(resources, R.color.greenD, null),
            ResourcesCompat.getColor(resources, R.color.tealD, null),
            ResourcesCompat.getColor(resources, R.color.blueD, null),
            ResourcesCompat.getColor(resources, R.color.purple_200, null),
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.CYAN,
            Color.MAGENTA,
            Color.YELLOW
        )

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
                    val pixelsArray = canvasView.getPixelsArray()
                    PlaybackEngine.setPixelsArray(pixelsArray)
                    waveButton?.let {
                        it.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.redD, null))
                        it.text = "STOP"
                    }
                    clearButton?.let {
                        it.isEnabled = false
                        it.alpha = 0.5f
                    }
                } else {
                    waveButton?.let {
                        it.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.greenD, null))
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
            R.id.btn_color_picker -> {
                SpectrumDialog.Builder(context)
                    .setColors(colors)
                    .setSelectedColor(canvasView.getCurrentColor())
                    .setDismissOnColorSelected(true)
                    .setOutlineWidth(2)
                    .setOnColorSelectedListener { positiveResult, color -> handleColorPicked(positiveResult, color) }
                    .build().show(parentFragmentManager, "dialog_demo_1")
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
        colorPickerButton = activity?.findViewById(R.id.btn_color_picker)
        colorPickerButton?.setOnClickListener(this)
    }

    private fun handleReadCanvas(uri: Uri?) {
        activity?.let { fragmentActivity ->
            val loadedBitmap = readCanvasFromPath(fragmentActivity, uri?.path)
            canvasView.applyLoadedBitmap(loadedBitmap)
        }
    }

    private fun handleColorPicked(positiveResult: Boolean, color: Int) {
        if (positiveResult) {
            canvasView.setCurrentColor(color)
        }
    }
}