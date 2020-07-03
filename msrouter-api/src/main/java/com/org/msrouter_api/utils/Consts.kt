/**
 * FileName: MSConst
 * Author: ms
 * Date: 2020/7/2 4:56 PM
 * Description: 静态变量定义
 */
package com.org.msrouter_api.utils;

object MSConst {

    const val kServiceRegisterPackage = "com.org.msrouter"
    const val kProviderSuffixKey = "MSRouter_IRegister"

    const val kOptionKey = "option"

    /*************   common error code define   *************/
    /*
    * path为空*/
    const val kRouteEmptyErrorCode = 900001
    /*
    * path 解析失败*/
    const val kRouteUriParserErrorCode = 900002
    /*
    * 方法反射失败*/
    const val kRouteInvokeErrorCode = 900003
    /*
    * 方法找不到*/
    const val kRouteServiceNotFoundErrorCode = 900004

}


