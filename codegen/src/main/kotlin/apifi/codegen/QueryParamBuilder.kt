package apifi.codegen

import apifi.helpers.toCamelCase
import apifi.helpers.toKotlinPoetType
import apifi.parser.models.Param
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec

object QueryParamBuilder {
    fun build(queryParam: Param, modelMapping: Map<String, String>): ParameterSpec =
        ParameterSpec.builder(queryParam.name.toCamelCase(), queryParam.dataType.toKotlinPoetType(modelMapping).copy(nullable = !queryParam.isRequired))
            .addAnnotation(AnnotationSpec.builder(ClassName(micronautHttpAnnotationPackage, "QueryValue"))
                .also { if (queryParam.name != queryParam.name.toCamelCase()) it.addMember("value = %S", queryParam.name) }
                .build())
            .build()
}