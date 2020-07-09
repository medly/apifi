package apifi.codegen

import apifi.helpers.toKotlinPoetType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec

object RequestBodyBuilder {
  fun build(bodyType: String, modelMapping: List<Pair<String, String>>): ParameterSpec =
      ParameterSpec.builder("body", bodyType.toKotlinPoetType(modelMapping))
          .addAnnotation(ClassName("io.micronaut.http.annotation", "Body"))
          .build()
}
