package com.example.papersynth.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.papersynth.R
import com.example.papersynth.views.OscillatorCanvasView

/**
 * A simple [Fragment] subclass.
 * Use the [OscillatorCanvasFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OscillatorCanvasFragment : Fragment(R.layout.fragment_oscillator_canvas) {

    private lateinit var oscillatorCanvasView: OscillatorCanvasView

    @Override
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_oscillator_canvas, container, false)
        oscillatorCanvasView = view.findViewById(R.id.oscillator_canvas_view)

        return view
    }
}