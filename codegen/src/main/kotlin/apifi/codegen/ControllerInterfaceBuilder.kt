package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.parser.models.Path
import com.squareup.kotlinpoet.*

object ControllerInterfaceBuilder {
    fun build(paths: List<Path>, baseName: String): TypeSpec {
        val controllerMethods = paths.flatMap { path ->
            path.operations?.map { operation ->
                val queryParams = operation.queryParams()
                val pathParams = operation.pathParams()
                val params = (queryParams + pathParams).map {
                    ParameterSpec.builder(it.name, it.dataType.toKotlinPoetType().copy(nullable = !it.isRequired)).build()
                }

                val requestBodyParam = operation.request?.let {
                    ParameterSpec.builder("body",
                            (if(it.type == micronautMultipartFileUploadPackage) "java.io.File" else it.type).toKotlinPoetType())
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