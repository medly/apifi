package apifi.parser

import apifi.parser.ModelParser.parseReference
import apifi.parser.models.Model
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponses

object ResponseBodyParser {
    fun parse(responses: ApiResponses?, operationSpecifier: String): Pair<List<String>, List<Model>>? {
        return responses?.mapNotNull { r -> r.value.content?.entries?.map { v -> v.value.schema } }
                ?.flatten()
                ?.map {
                    if (ModelParser.shouldCreateModel(it)) ModelParser.modelsFromSchema(responseModelName(operationSpecifier), it).let { m -> responseBodyType(m.first().name, it) to m }
                    else parseReference(it) to emptyList()
                }?.let { it.map { p -> p.first } to it.flatMap { p -> p.second } }
    }

    private fun responseBodyType(modelName: String, schema: Schema<Any>) = if (schema is ArraySchema) "kotlin.Array<$modelName>" else modelName

    private fun responseModelName(operationSpecifier: String) = "${operationSpecifier.capitalize()}Response"

}