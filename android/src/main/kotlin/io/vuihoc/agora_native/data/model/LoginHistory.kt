package io.vuihoc.agora_native.data.model

data class LoginHistory(
    val items: List<LoginHistoryItem> = emptyList()
)

data class LoginHistoryItem(
    val value: String,
    val password: String,
)
