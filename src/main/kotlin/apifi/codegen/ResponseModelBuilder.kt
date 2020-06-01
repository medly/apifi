package apifi.codegen

import apifi.helpers.toKotlinPoetType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object ResponseModelBuilder {
    fun build(basePackageName: String): FileSpec {
        val packageName = "$basePackageName.models"
        val builder = FileSpec.builder(packageName, "Response.kt")
        val className = ClassName(packageName, "Response")
        className.parameterizedBy(TypeVariableName("T"))

        builder.addType(
                TypeSpec.enumBuilder(ClassName(packageName, "ResponseType"))
                        .addEnumConstant("SUCCESS")
                        .addEnumConstant("BAD_REQUEST")
                        .addEnumConstant("FORBIDDEN")
                        .addEnumConstant("NOT_FOUND")
                        .build())
                .addType(
                TypeSpec.classBuilder(className)
                        .addTypeVariable(TypeVariableName.Companion.invoke("T"))
                        .addModifiers(KModifier.DATA)
                        .primaryConstructor(
                                FunSpec.constructorBuilder()
                                        .addParameter(ParameterSpec.builder("status", ClassName(packageName, "ResponseType")).build())
                                        .addParameter(ParameterSpec.builder("body", TypeVariableName.Companion.invoke("T")).build())
                                        .addParameter(ParameterSpec.builder("headers", Map::class.asClassName().parameterizedBy(CharSequence::class.asTypeName(), CharSequence::class.asTypeName())).build())
                                        .build()
                        )
                        .build()
        )

        return builder.build()
    }
}