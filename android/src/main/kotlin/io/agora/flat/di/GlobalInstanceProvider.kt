package io.agora.flat.di

import android.app.Application
import androidx.room.Room
import io.agora.flat.BuildConfig
import io.agora.flat.common.android.AndroidClipboardController
import io.agora.flat.common.android.AppCoroutineDispatchers
import io.agora.flat.common.android.ClipboardController
import io.agora.flat.common.login.LoginManager
import io.agora.flat.data.AppDatabase
import io.agora.flat.data.AppEnv
import io.agora.flat.data.AppKVCenter
import io.agora.flat.event.EventBus
import io.agora.flat.logger.AliyunLogReporter
import io.agora.flat.logger.BuglyCrashlytics
import io.agora.flat.di.interfaces.NetworkObserver
import kotlinx.coroutines.Dispatchers

object GlobalInstanceProvider {
    private lateinit var applicationContext: Application
    private val instances = mutableMapOf<Class<*>, Any>()

    fun init(application: Application) {
        applicationContext = application
        
        // Initialize core dependencies
        val appEnv = AppEnv(application)
        val appKvCenter = AppKVCenter(application)
        val eventBus = EventBus()
        val dispatchers = AppCoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main
        )
        val clipboardController = AndroidClipboardController(application)
        val loginManager = LoginManager(application, appEnv)
        val database = Room.databaseBuilder(application, AppDatabase::class.java, "flat-database").build()
        
        // Register instances
        register(AppEnv::class.java, appEnv)
        register(AppKVCenter::class.java, appKvCenter)
        register(EventBus::class.java, eventBus)
        register(AppCoroutineDispatchers::class.java, dispatchers)
        register(ClipboardController::class.java, clipboardController)
        register(LoginManager::class.java, loginManager)
        register(AppDatabase::class.java, database)
        
        // Đăng ký NetworkObserver
        val networkObserver = NetworkObserverImpl(application)
        register(NetworkObserver::class.java, networkObserver)
        
        // Logger
        try {
            val crashlytics = BuglyCrashlytics()
            val logReporter = AliyunLogReporter(null)
            val logger = io.agora.flat.logger.FlatLogger(crashlytics, logReporter)
            
            // Khởi tạo các thành phần logger
            crashlytics.init(application)
            logReporter.init(application)
            
            // Setup logger với debug mode
            logger.setup(BuildConfig.DEBUG)
            
            register(io.agora.flat.di.interfaces.Crashlytics::class.java, crashlytics)
            register(io.agora.flat.di.interfaces.LogReporter::class.java, logReporter)
            register(io.agora.flat.di.interfaces.Logger::class.java, logger)
            
            // Khởi tạo Timber
            val timberInitializer = io.agora.flat.logger.TimberInitializer(logger)
            timberInitializer.init(application)
        } catch (e: Exception) {
            // Ignore
        }
    }

    @JvmStatic
    fun getAppKvCenter(): AppKVCenter {
        return get(AppKVCenter::class.java)
    }

    @JvmStatic
    fun getAppEnv(): AppEnv {
        return get(AppEnv::class.java)
    }

    @JvmStatic
    fun getEventBus(): EventBus {
        return get(EventBus::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> get(clazz: Class<T>): T {
        return instances[clazz] as? T ?: throw IllegalStateException("No instance found for ${clazz.name}")
    }

    @JvmStatic
    fun <T> register(clazz: Class<T>, instance: T) {
        instances[clazz] = instance as Any
    }

    // Convenience method for ViewModels to get dependencies
    @JvmStatic
    fun getContext(): Application {
        return applicationContext
    }
}