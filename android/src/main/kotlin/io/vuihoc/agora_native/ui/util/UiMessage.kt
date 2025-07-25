/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vuihoc.agora_native.ui.util

import java.util.*

enum class UiMessageType {
    Info,
    Error,
}

class UiMessage(
    val text: String = "",
    val exception: Throwable? = null,
    val type: UiMessageType = UiMessageType.Info,
    val id: Long = UUID.randomUUID().mostSignificantBits,
) {
    val isError: Boolean
        get() = type == UiMessageType.Error

    val isInfo: Boolean
        get() = type == UiMessageType.Info
}

