/**
 * FileName: RouteMeta
 * Author: ms
 * Date: 2020-07-02 19:00
 * Description: route meta
 */
package com.org.msrouter_api.metamap;


interface IRouteRegister {

    fun loadInto(methodMap: HashMap<String, HashMap<String, String>>)
}

typealias kCompletion = (response: HashMap<String, Any?>) -> Unit
