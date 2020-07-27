package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.parser.models.Operation
import apifi.parser.models.Param
import apifi.parser.models.ParamType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy


fun Operation.queryParamSpecs() = params?.filter { it.type == ParamType.Query }?.map(QueryParamBuilder::build)
        ?: emptyList()

fun Operation.queryParamSpecNames() = params?.filter { it.type == ParamType.Query }?.map(QueryParamBuilder::build)?.map { it.name }
        ?: emptyList()

fun Operation.queryParams(): List<Param> = params?.filter { it.type == ParamType.Query } ?: emptyList()

fun Operation.pathParamSpecs() = params?.filter { it.type == ParamType.Path }?.map(PathVariableBuilder::build)
        ?: emptyList()

fun Operation.pathParamSpecNames() = params?.filter { it.type == ParamType.Path }?.map(PathVariableBuilder::build)?.map { it.name }
        ?: emptyList()

fun Operation.pathParams(): List<Param> = params?.filter { it.type == ParamType.Path } ?: emptyList()

fun Operation.headerParamSpecs() = params?.filter { it.type == ParamType.Header }?.map(HeaderBuilder::build)
        ?: emptyList()

fun Operation.requestParams(modelMapping: Map<String, String>) = request?.let {
    listOf(ParameterSpec.builder("body", it.type.toKotlinPoetType(modelMapping))
            .addAnnotation(ClassName(micronautHttpAnnotationPackage, "Body"))
            .build())
} ?: emptyList()

fun Operation.requestParamNames(modelMapping: Map<String, String>) = request?.let {
    listOf(ParameterSpec.builder("body", it.type.toKotlinPoetType(modelMapping))
            .addAnnotation(ClassName(micronautHttpAnnotationPackage, "Body"))
            .build())
}?.map { it.name } ?: emptyList()

fun Operation.returnType(modelMapping: Map<String, String>) = responses
        ?.firstOrNull { it.defaultOrStatus == "200" || it.defaultOrStatus == "201" }
        ?.let { ClassName(micronautHttpPackage, "HttpResponse").parameterizedBy(it.type.toKotlinPoetType(modelMapping)) }