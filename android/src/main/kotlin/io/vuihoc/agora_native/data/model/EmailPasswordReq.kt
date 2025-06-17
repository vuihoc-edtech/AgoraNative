package io.vuihoc.agora_native.data.model

data class EmailPasswordReq(
    val email: String,
    // 8..32 length
    val password: String,
)
