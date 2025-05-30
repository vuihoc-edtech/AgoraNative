package io.vuihoc.agora_native.data.model

data class PhonePasswordReq(
    val phone: String,
    // 8..32 length
    val password: String,
)
