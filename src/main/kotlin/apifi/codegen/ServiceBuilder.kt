package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.parser.models.ParamType
import apifi.parser.models.Spec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec

object ServiceBuilder {
    fun build(spec: Spec, name: String): TypeSpec {
        val serviceMethods = spec.paths.flatMap { path ->
            path.operations?.map { operation ->
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
        }

        return TypeSpec.interfaceBuilder("${name}Service")
                .addFunctions(serviceMethods).build()
    }
}