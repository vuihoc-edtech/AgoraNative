package io.vuihoc.agora_native.ui.view

import android.app.Dialog
import android.os.Bundle
import android.view.View
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.databinding.DialogReplayExitBinding

class ReplayExitDialog : ClassDialogFragment(R.layout.dialog_replay_exit) {
    private lateinit var binding: DialogReplayExitBinding
    private var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogReplayExitBinding.bind(view)

        binding.leftButton.setOnClickListener {
            listener?.onLeftButtonClick()
            dismiss()
        }

        binding.rightButton.setOnClickListener {
            listener?.onRightButtonClick()
            dismiss()
        }

        binding.close.setOnClickListener {
            listener?.onClose()
            dismiss()
        }
    }

    interface Listener {
        fun onClose()
        fun onLeftButtonClick()
        fun onRightButtonClick()
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }
}