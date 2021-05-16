package com.example.papersynth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val canvasView = CanvasView(this)

        // Deprecated in API 30 but not 19 (current minSdkVersion)
        canvasView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        canvasView.contentDescription = getString(R.string.canvasContentDescription)
        setContentView(canvasView)

        // Show/hide keyboard code (for reference)
        // You have to wait for the view to be attached to the
        // window (otherwise, windowInsetController will be null)
//        view.doOnLayout {
//            view.windowInsetsController?.show(WindowInsets.Type.ime())
//            // You can also access it from Window
//            window.insetsController?.show(WindowInsets.Type.ime())
//        }
    }
}