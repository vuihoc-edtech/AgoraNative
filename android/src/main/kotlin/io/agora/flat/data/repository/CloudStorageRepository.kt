package io.agora.flat.data.repository

import io.agora.flat.data.AppKVCenter
import io.agora.flat.data.Result
import io.agora.flat.data.ServiceFetcher
import io.agora.flat.data.Success
import io.agora.flat.data.model.RespNoData
import io.agora.flat.data.toResult
import io.agora.flat.http.api.CloudStorageServiceV2
import io.agora.flat.http.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloudStorageRepository(
    private val cloudStorageService: CloudStorageServiceV2 = ServiceFetcher.getInstance().fetchCloudStorageServiceV2(),
    private val appKVCenter: AppKVCenter = AppKVCenter.getInstance(),
) {
    private var avatarUrl: String? = null
    companion object {
        @Volatile
        private var INSTANCE: CloudStorageRepository? = null

        fun getInstance(): CloudStorageRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudStorageRepository().also { INSTANCE = it }
            }
        }
    }
    suspend fun createDirectory(dir: String, name: String): Result<RespNoData> {
        return withContext(Dispatchers.IO) {
            cloudStorageService.createDirectory(
                CreateDirectoryReq(parentDirectoryPath = dir, directoryName = name)
            ).toResult()
        }
    }

    suspend fun listFiles(
        page: Int = 1,
        size: Int = 20,
        path: String,
        order: String = "DESC",
    ): Result<CloudListFilesResp> {
        return withContext(Dispatchers.IO) {
            cloudStorageService.listFiles(
                CloudListFilesReq(page = page, size = size, directoryPath = path, order = order)
            ).toResult()
        }
    }

    suspend fun rename(fileUUID: String, name: String): Result<RespNoData> {
        return withContext(Dispatchers.IO) {
            cloudStorageService.rename(CloudFileRenameReq(fileUUID = fileUUID, newName = name)).toResult()
        }
    }

    /**
     * @param fileName
     * @param fileSize
     * @param path DirectoryPath
     */
    suspend fun updateStart(
        fileName: String,
        fileSize: Long,
        path: String,
        convertType: String = "WhiteboardProjector"
    ): Result<CloudUploadStartResp> {
        return withContext(Dispatchers.IO) {
            cloudStorageService.updateStart(
                CloudUploadStartReq(
                    fileName = fileName,
                    fileSize = fileSize,
                    targetDirectoryPath = path,
                    convertType = convertType,
                )
            ).toResult()
        }
    }

    suspend fun updateFinish(fileUUID: String): Result<RespNoData> {
        return withContext(Dispatchers.IO) {
            cloudStorageService.updateFinish(CloudUploadFinishReq(fileUUID)).toResult()
        }
    }

    suspend fun uploadTempFileStart(fileName: String, fileSize: Long): Result<CloudUploadStartResp> {
        return withContext(Dispatchers.IO) {
            cloudStorageService.uploadTempFileStart(
                CloudUploadTempFileStartReq(fileName = fileName, fileSize = fileSize)
            ).toResult()
        }
    }

    suspend fun uploadTempFileFinish(fileUUID: String): Result<RespNoData> {
        return withContext(Dispatchers.IO) {
            cloudStorageService.uploadTempFileFinish(CloudUploadFinishReq(fileUUID)).toResult()
        }
    }

    suspend fun delete(fileUUIDs: List<String>): Result<RespNoData> {
        return withContext(Dispatchers.IO) {
            cloudStorageService.delete(CloudFileDeleteReq(fileUUIDs)).toResult()
        }
    }

    suspend fun convertStart(fileUUID: String): Result<CloudConvertStartResp> {
        return withContext(Dispatchers.IO) {
            cloudStorageService.convertStart(CloudConvertStartReq(fileUUID)).toResult()
        }
    }

    suspend fun convertFinish(fileUUID: String): Result<RespNoData> {
        return withContext(Dispatchers.IO) {
            cloudStorageService.convertFinish(CloudConvertFinishReq(fileUUID)).toResult()
        }
    }
}