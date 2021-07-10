package com.example.papersynth.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.papersynth.enums.MusicalScale

class ScalesDialog: DialogFragment() {

    internal lateinit var listener: ScalesDialogListener

    interface ScalesDialogListener {
        fun onDialogCancelClick(dialog: DialogFragment)
        fun onSelectScale(dialog: DialogFragment, selectedScale: MusicalScale)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            val musicalScalesStrings = MusicalScale.getStrings()

            builder
                .setTitle("Select a Scale")
                .setItems(musicalScalesStrings
                ) { _, which ->
                    listener.onSelectScale(
                        this,
                        MusicalScale.valueOf(musicalScalesStrings[which])
                    )
                }
                .setNegativeButton("Cancel"
                ) { _, _ ->
                    listener.onDialogCancelClick(this)
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as ScalesDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }
}