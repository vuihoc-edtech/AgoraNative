package io.vuihoc.agora_native.event

import io.vuihoc.agora_native.common.rtm.Message

data class MessagesAppended(val messages: List<Message>) : Event()