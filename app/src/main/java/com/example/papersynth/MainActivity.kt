package com.example.papersynth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit

class MainActivity : AppCompatActivity() {
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
                null)
        )

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container_view, CanvasFragment())
            .commit()
    }
}