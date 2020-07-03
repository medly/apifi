package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.parser.models.Operation
import apifi.parser.models.ParamType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

fun Operation.pathParams() = params?.filter { it.type == ParamType.Path }?.map(PathVariableBuilder::build)
        ?: emptyList()

fun Operation.queryParams() = params?.filter { it.type == ParamType.Query }?.map(QueryParamBuilder::build)
        ?: emptyList()

fun Operation.headerParams() = params?.filter { it.type == ParamType.Header }?.map(HeaderBuilder::build)
        ?: emptyList()

fun Operation.requestParams(modelMapping: Map<String, String>) = request?.let {
    listOf(ParameterSpec.builder("body", it.type.toKotlinPoetType(modelMapping))
            .addAnnotation(ClassName("io.micronaut.http.annotation", "Body"))
            .build())
} ?: emptyList()

fun Operation.returnType(modelMapping: Map<String, String>) = response
		?.firstOrNull { it.defaultOrStatus == "200" || it.defaultOrStatus == "201" }
        ?.let { ClassName("io.micronaut.http", "HttpResponse").parameterizedBy(it.type.toKotlinPoetType(modelMapping)) }
