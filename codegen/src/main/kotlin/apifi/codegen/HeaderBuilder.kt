package apifi.codegen

import apifi.helpers.toCamelCase
import apifi.helpers.toKotlinPoetType
import apifi.parser.models.Param
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec

object HeaderBuilder {
    fun build(pathParam: Param): ParameterSpec =
        ParameterSpec.builder(pathParam.name.toCamelCase(), pathParam.dataType.toKotlinPoetType().copy(nullable = !pathParam.isRequired))
            .addAnnotation(
                AnnotationSpec.builder(ClassName(micronautHttpAnnotationPackage, "Header"))
                    .addMember("value = %S", pathParam.name)
                    .build())
            .build()
}