package io.vuihoc.agora_native.event

import java.util.*

/**
 * notify this event when account user info updated
 * workaround for observe [io.vuihoc.agora_native.data.repository.UserRepository]
 */
data class UserBindingsUpdated(val id: Long = UUID.randomUUID().mostSignificantBits) : Event()