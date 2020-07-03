/**
 * FileName: FunctionProcessor
 * Author: ms
 * Date: 2020/07/02 5:31 PM
 */
package com.org.msrouter_compiler

import com.alibaba.android.arouter.facade.annotation.Route
import com.google.auto.service.AutoService
import com.google.gson.Gson
import com.org.msrouter_annotation.Func
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.Writer
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.StandardLocation
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(
    Const.kRT_ANNOTATION_TYPE_FUNC
)
class FuncProcessor: AbstractProcessor() {

    lateinit var elementsUtils: Elements
    lateinit var typeUtils: Types
    var messager: Messager? = null
    var mFiler: Filer? = null

    var moduleName = "default"
    var iProvider: TypeMirror? = null

    var generateDoc = false

    // Writer used for write doc
    private var docWriter : Writer? = null

    override fun init(p0: ProcessingEnvironment) {
        super.init(p0)
        messager = processingEnv.messager
        elementsUtils = p0.elementUtils
        mFiler = p0.filer
        typeUtils = p0.typeUtils
        val options = p0.options
        iProvider = elementsUtils.getTypeElement(Const.kAROUTER_IPROVIDER).asType()
        // Attempt to get user configuration [moduleName]
        if (!options.isNullOrEmpty()) {
            moduleName = options[Const.kAROUTER_KEY_MODULE_NAME]!!
            generateDoc = Const.kAROUTER_VALUE_ENABLE == options[Const.kAROUTER_KEY_GENERATE_DOC_NAME]
        }


    if (moduleName.isNotEmpty()) {
        moduleName = moduleName.replace("[^0-9a-zA-Z_]+".toRegex(), "")
    }


        docWriter = mFiler!!.createResource(
            StandardLocation.SOURCE_OUTPUT,
            "com.org.msrouter.routes.docs", "$moduleName-map-api.json"
        ).openWriter()
    }


    override fun process(p0: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (!p0.isNullOrEmpty() && roundEnv != null) {
            try {
                this.parseRoutes(roundEnv)
            } catch (e: Exception) { }
            return true
        }
        return false
    }


