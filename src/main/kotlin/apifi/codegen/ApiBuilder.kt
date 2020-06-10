package apifi.codegen

import apifi.helpers.toCamelCase
import apifi.helpers.toKotlinPoetType
import apifi.helpers.toTitleCase
import apifi.parser.models.Operation
import apifi.parser.models.ParamType
import apifi.parser.models.Path
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object ApiBuilder {

    fun build(name: String, paths: List<Path>, securityDependencies: List<SecurityDependency>, basePackageName: String, modelMapping: List<Pair<String, String>>): FileSpec {
        val baseName = toTitleCase(name)
        val controllerClassName = "${baseName}Api"

        val controllerInterfaceClass = ControllerInterfaceBuilder.build(paths, baseName)

        val primaryConstructor = FunSpec.constructorBuilder()
                .addAnnotation(ClassName("javax.inject", "Inject"))
                .addParameter(ParameterSpec.builder("controller", ClassName(basePackageName, controllerInterfaceClass.name!!)).build())

        val controllerProperty = PropertySpec.builder("controller", ClassName(basePackageName, controllerInterfaceClass.name!!))
                .addModifiers(KModifier.PRIVATE).initializer("controller").build()

        val classSpec = TypeSpec.classBuilder(ClassName(basePackageName, controllerClassName))
                .addAnnotation(AnnotationSpec.builder(ClassName("io.micronaut.http.annotation", "Controller"))
                        .build())
                .addProperty(controllerProperty)
                .addFunctions(generateOperationFunctions(paths, modelMapping, securityDependencies))

        securityDependencies.forEach { dependency ->
            primaryConstructor.addParameter(
                    ParameterSpec.builder(toCamelCase(dependency.name), ClassName(dependency.packageName, dependency.name)).build()
            )

            classSpec.addProperty(PropertySpec.builder(toCamelCase(dependency.name), ClassName(dependency.packageName, dependency.name))
                    .addModifiers(KModifier.PRIVATE).initializer(toCamelCase(dependency.name)).build())
        }

        classSpec.primaryConstructor(primaryConstructor.build())

        return FileSpec.builder(basePackageName, "$controllerClassName.kt").addType(classSpec.build()).addType(controllerInterfaceClass).build()
    }

    private fun generateOperationFunctions(paths: List<Path>, modelMapping: List<Pair<String, String>>, securityDependencies: List<SecurityDependency>): List<FunSpec> {
        return paths.flatMap { path ->
            path.operations?.map { operation ->
                val queryParams = operation.params?.filter { it.type == ParamType.Query }?.map(QueryParamBuilder::build)
                        ?: emptyList()
                val pathParams = operation.params?.filter { it.type == ParamType.Path }?.map(PathVariableBuilder::build)
                        ?: emptyList()
                val headerParams = operation.params?.filter { it.type == ParamType.Header }?.map(HeaderBuilder::build)
                        ?: emptyList()
                val requestBodyParams = operation.request?.let { listOf(RequestBodyBuilder.build(it.type, modelMapping)) }
                        ?: emptyList()

                val serviceCallStatement = serviceCallStatement(operation, queryParams, pathParams, requestBodyParams)

                val httpRequestParam = ParameterSpec.builder("httpRequest",
                        ClassName("io.micronaut.http", "HttpRequest").parameterizedBy(Any::class.asClassName()))
                        .build()
                val responseType = operation.response?.firstOrNull()?.let { ClassName("io.micronaut.http", "HttpResponse").parameterizedBy(it.toKotlinPoetType(modelMapping)) }
                FunSpec.builder(operation.name)
                        .addAnnotation(operationTypeAnnotation(operation, path))
                        .also { b -> operation.request?.consumes?.let { consumes -> b.addAnnotation(operationContentTypeAnnotation(consumes)) } }
                        .addParameters(queryParams + pathParams + headerParams + requestBodyParams)
                        .addParameter(httpRequestParam)
                        .also { responseType?.let { res -> it.returns(res) } }
                        .addStatement(if (securityDependencies.isNotEmpty()) "return basicauthorizer.authorize(httpRequest.headers.authorization){$serviceCallStatement}" else "return $serviceCallStatement")
                        .build()
            } ?: emptyList()
        }
    }

    private fun operationTypeAnnotation(operation: Operation, path: Path) =
            AnnotationSpec.builder(ClassName("io.micronaut.http.annotation", toTitleCase(operation.type.toString())))
                    .addMember("value = %S", path.url)
                    .build()

    private fun operationContentTypeAnnotation(consumes: List<String>) =
            AnnotationSpec.builder(ClassName("io.micronaut.http.annotation", "Consumes"))
                    .also { ab -> consumes.forEach { ab.addMember("%S", it) } }
                    .build()

    private fun serviceCallStatement(operation: Operation, queryParams: List<ParameterSpec>, pathParams: List<ParameterSpec>, requestBodyParams: List<ParameterSpec>): String {
        val queryParamNames = queryParams.map { it.name }
        val pathParamNames = pathParams.map { it.name }
        val requestParamNames = operation.request?.let { req ->
            if (req.consumes?.contains("multipart/form-data") == true) "java.io.File.createTempFile(body.filename, \"\").also { it.writeBytes(body.bytes) }" else requestBodyParams.joinToString { it.name }
        }?.let { listOf(it) } ?: emptyList()
        return "HttpResponse.ok(service.${operation.name}(${(queryParamNames + pathParamNames + requestParamNames).joinToString()}))"
    }

}
