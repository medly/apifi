package apifi.codegen

import apifi.helpers.toCamelCase
import apifi.helpers.toKotlinPoetType
import apifi.helpers.toTitleCase
import apifi.parser.models.ParamType
import apifi.parser.models.Spec
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object ControllerBuilder {

    fun build(spec: Spec, securityDependencies: List<SecurityDependency>, basePackageName: String, modelMapping: List<Pair<String, String>>): FileSpec {
        val baseName = toTitleCase(spec.name)
        val controllerClassName = "${baseName}Controller"

        val serviceClass = ServiceBuilder.build(spec, baseName)

        val primaryConstructor = FunSpec.constructorBuilder()
                .addAnnotation(ClassName("javax.inject", "Inject"))
                .addParameter(ParameterSpec.builder("service", ClassName(basePackageName, serviceClass.name!!)).build())
                .addStatement("this.service = service")

        val serviceProperty = PropertySpec.builder("service", ClassName(basePackageName, serviceClass.name!!))
                .addModifiers(KModifier.PRIVATE).build()

        val classSpec = TypeSpec.classBuilder(ClassName(basePackageName, controllerClassName))
                .addAnnotation(AnnotationSpec.builder(ClassName("io.micronaut.http.annotation", "Controller"))
                        .addMember("value = %S", "/${spec.basePath}")
                        .build())
                .addProperty(serviceProperty)
                .addFunctions(generateOperationFunctions(spec, modelMapping, securityDependencies))

        securityDependencies.forEach { dependency ->
            primaryConstructor.addParameter(
                    ParameterSpec.builder(toCamelCase(dependency.name), ClassName(dependency.packageName, dependency.name)).build()
            ).addStatement("this.${toCamelCase(dependency.name)} = ${toCamelCase(dependency.name)}")

            classSpec.addProperty(PropertySpec.builder(toCamelCase(dependency.name), ClassName(dependency.packageName, dependency.name))
                    .addModifiers(KModifier.PRIVATE).build())
        }

        classSpec.primaryConstructor(primaryConstructor.build())

        return FileSpec.builder(basePackageName, "$controllerClassName.kt").addType(classSpec.build()).addType(serviceClass).build()
    }

    private fun generateOperationFunctions(spec: Spec, modelMapping: List<Pair<String, String>>, securityDependencies: List<SecurityDependency>): List<FunSpec> {
        return spec.paths.flatMap { path ->
            path.operations?.map { operation ->
                val queryParams = operation.params?.filter { it.type == ParamType.Query }?.map(QueryParamBuilder::build)
                        ?: emptyList()
                val pathParams = operation.params?.filter { it.type == ParamType.Path }?.map(PathVariableBuilder::build)
                        ?: emptyList()
                val headerParams = operation.params?.filter { it.type == ParamType.Header }?.map(HeaderBuilder::build)
                        ?: emptyList()
                val requestBodyParams = operation.requestBodyType?.let { listOf(RequestBodyBuilder.build(it, modelMapping)) }
                        ?: emptyList()
                val serviceCallParams = queryParams + pathParams + requestBodyParams

                val serviceCallStatement = "HttpResponse.ok(service.${operation.name}(${serviceCallParams.joinToString { it.name }}))"

                val httpRequestParam = ParameterSpec.builder("httpRequest",
                        ClassName("io.micronaut.http", "HttpRequest").parameterizedBy(Any::class.asClassName()))
                        .build()
                val responseType = operation.response?.firstOrNull()?.let { ClassName("io.micronaut.http", "HttpResponse").parameterizedBy(it.toKotlinPoetType(modelMapping)) }
                FunSpec.builder(operation.name)
                        .addAnnotation(
                                AnnotationSpec.builder(ClassName("io.micronaut.http.annotation", toTitleCase(operation.type.toString())))
                                        .addMember("value = %S", path.url)
                                        .build()
                        )
                        .addParameters(queryParams + pathParams + headerParams + requestBodyParams)
                        .addParameter(httpRequestParam)
                        .also { responseType?.let { res -> it.returns(res) } }
                        .addStatement(if (securityDependencies.isNotEmpty()) "return basicauthorizer.authorize(httpRequest.headers.authorization){$serviceCallStatement}" else "return $serviceCallStatement")
                        .build()
            } ?: emptyList()
        }
    }

}
