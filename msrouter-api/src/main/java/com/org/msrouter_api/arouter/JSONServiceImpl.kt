package com.org.msrouter_api.arouter;

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.service.SerializationService
import com.google.gson.Gson
import java.lang.reflect.Type

/**
 * @ClassName: JsonServiceImpl
 * @Description: arouter json service
 * @Author: ms
 * @Date: 2020-07-02 11:06
 */

@Route(path = "/service/json")
public class JSONServiceImpl: SerializationService {

    companion object {
        val gson by lazy {
          return@lazy Gson()
        }
    }
    /**
     * Parse json to object
     * USE @parseObject PLEASE
     */
    override fun <T : Any?> json2Object(input: String?, clazz: Class<T>?): T {
        return gson.fromJson(input, clazz)
    }

    override fun init(context: Context?) {
    }

    override fun object2Json(instance: Any?): String {
        return gson.toJson(instance)
    }

    override fun <T : Any?> parseObject(input: String?, clazz: Type?): T {
        return gson.fromJson(input, clazz)
    }
}

