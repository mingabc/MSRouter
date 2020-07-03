package com.org.msrouter_api.metamap;

import android.net.Uri
import com.alibaba.android.arouter.launcher.ARouter
import com.org.msrouter_api.utils.MSConst
import com.org.msrouter_api.arouter.JSONServiceImpl
import java.lang.Exception
import java.lang.reflect.Method

/**
 * @ClassName: Request
 * @Description: route request include path
 * @Author: ms
 * @Date: 2020-07-02 15:01
 */

class Request(private val uri: Uri, paramModel: Any? = null) {
    var params: HashMap<Any?, Any?>? = null

    init {
        paramModel?.also {
            val json = JSONServiceImpl.gson.toJson(it)
            params = HashMap(JSONServiceImpl.gson.fromJson(json, Map::class.java))
        }
    }

    fun <R>response(responseCls: Class<R>, completion: (R?) -> Unit)   {
        val path = uri.path
        if (uri.path.isNullOrEmpty()) {
            val failureMap = HashMap<String, Any?>()
            failureMap["code"]  = "${MSConst.kRouteEmptyErrorCode}"
            failureMap["msg"] = "path不能为空"
            result(failureMap, responseCls, completion)
            return
        }
        val servicePostcard = ARouter.getInstance().build(path)
        val moduleKey = servicePostcard.group

        val instance = servicePostcard.navigation()

        if (instance == null){
            val failureMap = HashMap<String, Any?>()
            failureMap["code"]  = MSConst.kRouteServiceNotFoundErrorCode.toString()
            failureMap["msg"] = "route service not found $path"
            result(failureMap, responseCls, completion)
            return;
        }
        //查找对应接口
        val methodKey = uri.getQueryParameter(MSConst.kOptionKey)
        val serviceCls = instance.javaClass
        if (moduleKey.isEmpty() || methodKey.isNullOrEmpty()){
            val failureMap = HashMap<String, Any?>()
            failureMap["code"]  = MSConst.kRouteUriParserErrorCode.toString()
            failureMap["msg"] = "path解析异常 $path"
            result(failureMap, responseCls, completion)
            return
        }
        val groupMap = WhiteHouse.methodMap[moduleKey]
        var methodName: String? = null
        if (groupMap?.contains(methodKey) == true){
            methodName = groupMap[methodKey]
        }
        if (methodName.isNullOrEmpty()){
            val failureMap = HashMap<String, Any?>()
            failureMap["code"]  = MSConst.kRouteUriParserErrorCode.toString()
            failureMap["msg"] = "path解析异常 $path"
            result(failureMap, responseCls, completion)
            return
        }
        try {
            val kCompletion: kCompletion = { res ->
                result(res, responseCls, completion)
            }
            val paramCls = java.util.HashMap::class.java// Class.forName(kHashMapClassName)
            val completionCls = kotlin.jvm.functions.Function1::class.java//Class.forName(kCompletionClassName)
            val method: Method?
            if (params == null) {
                //接口无参数
                method = serviceCls.getDeclaredMethod(methodName, completionCls)
                method.invoke(instance, kCompletion)
            }else {
                method = serviceCls.getDeclaredMethod(methodName, paramCls, completionCls)
                method.invoke(instance, params, kCompletion)
            }
        } catch (exception: Exception) {
            val failureMap = HashMap<String, Any?>()
            failureMap["code"]  = MSConst.kRouteInvokeErrorCode.toString()
            failureMap["msg"] = exception.localizedMessage
            result(failureMap, responseCls, completion)
        }
    }

    private fun <R>result(response: HashMap<String, Any?>, cls: Class<R>, completion: (R?) -> Unit){
        val jsonStr = JSONServiceImpl.gson.toJson(response)
        val r = JSONServiceImpl.gson.fromJson(jsonStr, cls)
        completion(r)
    }

}

