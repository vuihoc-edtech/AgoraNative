package io.agora.flat.ui.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import io.agora.vuihoc.agora_native.R
import io.agora.vuihoc.agora_native.databinding.DialogOwnerExitBinding

class OwnerExitDialog : ClassDialogFragment(R.layout.dialog_owner_exit) {
    private lateinit var binding: DialogOwnerExitBinding
    private var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogOwnerExitBinding.bind(view)

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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismiss()
    }

    interface Listener {
        fun onClose()
        fun onLeftButtonClick()
        fun onRightButtonClick()
        fun onDismiss()
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }
}