package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.models.Param
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec

object QueryParamBuilder {
    fun build(queryParam: Param): ParameterSpec =
            ParameterSpec.builder(queryParam.name, queryParam.dataType.toKotlinPoetType().copy(nullable = !queryParam.isRequired))
                    .addAnnotation(ClassName("io.micronaut.http.annotation", "QueryValue"))
                    .build()
}