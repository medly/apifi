package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.parser.models.ParamType
import apifi.parser.models.Path
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec

object ServiceBuilder {
    fun build(paths: List<Path>, baseName: String, modelMapping: Map<String, String>): TypeSpec {
        val serviceMethods = paths.flatMap { path ->
            path.operations?.map { operation ->
                val queryParams = operation.params?.filter { it.type == ParamType.Query } ?: emptyList()
                val pathParams = operation.params?.filter { it.type == ParamType.Path } ?: emptyList()
                val params = (queryParams + pathParams).map {
                    ParameterSpec.builder(it.name, it.dataType.toKotlinPoetType().copy(nullable = !it.isRequired)).build()
                }

                val requestBodyParam = operation.request?.let {
                    ParameterSpec.builder("body",
                            (if(it.type == "io.micronaut.http.multipart.CompleteFileUpload") "java.io.File" else it.type).toKotlinPoetType())
                            .build()
                }

                FunSpec.builder(operation.name)
                        .addModifiers(KModifier.ABSTRACT)
                        .addParameters(params)
                        .also { requestBodyParam?.let { req -> it.addParameter(req) } }
                        .also { operation.responseType(modelMapping)?.let { res -> it.returns(res) } }
                        .build()
            } ?: emptyList()
        }

        return TypeSpec.interfaceBuilder("${baseName}Service")
                .addFunctions(serviceMethods).build()
    }
}