## MSRouter简介

MSRouter是为了解决 Anroid App 组件化拆分，模块组件路由跳转、服务调用的问题；

**路由跳转**

路由跳转**MSRouter**是直接采用**ARouter**实现，简单的对[**ARouter**](https://github.com/alibaba/ARouter)接口封装

```kotlin
/* 原有ARouter接口
  ARouter.getInstance().build("/main/subpage").withString("videoId","1001").navigation()
 */
 MTRouter.build("/main/subpage").withString("videoId","1001").navigation()
```

**服务接口调用**

**ARouter**模块组件服务调用是通过interface，获取到IProvider，进而直接调用对应接口

```kotlin
//模块A 的接口定义
interface IMsgService {
  fun unreadMsgCount(): Int
}

class MsgParamModel(val token: String) { }

//模块A
@Route(path = "/moduleA/service")
class ModuleAService: IProvider, IMsgService {
   override fun unreadMsgCount(paraM: MsgParamModel): Int {
     //如果token为空，则返回0
     if(paraM.token.isEmpty){ return 0; }
     return 10;
   }
}

//模块B
class ModuleBActivity {
  
  fun onClick() {
    IMsgService msgService = ARouter.getInstance().navigation(IMsgService::class.java)
    val paramModel = MsgParamModel(User.token)
    //调起模块A接口
    val unreadCount = msgService.unreadMsgCount(paramModel)
  }
}
```

针对上述示例，认为有几个问题待处理

* 模块接口定义存放位置

  一般情况下，公共服务接口位置是可以被所有业务模块都可以依赖的，因此将它放在Router或者CommonLib里；会出现很多业务模块同时维护一个组件库的情况，每个提供服务接口的模块都需要对Router/CommonLib进行维护，比较混乱

* 调用接口不统一

  如果模块B需要调用模块C，模块D接口，需要找到`ModuleCService`,`ModuleDService`, 然后在进行调用 `moduleCService.xxx()` ，`moduleDService.xxxxx()`，没有统一入口进行接口调用



因此针对上述问题MSRouter对模块接口进行改造

1. 服务接口映射到Uri，String类型进行管理
2. MSRouter整合App内各个模块的服务接口映射表，接口调用统一通过MSRouter处理



针对接口映射到Uri，增加`@Function`注解和`ARouter.@Route`一起对模块Service进行Uri拼接

`@Function`定义

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Function(
    val methodKey:String, //接口key
    val describe: String //接口描述
)
```



**Kotlin**示例

```kotlin
// Completion类型: 1个参数的闭包, Function1 
// typealias Completion = (response: HashMap<String, Any?>) -> Unit

@Route(path = "/shopcart/service")
class ShopCartService: IProvider {
    override fun init(context: Context?) { }
    
   //uri: "/shopcart/service?option=addshopcart"
    @Function("addshopcart", describe = "商品加入购物车接口")
    fun addShopCart(hashMap: HashMap<String, String>, complete: Completion) {
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            val map = HashMap<String, Any?>()
            map["count"] = "10000"
            map["code"] = "0"
            map["msg"] = "加入购物车成功"
            complete(map)
        }, 500)
    }
}
```

**Java**示例

```java
@Route(path = "/shopcart/service")
public final class ShopCartService implements IProvider {

    @Override
    public void init(Context context) { }

    //uri: "/shopcart/service?option=addshopcart"
    @Function(methodKey = "addshopcart", describe = "商品加入购物车接口")
    public void addShopCart(HashMap<String, Object> hashMap, Function1<HashMap<String, Object>, Void> function1) {
        (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> map = new HashMap<String,  Object>();
                map.put("count", "10000");
                map.put("code", "0");
                map.put("msg", "加入购物车成功");
                function1.invoke(map);
            }
        }, 500);
    }
}

```

router-compiler会解析Service注解内容，生成模块Service注册表

```kotlin
package xxx.routes.provider

import xxx.router_api.metamap.RouteProvider
import kotlin.String
import kotlin.collections.HashMap

/**
 *    ***************************************************
 *    * THIS CODE IS GENERATED BY MSRouter, DO NOT EDIT. *
 *    ***************************************************
 */
