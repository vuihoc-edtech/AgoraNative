package io.agora.flat.event

import io.agora.flat.common.rtm.Message

data class MessagesAppended(val messages: List<Message>) : Event()