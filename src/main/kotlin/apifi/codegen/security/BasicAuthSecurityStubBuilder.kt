package apifi.codegen.security

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object BasicAuthSecurityStubBuilder {
    fun build(basePackageName: String): FileSpec {
        val packageName = "$basePackageName.security"
        val httpResponse = ClassName("io.micronaut.http", "HttpResponse")
        val optionalString = ClassName("java.util", "Optional")
        val funSpec = FunSpec.builder("authorize")
                .addTypeVariable(TypeVariableName("T"))
                .addModifiers(KModifier.ABSTRACT)
                .addParameter("authHeader", optionalString.parameterizedBy(String::class.asClassName()))
                .addParameter("f", LambdaTypeName.get(returnType = httpResponse.parameterizedBy(TypeVariableName("T"))))
                .returns(httpResponse.parameterizedBy(TypeVariableName("T"))).build()

        val basicAuthorizerInterface = TypeSpec.interfaceBuilder(ClassName(packageName, "BasicAuthorizer"))
                .addFunction(funSpec)
                .build()

        return FileSpec.builder(packageName, "BasicAuthorizer.kt")
                .addType(basicAuthorizerInterface)
                .build()
    }
}
