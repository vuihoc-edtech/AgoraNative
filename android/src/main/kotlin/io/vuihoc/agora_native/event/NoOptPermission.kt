package io.vuihoc.agora_native.event

import java.util.*

data class NoOptPermission(val id: Long = UUID.randomUUID().mostSignificantBits) : Event()