    private  fun parseRoutes(roundEnv: RoundEnvironment) {

        val methodMap = ClassName("kotlin.collections", "HashMap")
        val contentMap = ClassName("kotlin.collections", "HashMap")
        val contentMapType =
            contentMap.parameterizedBy(String::class.asClassName(), String::class.asClassName())
        val methodMapTypeOfRouteLoader =
            methodMap.parameterizedBy(String::class.asClassName(), contentMapType)

        val routeLoaderFunSpecBuild = FunSpec.builder(Const.METHOD_LOAD) //方法声明
            .addParameter("methodMap", methodMapTypeOfRouteLoader)  //methodMap参数
            .addModifiers(KModifier.OVERRIDE)

        val groupMap: HashMap<String, HashMap<String, Any>> = HashMap()

        //获取ARouter的Route注解
        val route_elements = roundEnv.getElementsAnnotatedWith(Route::class.java)

        val type_Activity: TypeMirror = elementsUtils.getTypeElement(Const.ACTIVITY).asType()
        val type_Service: TypeMirror = elementsUtils.getTypeElement(Const.SERVICE).asType()
        val fragmentTm: TypeMirror = elementsUtils.getTypeElement(Const.FRAGMENT).asType()
        val fragmentTmV4: TypeMirror = elementsUtils.getTypeElement(Const.FRAGMENT_V4).asType()

         
        val constPropertySpecs = ArrayList<PropertySpec>()

        route_elements.forEach {
            val typeElement = it as TypeElement
            val enclosedEles = it.enclosedElements
            val type = it.asType()
            val routeAnnotation = typeElement.getAnnotation(Route::class.java)
            //路由静态变量类型后缀
            var suffixPathKind = ""
            if (typeUtils.isSubtype(type, type_Activity)) {
                //is activity
                suffixPathKind = "Activity"
            }else if (typeUtils.isSubtype(type, fragmentTm) || typeUtils.isSubtype(type, fragmentTmV4)) {
                // is fragment
                suffixPathKind = "Fragment"
            }else if (typeUtils.isSubtype(type, type_Service)) {
                //is system service
                suffixPathKind = "Service"
            }else if(typeUtils.isSubtype(type, iProvider)) {
                suffixPathKind = "IProvider"
            }
            val serviceMap = HashMap<String, Any>()
            val path = routeAnnotation.path
            val defaultGroup: String = path.substring(1, path.indexOf("/", 1))
            val groupName = if(routeAnnotation.group.isEmpty()){ defaultGroup } else { routeAnnotation.group }

            serviceMap["path"] = routeAnnotation.path
            serviceMap["type"] = suffixPathKind
            serviceMap["group"] = groupName

           val propertySpec = PropertySpec.builder("k${path.firstCharUpperCase()}${suffixPathKind}Key", String::class, KModifier.PUBLIC, KModifier.CONST).initializer("\"$path\"").addKdoc("******************  $groupName ******************").build()
            constPropertySpecs.add(propertySpec)
            val list = ArrayList<HashMap<String,String>>()
            
            enclosedEles.forEach { enclosedElement ->
                val executableElement = enclosedElement as? ExecutableElement
                val funcAnnotation = executableElement?.getAnnotation(Func::class.java)
                if (executableElement !== null && funcAnnotation != null) {
                    //RTFunc.element
                    val map = HashMap<String, String>()
                    map["key"] = funcAnnotation.methodName
                    map["method"] = executableElement.simpleName.toString()
                    val uri = "${routeAnnotation.path}?${Const.kOPTION_KEY}=${funcAnnotation.methodName}"
                    map["uri"] = uri
                    map["desc"] = funcAnnotation.describe
                    list.add(map)

                    val subPropertySpec = PropertySpec.builder("k${funcAnnotation.methodName.firstCharUpperCase()}URIPath", String::class, KModifier.PUBLIC, KModifier.CONST).initializer("\"$uri\"").addKdoc(funcAnnotation.describe).build()
                    constPropertySpecs.add(subPropertySpec)
                }

            }
            
            if (list.isNotEmpty()){
                routeLoaderFunSpecBuild.addComment("add group $groupName")
                routeLoaderFunSpecBuild.addStatement("val ${groupName}_map = HashMap<String, String>()")
                //遍历groupName下的RTFunc注解接口
                list.forEach { map ->
                    routeLoaderFunSpecBuild.addStatement( "${groupName}_map[%S] = %S", map["key"]!!, map["method"]!!)
                }
                routeLoaderFunSpecBuild.addStatement("methodMap[%S] = ${groupName}_map", groupName)
                serviceMap["method"] = list
            }
            groupMap[groupName] = serviceMap
        }

        val typeIRouteLoader =
            TypeSpec.classBuilder("${moduleName.capitalize()}${Const.ROUTE_PROVIDER_NAME}")
                .addSuperinterface(ClassName.bestGuess(Const.ROUTE_PROVIDER))
                .addKdoc(Const.APT_PROVIDER_TINT_MSG)
                .addFunction(routeLoaderFunSpecBuild.build())
                .build()


        FileSpec.builder("${Const.PACKAGE}.routes", "${moduleName.capitalize()}${Const.ROUTE_PROVIDER_NAME}").addType(typeIRouteLoader).build().writeTo(mFiler!!)

       val constTypeSpec = TypeSpec.objectBuilder("${moduleName.firstCharUpperCase()}Const").addModifiers(KModifier.PUBLIC).addProperties(constPropertySpecs).build()

        FileSpec.builder("${Const.PACKAGE}.routes","${moduleName.firstCharUpperCase()}Const").addType(constTypeSpec).build().writeTo(mFiler!!)

        if (generateDoc) {
            writeDoc(groupMap) //生成json文件
        }

    }

    private fun writeDoc(map: HashMap<String, HashMap<String, Any>>){
        val formatContent = Gson().toJson(Gson().toJsonTree(map))
        docWriter?.append(formatContent)
        docWriter?.flush()
        docWriter?.close()
    }

}


//首字母大写
private fun String.firstCharUpperCase(): String {
    return split("/").map {
        if (it.isNotEmpty()){ it.substring(0,1).toUpperCase(Locale.getDefault()) + it.substring(1) }else {""}
    }.joinToString("")
}