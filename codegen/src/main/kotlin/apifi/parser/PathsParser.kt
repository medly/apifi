package apifi.parser

import apifi.helpers.toCamelCase
import apifi.helpers.toCodeGenModel
import apifi.helpers.toTitleCase
import apifi.parser.models.*
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths

object PathsParser {
    fun parse(paths: Paths?): Pair<List<Path>, List<Model>> {
        val models = mutableListOf<Model>()
        return (paths?.map { (endpoint, config) ->
            val operations = config.readOperationsMap().map { (httpMethod, operation) ->
                val params = operation.parameters?.map { param ->
                    Param(param.name, param.schema.toCodeGenModel().dataType, param.required, ParamType.fromString(param.`in`))
                }
                val operationSpecifier = operationSpecifier(operation, httpMethod, endpoint)
                val request = RequestBodyParser.parse(operation.requestBody, operationSpecifier)
                val responses = ResponseBodyParser.parse(operation.responses, operationSpecifier)
                models.addAll(request?.second ?: emptyList())
                models.addAll(responses?.second ?: emptyList())
                Operation(httpMethod, operation.operationId ?: toCamelCase(httpMethod.toString()),
                        operation.tags, params, request?.first, responses?.first)
            }
            Path(endpoint, operations)
        } ?: emptyList()) to models
    }

    private fun operationSpecifier(operation: io.swagger.v3.oas.models.Operation, httpMethod: PathItem.HttpMethod, endpoint: String) =
            (operation.operationId
                    ?: toTitleCase(httpMethod.toString() + endpoint.replace(Regex("[^A-Za-z ]"), " ")))
}