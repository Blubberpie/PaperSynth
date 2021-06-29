package com.example.papersynth.utils

import android.Manifest
import android.graphics.Bitmap
import android.os.Environment
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.papersynth.dataclasses.Oscillator
import java.io.*

object FileUtil {

    /// PUBLIC ///

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
    fun readOscillatorFromFile(
        fragmentActivity: FragmentActivity,
        filename: String
    ) : FloatArray? {

        ActivityCompat.requestPermissions(fragmentActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        var floatArr: FloatArray?
        val dir = File(fragmentActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "PaperSynth")

        if (!dir.exists()) {
            throw FileNotFoundException("PaperSynth directory doesn't exist in Documents")
        } else {
            val file = File(dir, filename)
            if (!file.exists()) {
                throw FileNotFoundException("Filename $filename doesn't exist in the PaperSynth directory")
            } else {
                FileInputStream(file).use { fis ->
                    val reader = JsonReader(InputStreamReader(fis, "UTF-8"))

                    val oscillators: MutableList<Oscillator> = ArrayList()

                    reader.beginArray()
                    while(reader.hasNext()) {
                        oscillators.add(readOscillator(reader))
                    }
                    reader.endArray()

                    floatArr = oscillators[0].oscillator_data
                }
            }
        }
        return floatArr
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

    /// PRIVATE ///

    @Throws(IOException::class)
    private fun writeOscillatorObject(writer: JsonWriter, data: FloatArray) {
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

    @Throws(IOException::class)
    private fun readOscillator(reader: JsonReader): Oscillator {
        var oscName: String? = null
        var oscData: FloatArray? = null

        reader.beginObject()
        while(reader.hasNext()) {
            val name = reader.nextName()
            if (name.equals("name")) {
                oscName = reader.nextString()
            } else if (name.equals("oscillator_data")) {
                oscData = readOscillatorDataArray(reader)
            }
        }
        reader.endObject()
        return Oscillator(oscName, oscData)
    }

    @Throws(IOException::class)
    private fun readOscillatorDataArray(reader: JsonReader): FloatArray {
        val arr: MutableList<Float> = ArrayList()

        reader.beginArray()
        while (reader.hasNext()) {
            arr.add(reader.nextDouble().toFloat())
        }
        reader.endArray()

        return arr.toFloatArray()
    }
}