package apifi.parser

import apifi.models.SecurityDefinition
import apifi.models.SecurityDefinitionType
import apifi.models.Spec
import io.swagger.v3.oas.models.OpenAPI

object SpecFileParser {
    fun parse(openApiSpec: OpenAPI): Spec {
        val paths = PathsParser.parse(openApiSpec.paths)
        val models = (openApiSpec.components?.schemas?.flatMap { (name, schema) -> ModelParser.modelsFromSchema(name, schema) }
                ?: emptyList()) + paths.second
        val securitySchemes = openApiSpec.components?.securitySchemes?.entries?.map { scheme -> SecurityDefinition(scheme.key, SecurityDefinitionType.fromTypeAndScheme(scheme.value.type, scheme.value.scheme)) } ?: emptyList()
        val securityRequirements = openApiSpec.security?.flatMap { it.keys } ?: emptyList()
        return Spec(paths.first, models, securityRequirements, securitySchemes)
    }
}