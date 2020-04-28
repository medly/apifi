package apifi.parser

import apifi.parser.models.SecurityDefinition
import apifi.parser.models.SecurityDefinitionType
import apifi.parser.models.Spec
import io.swagger.v3.oas.models.OpenAPI

object SpecFileParser {
        fun parse(openApiSpec: OpenAPI): Spec {
            val paths = PathsParser.parse(openApiSpec.paths)
            val models = openApiSpec.components?.schemas?.map { (name, schema) -> ModelParser.modelFromSchema(name, schema) }
            val securitySchemes = openApiSpec.components?.securitySchemes?.entries?.map { scheme -> SecurityDefinition(scheme.key, SecurityDefinitionType.fromTypeAndScheme(scheme.value.type, scheme.value.scheme)) }
            return Spec(paths, models, securitySchemes)
        }
}