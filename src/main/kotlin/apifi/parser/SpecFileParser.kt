package apifi.parser

import apifi.parser.models.Spec
import io.swagger.v3.oas.models.OpenAPI

object SpecFileParser {
        fun parse(openApiSpec: OpenAPI): Spec {
            val paths = PathsParser.parse(openApiSpec.paths)
            val models = openApiSpec.components?.schemas?.map { (name, schema) -> ModelParser.modelFromSchema(name, schema) } ?: emptyList()
            val securityRequirements = openApiSpec.security?.flatMap { it.keys } ?: emptyList()
            return Spec(paths, models, securityRequirements)
        }
}