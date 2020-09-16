package apifi.parser

import apifi.helpers.toCamelCase
import apifi.helpers.toTitleCase
import apifi.helpers.typeDeclaration
import apifi.models.Model
import apifi.parser.models.*
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths

object PathsParser {
    fun parse(paths: Paths?): ParseResult<List<Path>> {
        val models = mutableListOf<Model>()
        return ParseResult(paths?.map { (endpoint, config) ->
            val operations = config.readOperationsMap().map { (httpMethod, operation) ->
                val params = operation.parameters?.map { param ->
                    Param(param.name, param.schema.typeDeclaration(), param.required, ParamType.fromString(param.`in`))
                } ?: emptyList()
                val operationSpecifier = operationSpecifier(operation, httpMethod, endpoint)
                val request = RequestBodyParser.parse(operation.requestBody, operationSpecifier)
                val responses = ResponseBodyParser.parse(operation.responses, operationSpecifier)
                models.addAll(request?.models ?: emptyList())
                models.addAll(responses?.models ?: emptyList())
                Operation(httpMethod, operation.operationId ?: httpMethod.toString().toLowerCase(),
                    operation.tags ?: emptyList(), params, request?.result, responses?.result ?: emptyList())
            }
            Path(endpoint, operations)
        } ?: emptyList(), models)
    }

    private fun operationSpecifier(operation: io.swagger.v3.oas.models.Operation, httpMethod: PathItem.HttpMethod, endpoint: String) =
        (operation.operationId
            ?: (httpMethod.toString() + endpoint.replace(Regex("[^A-Za-z ]"), " ")).toTitleCase())
}