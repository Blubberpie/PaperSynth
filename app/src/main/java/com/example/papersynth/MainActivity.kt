package com.example.papersynth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.papersynth.PlaybackEngine.create
import com.example.papersynth.PlaybackEngine.delete
import com.example.papersynth.PlaybackEngine.setChannelCount
import com.example.papersynth.PlaybackEngine.start
import com.example.papersynth.PlaybackEngine.stop


class MainActivity : AppCompatActivity(), View.OnClickListener {

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

        val oscillatorActivityButton = findViewById<Button>(R.id.btn_oscillator_activity)
        oscillatorActivityButton.setOnClickListener(this)
    }

    /*
    * Creating engine in onResume() and destroying in onPause() so the stream retains exclusive
    * mode only while in focus. This allows other apps to reclaim exclusive stream mode.
    */
    @Override
    override fun onResume() {
        super.onResume()
        create(this)
        setChannelCount(2) // stereo
        val result = start()
        if (result != 0) {
            println("Error opening stream = $result")
        }
    }

    @Override
    override fun onPause() {
        val result = stop()
        if (result != 0) {
            println("Error stopping stream = $result")
        }
        delete()
        super.onPause()
    }

    @Override
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_oscillator_activity -> {
                openOscillatorCanvas()
            }
        }
    }

    private fun openOscillatorCanvas() {
        val oscillatorCanvasIntent = Intent(this@MainActivity, OscillatorCanvasActivity::class.java)
        startActivity(oscillatorCanvasIntent)
    }
}