package com.org.testaccountmodule

import android.content.Context
import android.os.Looper
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.template.IProvider
import com.org.msrouter_annotation.Func
import com.org.msrouter_api.metamap.kCompletion
import kotlin.collections.set

/**
 * @ClassName: AccountService
 * @Description: account service
 * @Author: ms
 * @Date: 2020/07/02 3:38 PM
 */

@Route(path = "/account/service")
class AccountService:IProvider {

    override fun init(context: Context?) {

    }

    @Func("query_userinfo",describe = "获取用户信息接口")
    fun queryUserInfo(hashMap: HashMap<String, String>,complete: kCompletion) {

        android.os.Handler(Looper.getMainLooper()).postDelayed({

            val map = HashMap<String, Any?>()
            map["name"] = "光头强-"
            map["address"] = "平江路12220号"
            map["code"] = "0"
            map["msg"] = "用户信息请求成功"
            complete(map)
        }, 500)
    }

    private fun verifyToken(): Boolean {

        return  true;
    }

}

