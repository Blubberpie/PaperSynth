package com.example.papersynth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.papersynth.enums.MusicalScale

class ScaleViewModel: ViewModel() {
    private val mutableSelectedScale = MutableLiveData(MusicalScale.CHROMATIC)

    val selectedScale: LiveData<MusicalScale> get() = mutableSelectedScale

    fun setScale(scale: MusicalScale) {
        mutableSelectedScale.value = scale
    }
}