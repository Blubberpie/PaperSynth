package com.example.papersynth.utils

import android.Manifest
import android.graphics.Bitmap
import android.os.Environment
import android.util.JsonWriter
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

object FileWriterUtil {

    @Throws(IOException::class)
    fun writeOscillatorToFile(
        fragmentActivity: FragmentActivity,
        filename: String,
        data: FloatArray
    ) {
        ActivityCompat.requestPermissions(fragmentActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        val file = File(fragmentActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "PaperSynth")

        if (!file.mkdirs()) {
            Log.e("Directory error", "Directory not created")
        }

        FileOutputStream(File(file, filename)).use { fos ->
            val writer = JsonWriter(OutputStreamWriter(fos, "UTF-8"))
            writer.setIndent("  ")
            writeOscillatorObject(writer, data)
            writer.close()
        }
    }

    @Throws(IOException::class)
    fun writeOscillatorObject(writer: JsonWriter, data: FloatArray) {
        writer.beginArray()

        writer.beginObject()
        writer.name("name").value("oscillator_1")
        writer.name("oscillator_data")
        writer.beginArray()
        for (x in data) {
            writer.value(x)
        }
        writer.endArray()
        writer.endObject()

        writer.endArray()
    }

    fun writeCanvasToFile(
        fragmentActivity: FragmentActivity,
        filename: String,
        data: Bitmap
    ) {
        ActivityCompat.requestPermissions(fragmentActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        val file = File(fragmentActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PaperSynth")

        if (!file.mkdirs()) {
            Log.e("Directory error", "Directory not created")
        }

        FileOutputStream(File(file, filename)).use { fos ->
            data.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
    }
}