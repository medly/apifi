package apifi.codegen.exceptions

import apifi.codegen.exceptions.ExceptionDetailsHolder
import apifi.helpers.toKotlinPoetType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object ExceptionFileBuilder {

    fun build(exception: ExceptionDetailsHolder, basePackageName: String): FileSpec {
        val packageName = "$basePackageName.exceptions"
        val exceptionClassName = exception.exceptionClassName

        val builder = FileSpec.builder(packageName, "$exceptionClassName.kt")
        builder.addType(
                TypeSpec.classBuilder(ClassName(packageName, exceptionClassName))
                        .superclass(Exception::class)
                        .addSuperclassConstructorParameter("%L", "message")
                        .primaryConstructor(
                                FunSpec.constructorBuilder()
                                        .addParameter(ParameterSpec.builder("message", String::class).build()).build()
                        ).build())

        builder.addType(
                TypeSpec.classBuilder(ClassName(packageName, "Global${exceptionClassName}Handler"))
                        .addAnnotation(ClassName("javax.inject", "Singleton"))
                        .addAnnotation(ClassName("io.micronaut.http.annotation", "Produces"))
                        .addAnnotation(
                                AnnotationSpec.builder(ClassName("io.micronaut.context.annotation", "Requires"))
                                .addMember("classes = [%T::class, %T::class]", ClassName(packageName, exceptionClassName), ClassName("io.micronaut.http.server.exceptions", "ExceptionHandler"))
                                .build())
                        .addSuperinterface(
                                ClassName("io.micronaut.http.server.exceptions", "ExceptionHandler")
                                        .parameterizedBy(ClassName(packageName, exceptionClassName),
                                                         ClassName("io.micronaut.http", "HttpResponse").parameterizedBy("String".toKotlinPoetType()))
                        )
                        .addFunction(FunSpec.builder("handle")
                                .addModifiers(KModifier.OVERRIDE)
                                .addParameter("request", ClassName("io.micronaut.http", "HttpRequest").parameterizedBy(ClassName("kotlin", "Any")).copy(nullable = true))
                                .addParameter("exception", ClassName(packageName, exceptionClassName).copy(nullable = true))
                                .returns(ClassName("io.micronaut.http", "HttpResponse").parameterizedBy("String".toKotlinPoetType()))
                                .addStatement("val msg = exception?.cause?.localizedMessage ?: \"${exception.defaultExceptionMessage}\"")
                                .addStatement("return HttpResponse.status<String>(io.micronaut.http.HttpStatus.valueOf(${exception.status}), msg)")
                                .build()
                        )
                        .build()
        )
        return builder.build()
    }

}