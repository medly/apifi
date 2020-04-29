package apifi.parser

import apifi.parser.models.CommonSpec
import apifi.parser.models.SecurityDefinition
import apifi.parser.models.SecurityDefinitionType
import io.swagger.v3.oas.models.OpenAPI

object CommonSpecFileParser {
        fun parse(openApiSpec: OpenAPI): CommonSpec {
            val models = openApiSpec.components?.schemas?.map { (name, schema) -> ModelParser.modelFromSchema(name, schema) } ?: emptyList()
            val securitySchemes = openApiSpec.components?.securitySchemes?.entries?.map { scheme -> SecurityDefinition(scheme.key, SecurityDefinitionType.fromTypeAndScheme(scheme.value.type, scheme.value.scheme)) } ?: emptyList()
            return CommonSpec(models, securitySchemes)
        }
}