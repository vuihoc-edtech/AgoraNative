package io.agora.flat.data.model

data class PhoneRegisterReq(
    val phone: String,
    val code: String,
    // 8..32 length
    val password: String,
)
