package io.vuihoc.agora_native.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

const val CLOUD_ROOT_DIR = "/"

@Parcelize
data class CloudFile(
    val fileUUID: String,
    val fileName: String,
    val fileSize: Long,
    val fileURL: String,
    val resourceType: ResourceType?,
    val createAt: Long,
    val meta: CloudFileMeta? = null,
) : Parcelable {
    val convertStep: FileConvertStep
        get() = when (resourceType) {
            ResourceType.WhiteboardConvert -> whiteboardConvert.convertStep
            ResourceType.WhiteboardProjector -> whiteboardProjector.convertStep
            else -> FileConvertStep.Done
        }
    val whiteboardConvert: WhiteboardConvertPayload
        get() = meta!!.whiteboardConvert!!

    val whiteboardProjector: WhiteboardProjectorPayload
        get() = meta!!.whiteboardProjector!!
}

@Parcelize
data class CloudFileMeta(
    val whiteboardConvert: WhiteboardConvertPayload? = null,
    val whiteboardProjector: WhiteboardProjectorPayload? = null,
) : Parcelable

@Parcelize
data class WhiteboardConvertPayload(
    val region: String,
    val convertStep: FileConvertStep,
    val taskUUID: String,
    val taskToken: String,
) : Parcelable

@Parcelize
data class WhiteboardProjectorPayload(
    val region: String,
    val convertStep: FileConvertStep,
    val taskUUID: String,
    val taskToken: String,
) : Parcelable