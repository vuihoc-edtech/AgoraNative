package io.vuihoc.agora_native.http.interceptor

import io.vuihoc.agora_native.data.AppKVCenter
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder().apply {
            addHeader("Content-type", "application/json; charset=utf-8")
            addHeader("Authorization", "Bearer ${AppKVCenter.getInstance().getToken()}")
            addHeader("x-session-id", AppKVCenter.getInstance().getSessionId())
            addHeader("x-request-id",UUID.randomUUID().toString())
//            for (headerProvider in headerProviders) {
//                for (pair in headerProvider.getHeaders()) {
//                    addHeader(pair.first, pair.second)
//                }
//            }
        }

        return chain.proceed(builder.build())
    }
}