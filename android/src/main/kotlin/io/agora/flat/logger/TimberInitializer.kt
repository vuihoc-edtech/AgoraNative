package io.agora.flat.logger

import android.content.Context
//import io.agora.flat.BuildConfig
import io.agora.flat.di.interfaces.Logger
import io.agora.flat.di.interfaces.StartupInitializer
import javax.inject.Inject

class TimberInitializer @Inject constructor(
    private val logger: Logger,
) : StartupInitializer {

    override fun init(context: Context) {
        //linhndq
//        logger.setup(BuildConfig.DEBUG)
    }
}
