package apifi.parser

import apifi.parser.ModelParser.parseReference
import apifi.parser.ModelParser.shouldCreateModel
import apifi.parser.models.ParseResult
import apifi.parser.models.Request
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody

object RequestBodyParser {
    fun parse(requestBody: RequestBody?, operationSpecifier: String): ParseResult<Request>? {
        val consumes = requestBody?.content?.keys?.toList() ?: emptyList()
        return if (consumes.contains("multipart/form-data")) {
            ParseResult(Request("io.micronaut.http.multipart.CompleteFileUpload", consumes), emptyList())
        } else requestBody?.content?.entries?.firstOrNull()?.value?.schema?.let {
            if (shouldCreateModel(it)) ModelParser.modelsFromSchema(requestModelName(operationSpecifier), it).let { m -> requestBodyType(m.first().name, it) to m }
            else parseReference(it) to emptyList()
        }?.let { ParseResult(Request(it.first, consumes), it.second) }
    }

    private fun requestBodyType(modelName: String, schema: Schema<Any>) = if (schema is ArraySchema) "kotlin.Array<$modelName>" else modelName

    private fun requestModelName(operationSpecifier: String) = "${operationSpecifier.capitalize()}Request"

}