class ShopCart_MSRouter_Provider : RouteProvider {
  override fun loadInto(methodMap: HashMap<String, HashMap<String, String>>) {
    // add group shopcart
    val app_map = HashMap<String, String>()
    app_map["addshopcart"] = "addShopCart"
    methodMap["shopcart"] = app_map
  }
}
```

在App启动时，进行所有模块的映射表加载，加载方式和**ARouter**类似，

* 通过扫描Dex文件固定Package下的class，筛选符合条件的RouterProvider，进行映射表合并，缓存, 详见[ARouter.LogisticsCenter](https://github.com/alibaba/ARouter/blob/develop/arouter-api/src/main/java/com/alibaba/android/arouter/core/LogisticsCenter.java)
* plugin方式，字节码插桩，将模块间register方法插入到MSRouter中，详见[arouter-autoregister](https://github.com/alibaba/ARouter/tree/develop/arouter-gradle-plugin)



在模块调用服务接口

Kotlin

```kotlin
const val kAddShopCartPath = "/shopcart/service?option=addshopcart"

//定义接口模型
class AddCartParamModel(val token: String, val produceId: String, val count: String) { }
class RouteResponse {
  var code = "0"
  var msg = ""
  //总个数
  var count = ""
}

val para = AddCartParamModel(token, "1001", "2");
//调用购物车模块接口-商品加入购物车
MSRouter.request(kAddShopCartPath, para).response(RouteResponse::class.java) { 
  response ->                                                      
  Toast.makeText(this, response?.msg?:"", Toast.LENGTH_LONG).show();
  if (response?.code == "0") {
    Log.d("MainActivity", response.msg ?: "")
  }
}

//调用我的账户接口，查询个人信息
const val kQueryUserInfoURI = "/account/service?option=userinfo"
class QueryUserInfoParaModel(val token: String = "") { }
class QueryUserInfoResModel {
  var code = "0"
  var msg = ""
  var data: UserInfo? = null
}

val query_para = QueryUserInfoParaModel(token);
//调用 用户模块接口-查询用户信息
MSRouter.request(kQueryUserInfoURI, query_para).response(QueryUserInfoResModel::class.java) {
  response ->                                                                                         
  Toast.makeText(this, response?.msg?:"", Toast.LENGTH_LONG).show();
  if (response?.code == "0") {
    //查询用户信息成功
    Log.d("MainActivity", response.msg ?: "")
  }
}
```

Java

```java
//定义接口模型
class AddCartParamModel {
	String token;
  String produceId; 
  String count;
}

class RouteResponse {
  String code = "0";
  String msg = "";
  //总个数
  String count = "";
}

//加入购物车uri
static String kAddShopCartURIPath = "/shopcart/service?option=addshopcart";

AddCartParamModel para = new AddCartParamModel();
para.setToken(token);
para.setProductId("1001");
para.setCount("3");

//Kotlin闭包在Java类型是FunctionX<T,R>,一个参数对应Function1<T,R>，两个参数类型是Function2<T,R>，依次类推
//加入购物车
MSRouter.Companion.request(kAddShopCartURIPath, para).response(RouteResponse.class, new Function1<Response, Unit>() {
  @Override
  public Unit invoke(Response it) {
    String toast = response.msg + "  count:" + response.count;
    Toast.makeText(MainActivity.this, toast, Toast.LENGTH_LONG).show();
    return null;
  }
});

//查询用户信息

//查询个人信息URI
 static String kQueryUserInfoURIPath = "/account/service?option=userinfo";

//定义模型
class QueryUserInfoParaModel {
String token = "";
}
class QueryUserInfoResModel {
  String code = "0";
  String msg = "";
  UserInfo data;
}

//请求
QueryUserInfoParaModel query_para = new QueryUserInfoParaModel();
query_para.setToken(token);

//使用lambda
 MSRouter.Companion.request(kQueryUserInfoURIPath, query_para).response(QueryUserInfoResModel.class, response -> {
       Toast.makeText(this, response.getMsg(), Toast.LENGTH_LONG).show();
       return null;
  });

```

目前MSRouter接口是以上述方式进行模块间接口调用，代码量相对Interface多一些，类似于调用网络接口方式；基于Path调用接口，是以模块接口服务化方向去考虑的，模块接口调用都是以MSRouter为中心，由MSRouter进行解析，分发到对应模块功能接口; 业务模块开发接口，只需要注明对应接口就可以，不需要做额外配置
