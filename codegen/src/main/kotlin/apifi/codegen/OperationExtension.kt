package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.parser.models.Operation
import apifi.parser.models.Param
import apifi.parser.models.ParamType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy


fun Operation.queryParamSpecs(modelMapping: Map<String, String>) = queryParams().map { QueryParamBuilder.build(it, modelMapping) }

fun Operation.queryParamSpecNames(modelMapping: Map<String, String>) = queryParamSpecs(modelMapping).map { it.name }

fun Operation.queryParams(): List<Param> = params.filter { it.type == ParamType.Query }

fun Operation.pathParamSpecs(modelMapping: Map<String, String>) = pathParams().map { PathVariableBuilder.build(it, modelMapping) }

fun Operation.pathParamSpecNames(modelMapping: Map<String, String>) = pathParamSpecs(modelMapping).map { it.name }

fun Operation.pathParams(): List<Param> = params.filter { it.type == ParamType.Path }

fun Operation.headerParamSpecs() = params.filter { it.type == ParamType.Header }.map(HeaderBuilder::build)

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

fun Operation.returnType(modelMapping: Map<String, String>): ParameterizedTypeName? =
    responses.let { res ->
        when {
            hasOnlyDefaultResponse() -> ClassName(micronautHttpPackage, "HttpResponse").parameterizedBy(res[0].type.toKotlinPoetType(modelMapping))
            hasMoreThanOne2xxResponse() -> error("Invalid responses defined for operation with identifier: ${this.name}. Has more than one 2xx responses defined")
            else -> first2xxResponse()?.let { ClassName(micronautHttpPackage, "HttpResponse").parameterizedBy(it.type.toKotlinPoetType(modelMapping)) }
        }
    }
//TODO: we could give functionality of marking an operation as preferred one, in case we want to support multiple 2xx for some reason