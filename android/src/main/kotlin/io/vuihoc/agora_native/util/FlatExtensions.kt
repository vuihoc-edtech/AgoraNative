package io.vuihoc.agora_native.util

import androidx.annotation.ColorInt
import com.herewhite.sdk.domain.Region
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.data.model.CLOUD_ROOT_DIR
import io.vuihoc.agora_native.data.model.CloudFile
import io.vuihoc.agora_native.data.model.CoursewareType
import io.vuihoc.agora_native.data.model.ResourceType
import java.util.Locale

fun String.fileExtension(): String {
    return substringAfterLast('.').lowercase(Locale.getDefault())
}

fun String.nameWithoutExtension(): String {
    return if (contains('.')) substringBefore('.') else this
}

fun String.coursewareType(): CoursewareType {
    return when (fileExtension()) {
        "jpg", "jpeg", "png", "webp" -> {
            CoursewareType.Image
        }

        "doc", "docx", "pdf", "ppt" -> {
            CoursewareType.DocStatic
        }

        "pptx" -> {
            CoursewareType.DocDynamic
        }

        "mp3" -> {
            CoursewareType.Audio
        }

        "mp4" -> {
            CoursewareType.Video
        }

        else -> {
            CoursewareType.Unknown
        }
    }
}

fun CloudFile.fileIcon(): Int {
    return when (this.fileURL.fileExtension()) {
        "jpg", "jpeg", "png", "webp" -> R.drawable.ic_cloud_file_image
        "ppt", "pptx" -> R.drawable.ic_cloud_file_ppt
        "doc", "docx" -> R.drawable.ic_cloud_file_word
        "pdf" -> R.drawable.ic_cloud_file_pdf
        "mp4" -> R.drawable.ic_cloud_file_video
        "mp3", "aac" -> R.drawable.ic_cloud_file_audio
        else -> {
            if (this.resourceType == ResourceType.Directory) {
                R.drawable.ic_cloud_file_folder
            } else {
                R.drawable.ic_cloud_file_others
            }
        }
    }
}


fun String.isDynamicDoc(): Boolean {
    return this.coursewareType() == CoursewareType.DocDynamic
}

fun String.toInviteCodeDisplay() = if (length == 11) {
    "${substring(IntRange(0, 3))} ${substring(IntRange(4, 6))} ${substring(7)}"
} else {
    this
}

internal const val ROOM_UUID_PATTERN = """[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"""
internal const val INVITE_CODE_PATTERN = """[0-9]{3} [0-9a-fA-F]{3} [0-9a-fA-F]{4}"""
internal const val SIMPLE_PHONE_PATTERN = """^([0-9]+)${'$'}"""
internal const val PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$" // 8位以上，包含字母和数字
internal const val SIMPLE_EMAIl_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$" // 8位以上，包含字母和数字

fun String.parseRoomID(): String? {
    if (this.isBlank()) {
        return null
    }
    return parseInviteCode(this) ?: parseRoomUUID(this)
}

internal fun parseInviteCode(text: CharSequence): String? {
    val regex = INVITE_CODE_PATTERN.toRegex()
    val entire = regex.find(text)
    return entire?.value
}

internal fun parseRoomUUID(text: CharSequence): String? {
    val regex = ROOM_UUID_PATTERN.toRegex()
    val entire = regex.find(text)
    return entire?.value
}

fun String.isValidPhone(): Boolean {
    return SIMPLE_PHONE_PATTERN.toRegex().matches(this)
}

fun String.isValidEmail(): Boolean {
    return SIMPLE_EMAIl_PATTERN.toRegex().matches(this)
}

fun String.isValidSmsCode(): Boolean {
    return this.length == 6
}

fun String.isValidVerifyCode(): Boolean {
    return this.length == 6
}

fun String.isValidPassword(): Boolean {
    return PASSWORD_PATTERN.toRegex().matches(this)
}

/**
 * just for cloud files
 */
fun String.parentFolder(): String {
    val path = this
    if (CLOUD_ROOT_DIR == path) return CLOUD_ROOT_DIR
    val endsWithSlash = path.endsWith('/')
    val index = path.lastIndexOf('/', if (endsWithSlash) path.length - 2 else path.length - 1)
    return path.substring(0, index + 1)
}

fun String.folderName(): String {
    val folder = this
    return folder.split('/').findLast { it != "" } ?: ""
}

fun String.toRegion(): Region {
    return when (this) {
        "cn-hz" -> Region.cn
        "us-sv" -> Region.us
        "sg" -> Region.sg
        "in-mum" -> Region.in_mum
        "gb-lon" -> Region.gb_lon
        else -> Region.cn
    }
}

fun Int.toHex(): String {
    return String.format("#%08X", this)
}

fun Int.toRgbHex(): String {
    return String.format("#%06X", this and 0xFFFFFF)
}