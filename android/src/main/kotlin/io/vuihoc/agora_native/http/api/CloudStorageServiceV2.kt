package io.vuihoc.agora_native.http.api

import io.vuihoc.agora_native.data.model.BaseResp
import io.vuihoc.agora_native.data.model.RespNoData
import io.vuihoc.agora_native.http.model.CloudConvertFinishReq
import io.vuihoc.agora_native.http.model.CloudConvertStartReq
import io.vuihoc.agora_native.http.model.CloudConvertStartResp
import io.vuihoc.agora_native.http.model.CloudFileDeleteReq
import io.vuihoc.agora_native.http.model.CloudFileMoveReq
import io.vuihoc.agora_native.http.model.CloudFileRenameReq
import io.vuihoc.agora_native.http.model.CloudListFilesReq
import io.vuihoc.agora_native.http.model.CloudListFilesResp
import io.vuihoc.agora_native.http.model.CloudUploadFinishReq
import io.vuihoc.agora_native.http.model.CloudUploadStartReq
import io.vuihoc.agora_native.http.model.CloudUploadStartResp
import io.vuihoc.agora_native.http.model.CloudUploadTempFileStartReq
import io.vuihoc.agora_native.http.model.CreateDirectoryReq
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CloudStorageServiceV2 {
    @POST("v2/cloud-storage/create-directory")
    fun createDirectory(
        @Body req: CreateDirectoryReq,
    ): Call<BaseResp<RespNoData>>

    @POST("v2/cloud-storage/list")
    fun listFiles(
        @Body req: CloudListFilesReq,
    ): Call<BaseResp<CloudListFilesResp>>

    @POST("v2/cloud-storage/rename")
    fun rename(
        @Body req: CloudFileRenameReq,
    ): Call<BaseResp<RespNoData>>

    @POST("v2/cloud-storage/move")
    fun move(
        @Body req: CloudFileMoveReq,
    ): Call<BaseResp<RespNoData>>

    @POST("v2/cloud-storage/delete")
    fun delete(
        @Body req: CloudFileDeleteReq,
    ): Call<BaseResp<RespNoData>>

    @POST("v2/cloud-storage/upload/start")
    fun updateStart(
        @Body req: CloudUploadStartReq,
    ): Call<BaseResp<CloudUploadStartResp>>

    @POST("/v2/cloud-storage/upload/finish")
    fun updateFinish(
        @Body req: CloudUploadFinishReq,
    ): Call<BaseResp<RespNoData>>

    @POST("/v2/temp-photo/upload/start")
    fun uploadTempFileStart(
        @Body req: CloudUploadTempFileStartReq,
    ): Call<BaseResp<CloudUploadStartResp>>

    @POST("/v2/temp-photo/upload/finish")
    fun uploadTempFileFinish(
        @Body req: CloudUploadFinishReq,
    ): Call<BaseResp<RespNoData>>

    @POST("v2/cloud-storage/convert/start")
    fun convertStart(
        @Body req: CloudConvertStartReq,
    ): Call<BaseResp<CloudConvertStartResp>>

    @POST("v2/cloud-storage/convert/finish")
    fun convertFinish(
        @Body req: CloudConvertFinishReq,
    ): Call<BaseResp<RespNoData>>

    @POST("v2/user/upload-avatar/start")
    fun updateAvatarStart(
        @Body req: CloudUploadTempFileStartReq,
    ): Call<BaseResp<CloudUploadStartResp>>

    @POST("v2/user/upload-avatar/finish")
    fun updateAvatarFinish(
        @Body req: CloudUploadFinishReq,
    ): Call<BaseResp<RespNoData>>
}