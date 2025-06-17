package io.vuihoc.agora_native.interfaces

// here only for aliyun log, in the short term
data class LogConfig(
    val ak: String,
    val sk: String,
    val project: String,
    val logstore: String,
    val endpoint: String,
)