package io.vuihoc.agora_native.ui.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.databinding.LayoutConfirmExitBinding

class AidienceExitDialog : ClassDialogFragment(R.layout.layout_confirm_exit) {
    private lateinit var binding: LayoutConfirmExitBinding
    private var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LayoutConfirmExitBinding.bind(view)

        binding.buttonCancel.setOnClickListener {
            listener?.onLeftButtonClick()
            dismiss()
        }

        binding.buttonConfirm.setOnClickListener {
            listener?.onRightButtonClick()
            dismiss()
        }

        binding.buttonClose.setOnClickListener {
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