package io.vuihoc.agora_native.event

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * a event bus to manager app events
 */
class EventBus {
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    suspend fun produceEvent(event: Event) {
        Log.d("EventBus", "produceEvent $event")
        _events.emit(event)
    }

    companion object {
        @Volatile
        private var INSTANCE: EventBus? = null

        fun getInstance(): EventBus {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EventBus().also { INSTANCE = it }
            }
        }
    }
}

abstract class Event