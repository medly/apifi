package apifi.codegen

import apifi.helpers.toCamelCase
import apifi.helpers.toKotlinPoetType
import apifi.parser.models.ParamType
import apifi.parser.models.Path
import com.squareup.kotlinpoet.*

object ServiceBuilder {
    fun build(path: Path, baseName: String): TypeSpec {
        val serviceMethods = path.operations?.map { operation ->
            val queryParams = operation.params?.filter { it.type == ParamType.Query } ?: emptyList()
            val pathParams = operation.params?.filter { it.type == ParamType.Path } ?: emptyList()
            val params = (queryParams + pathParams).map {
                ParameterSpec.builder(it.name, it.dataType.toKotlinPoetType().copy(nullable = !it.isRequired)).build()
            }

            val requestBodyParam = operation.requestBodyType?.let {
                ParameterSpec.builder("body", it.toKotlinPoetType()).build()
            }

            FunSpec.builder(operation.name)
                    .addModifiers(KModifier.ABSTRACT)
                    .addParameters(params)
                    .also { requestBodyParam?.let { req -> it.addParameter(req) } }
                    .also { (operation.response?.firstOrNull()?.let { res -> it.returns(res.toKotlinPoetType()) }) }
                    .build()
        } ?: emptyList()

        return TypeSpec.interfaceBuilder("${baseName}Service")
                .addFunctions(serviceMethods).build()
    }
}