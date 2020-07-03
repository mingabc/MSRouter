package com.org.testshopcartmodule;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.org.msrouter_annotation.Func;

import java.util.HashMap;

import kotlin.jvm.functions.Function1;

@Route(path = "/shopcart/service")
public final class ShopCartService implements IProvider {

    @Override
    public void init(Context context) {

    }

    @Func(methodName = "addshopcart", describe = "加入购物车接口")
    public void addShopCart(HashMap<String, Object> hashMap, final Function1<HashMap<String, Object>, Void> function1) {

        (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
            @Override
            public void run() {


                HashMap<String, Object> map = new HashMap<String,  Object>();
                map.put("count", "10000");
                map.put("code", "0");
                map.put("msg", "加入购物车成功");
                function1.invoke(map);
            }
        }, 300);

    }


    private boolean verifyData(HashMap<String, Object> map ) {
        String token = (String) map.get("token");
        String productId = (String) map.get("productId");
        String count = (String) map.get("count");

        if (token.isEmpty() || productId.isEmpty() || count.isEmpty()) {
            return false;
        }
        return true;
    }






}
