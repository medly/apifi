package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.parser.models.ParamType
import apifi.parser.models.Path
import com.squareup.kotlinpoet.*

object ControllerInterfaceBuilder {
    fun build(paths: List<Path>, baseName: String): TypeSpec {
        val controllerMethods = paths.flatMap { path ->
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
                        .also { (operation.responses?.firstOrNull()?.let { res -> it.returns(res.type.toKotlinPoetType()) }) }
                        .build()
            } ?: emptyList()
        }

        return TypeSpec.interfaceBuilder("${baseName}Controller")
                .addFunctions(controllerMethods).build()
    }
}