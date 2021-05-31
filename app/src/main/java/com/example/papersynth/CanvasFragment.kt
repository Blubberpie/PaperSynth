package com.example.papersynth

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream

class CanvasFragment : Fragment(R.layout.fragment_canvas), View.OnClickListener {

    private lateinit var canvasView: CanvasView

    @Override
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_canvas, container, false)
        canvasView = view.findViewById(R.id.canvas_view)

        val testButton = activity?.findViewById<Button>(R.id.btn_test)
        testButton?.setOnClickListener(this)

        return view
    }

    @Override
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_test -> {
                val bitmap = canvasView.getBitmap()
                activity?.let { fragmentActivity ->
                    ActivityCompat.requestPermissions(fragmentActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

                    val filename = "test.png"
                    val file = File(fragmentActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PaperSynth")

                    if (!file.mkdirs()) {
                        Log.e("Directory error", "Directory not created")
                    }

                    FileOutputStream(File(file, filename)).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                }
            }
        }
    }
}