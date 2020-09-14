package apifi.codegen

import apifi.helpers.toCamelCase
import apifi.helpers.toKotlinPoetType
import apifi.parser.models.Param
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec

object PathVariableBuilder {
    fun build(pathParam: Param, modelMapping: Map<String, String>): ParameterSpec =
        ParameterSpec.builder(pathParam.name.toCamelCase(), pathParam.dataType.toKotlinPoetType(modelMapping).copy(nullable = !pathParam.isRequired))
            .addAnnotation(AnnotationSpec.builder(ClassName(micronautHttpAnnotationPackage, "PathVariable"))
                .also { if (pathParam.name != pathParam.name.toCamelCase()) it.addMember("value = %S", pathParam.name) }
                .build())
            .build()
}