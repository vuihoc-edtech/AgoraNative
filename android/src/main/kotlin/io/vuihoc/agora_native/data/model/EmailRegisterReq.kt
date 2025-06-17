package io.vuihoc.agora_native.data.model

data class EmailRegisterReq(
    val email: String,
    val code: String,
    // 8..32 length
    val password: String,
)
