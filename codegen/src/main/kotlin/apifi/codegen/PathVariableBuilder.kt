package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.models.Param
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec

object PathVariableBuilder {
  fun build(pathParam: Param): ParameterSpec =
      ParameterSpec.builder(pathParam.name, pathParam.dataType.toKotlinPoetType().copy(nullable = !pathParam.isRequired))
          .addAnnotation(ClassName("io.micronaut.http.annotation", "PathVariable"))
          .build()
}