package apifi.codegen

import apifi.helpers.toCamelCase
import apifi.helpers.toKotlinPoetType
import apifi.parser.models.Path
import com.squareup.kotlinpoet.*

class ControllerInterfaceBuilder(private val securityProvider: SecurityProvider) {
    fun build(paths: List<Path>, baseName: String): TypeSpec {
        val controllerMethods = paths.flatMap { path ->
            path.operations?.map { operation ->
                val queryParams = operation.queryParams()
                val pathParams = operation.pathParams()
                val params = (queryParams + pathParams).map {
                    ParameterSpec.builder(it.name.toCamelCase(), it.dataType.toKotlinPoetType().copy(nullable = !it.isRequired)).build()
                }

                val requestBodyParam = operation.request?.let {
                    ParameterSpec.builder("body",
                            (if(it.type == micronautMultipartFileUploadPackage) "java.io.File" else it.type).toKotlinPoetType())
                            .build()
                }
                val securityParameters =
                    when {
                        securityProvider.shouldAuthenticate(operation) ->
                            listOf(ParameterSpec.builder("authentication", ClassName(micronautSecurityAuthenticationPackage, "Authentication").copy(nullable = true)).build())
                        else -> emptyList()
                    }

                FunSpec.builder(operation.name)
                        .addModifiers(KModifier.ABSTRACT)
                        .addParameters(params)
                        .also { requestBodyParam?.let { req -> it.addParameter(req) } }
                        .also { it.addParameters(securityParameters) }
                        .also { (operation.responses.firstOrNull()?.let { res -> it.returns(res.type.toKotlinPoetType()) }) }
                        .build()
            } ?: emptyList()
        }

        return TypeSpec.interfaceBuilder("${baseName}Controller")
            .addFunctions(controllerMethods).build()
    }
}