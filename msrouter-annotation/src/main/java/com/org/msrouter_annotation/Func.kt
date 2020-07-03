package com.org.msrouter_annotation

/**
 * @ClassName: Func
 * @Description: function annotation
 * @Author: ms
 * @Date: 2020/07/02 3:16 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Func(

    val methodName: String,

    val describe: String = ""
)