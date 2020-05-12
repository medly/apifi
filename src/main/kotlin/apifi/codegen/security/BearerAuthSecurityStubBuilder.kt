package apifi.codegen.security

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object BearerAuthSecurityStubBuilder {
    fun build(basePackageName: String): FileSpec {
        val packageName = "$basePackageName.security"
        val httpResponse = ClassName("io.micronaut.http", "HttpResponse")
        val funSpec = FunSpec.builder("ifAuthorizedForClaim")
                .addTypeVariable(TypeVariableName("T"))
                .addModifiers(KModifier.ABSTRACT)
                .addParameter("claim", String::class.asClassName())
                .addParameter("authentication", ClassName("io.micronaut.security.authentication.Authentication", "Authentication"))
                .addParameter("f", LambdaTypeName.get(returnType = httpResponse.parameterizedBy(TypeVariableName("T"))))
                .returns(httpResponse.parameterizedBy(TypeVariableName("T"))).build()

        val basicAuthorizerInterface = TypeSpec.interfaceBuilder(ClassName(packageName, "BearerAuthorizer"))
                .addFunction(funSpec)
                .build()

        return FileSpec.builder(packageName, "BearerAuthorizer.kt")
                .addType(basicAuthorizerInterface)
                .build()
    }
}

/*
interface BearerAuthorizer {
  fun <T> ifAuthorizedForClaim(claim: String, authentication: Authentication, f: () -> HttpResponse<T>): HttpResponse<T>
}
 */