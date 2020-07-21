package apifi.parser

import apifi.parser.ModelParser.parseReference
import apifi.parser.models.ParseResult
import apifi.parser.models.Response
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponses

object ResponseBodyParser {
	fun parse(responses: ApiResponses?, operationSpecifier: String): ParseResult<List<Response>>? {
		return responses?.mapValues { r -> r.value.content?.values!!.first().schema }?.map {
			if (ModelParser.shouldCreateModel(it.value))
				ModelParser.modelsFromSchema<Any?>(responseModelName(operationSpecifier), it.value)
						.let { m -> Response(it.key, responseBodyType(m.first().name, it.value)) to m }
			else Response(it.key, parseReference(it.value)) to emptyList()
		}?.let { ParseResult(it.map { p -> p.first }, it.flatMap { p -> p.second }) }
	}

	private fun responseBodyType(modelName: String, schema: Schema<Any>) = if (schema is ArraySchema) "kotlin.Array<$modelName>" else modelName

	private fun responseModelName(operationSpecifier: String) = "${operationSpecifier.capitalize()}Response"

}