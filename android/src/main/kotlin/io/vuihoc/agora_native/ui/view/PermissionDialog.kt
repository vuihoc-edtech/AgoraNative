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
import androidx.core.text.set
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
    }

    private fun createTitle(): String {
        return "Hướng dẫn bật $permission"
    }

    private fun createFirstLine(): SpannableString{
        val fullText = "Vào cài đặt RINO EDU"
        val spannable = SpannableString(fullText)
        return spannable
    }

    private fun createSecondLine(): SpannableString {
        val fullText = "Chọn Quyền > Chọn $permission"
        val start = fullText.indexOf("Quyền")
        val end = start + "Quyền".length
        val spannable = SpannableString(fullText)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val start1 = fullText.indexOf(permission)
        val end1 = start1 + permission.length
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            start1, end1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun createThirdLine(): String {
        return "Chọn cho phép khi dùng ứng dụng"
    }
}