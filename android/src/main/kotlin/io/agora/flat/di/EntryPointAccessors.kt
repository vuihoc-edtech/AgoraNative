package io.agora.flat.di

import android.content.Context

/**
 * Class thay thế cho EntryPointAccessors của Hilt
 * Thay thế cho dagger.hilt.android.EntryPointAccessors
 */
object EntryPointAccessors {
    /**
     * Phương thức thay thế cho GlobalInstanceProvider.get
     */
    @JvmStatic
    fun <T> fromApplication(context: Context, clazz: Class<T>): T {
        return GlobalInstanceProvider.get(clazz)
    }
    
    /**
     * Phương thức thay thế cho GlobalInstanceProvider.get
     */
    @JvmStatic
    fun <T> fromActivity(context: Context, clazz: Class<T>): T {
        return GlobalInstanceProvider.get(clazz)
    }

    /**
     * Phương thức thay thế cho GlobalInstanceProvider.get
     */
    @JvmStatic
    fun <T> fromFragment(context: Context, clazz: Class<T>): T {
        return GlobalInstanceProvider.get(clazz)
    }
} 