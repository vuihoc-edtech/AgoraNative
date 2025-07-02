package io.vuihoc.agora_native.ui.view

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.DrawableRes
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.databinding.DialogPermissionBinding


class PermissionDialog(val permission: String, @DrawableRes val icon: Int ) : ClassDialogFragment(R.layout.dialog_permission) {
    private lateinit var binding: DialogPermissionBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogPermissionBinding.bind(view)
        binding.icon.setImageResource(icon)
        binding.title.text = createTitle()
        binding.firstLine.text = createFirstLine()
        binding.secondLine.text = createSecondLine()
        binding.thirdLine.text = createThirdLine()
        context?.let { ctx ->
            binding.button.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", ctx.packageName, null)
                intent.apply {
                    data = uri
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(intent)
                dismiss()
            }
        }

        binding.close.setOnClickListener {
            dismiss()
        }
    }

    private fun createTitle(): String {
        return "Hướng dẫn cấp quyền $permission"
    }

    private fun createFirstLine(): SpannableString{
        val fullText = "1. Vào cài đặt ứng dụng RINO EDU"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf("RINO EDU")
        val end = start + "RINO EDU".length

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun createSecondLine(): String {
        return "2. Chọn quyền $permission"
    }

    private fun createThirdLine(): String {
        return "3. Chọn cho phép sử dụng $permission"
    }
}