package apifi.parser

import apifi.parser.models.SecurityDefinition
import apifi.parser.models.SecurityDefinitionType
import apifi.parser.models.Spec
import io.swagger.v3.oas.models.OpenAPI

class OpenApiSpecReader {
    fun read(openApiSpec: OpenAPI): Spec {
        val paths = PathsParser.parse(openApiSpec.paths)
        val models = (openApiSpec.components?.schemas?.flatMap { (name, schema) -> ModelParser.modelsFromSchema(name, schema) }
                ?: emptyList()) + paths.models
        val securitySchemes = openApiSpec.components?.securitySchemes?.entries?.map { scheme -> SecurityDefinition(scheme.key, SecurityDefinitionType.fromTypeAndScheme(scheme.value.type, scheme.value.scheme)) } ?: emptyList()
        val securityRequirements = openApiSpec.security?.flatMap { it.keys } ?: emptyList()
        return Spec(paths.result, models, securityRequirements, securitySchemes)
    }
}
