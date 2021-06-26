package com.example.papersynth.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.papersynth.R
import com.example.papersynth.fragments.CanvasFragment
import com.example.papersynth.fragments.OscillatorCanvasFragment

class OscillatorCanvasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add oscillator canvas fragment
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<CanvasFragment>(R.id.osc_canvas_fragment_container_view)
            }
        }

        supportFragmentManager
            .beginTransaction()
            .add(R.id.osc_canvas_fragment_container_view, OscillatorCanvasFragment())
            .commit()

        setContentView(R.layout.activity_oscillator_canvas)
    }
}