package com.org.msrouter

import android.app.Application
import com.org.msrouter_api.MSRouter

/**
 * @ClassName: App
 * @Description: app
 * @Author: ms
 * @Date: 2020/07/02 3:32 PM
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        initRouter()
    }

    private fun initRouter() {
        // 这两行必须写在init之前，否则这些配置在init过程中将无效
        MSRouter.openLog();     // 打印日志
        MSRouter.openDebug()
        MSRouter.init(this); // 尽可能早，推荐在Application中初始化
    }
}

