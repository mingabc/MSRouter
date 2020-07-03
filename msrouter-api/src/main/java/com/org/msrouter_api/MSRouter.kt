package com.org.msrouter_api;

import android.app.Application
import android.net.Uri
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.template.ILogger
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.android.arouter.utils.ClassUtils
import com.org.msrouter_api.metamap.IRouteRegister
import com.org.msrouter_api.metamap.WhiteHouse
import com.org.msrouter_api.utils.MSConst
import com.org.msrouter_api.metamap.*


/**
 * @ClassName: RTRouter
 * @Description: router
 * @Author: ms
 * @Date: 2020-01-02 16:57
 */
public class MSRouter {

    companion object {

        private lateinit var application: Application
        public fun init(context: Application){
            ARouter.init(context)
            //设置默认ILogger(base RTLogger)，外部可通过接口覆盖
            application = context
            //加载路由map
            loadServiceMap(context)
        }

        public fun openLog() {
            ARouter.openLog()
        }

        public fun openDebug() {
            ARouter.openDebug()
        }
        public fun printStackTrace() {
            ARouter.printStackTrace()
        }

        // 使用自己的日志工具打印日志
        public fun setLogger(userLogger: ILogger?) {
            ARouter.setLogger(userLogger)
        }


        public fun request(path: String, param: Any? = null): Request {
            return Uri.parse(path).run { Request(this, param) }
        }

        public fun request(uri: Uri, param: Any? = null): Request {
            return Request(uri, param)
        }

        public fun build(path: String): Postcard {
            return ARouter.getInstance().build(path)
        }

        public fun build(uri: Uri): Postcard {
            return ARouter.getInstance().build(uri)
        }

        fun inject(thiz: Any?) {
            ARouter.getInstance().inject(thiz)
        }

        /*
        * 初始化request.service注册表*/
        private fun loadServiceMap(context: Application) {
            //加载 package对应下的所有class
            val routerMap = ClassUtils.getFileNameByPackageName(application, MSConst.kServiceRegisterPackage)
            for (className in routerMap) {
                if (className.endsWith(MSConst.kProviderSuffixKey)) {
                    (Class.forName(className).getConstructor().newInstance() as? IRouteRegister)?.loadInto(
                        WhiteHouse.methodMap
                    )
                }
            }
        }

    }

}
