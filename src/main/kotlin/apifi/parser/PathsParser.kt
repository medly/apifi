package apifi.parser

import apifi.helpers.toCamelCase
import apifi.helpers.toCodeGenModel
import apifi.parser.models.Operation
import apifi.parser.models.Param
import apifi.parser.models.ParamType
import apifi.parser.models.Path
import io.swagger.v3.oas.models.Paths

object PathsParser {
    fun parse(paths: Paths?): List<Path> =
        paths?.map { (endpoint, config) ->
            val operations = config.readOperationsMap().map { (httpMethod, operation) ->
                val params = operation.parameters?.map { param ->
                    Param(param.name, param.schema.toCodeGenModel().dataType, param.required, ParamType.fromString(param.`in`))
                }
                val consumes = operation.requestBody?.content?.keys?.toList()
                val requestModel = RequestBodyParser.parse(operation.requestBody)
                val responses = ResponseBodyParser.parse(operation.responses)
                Operation(httpMethod, operation.operationId ?: toCamelCase(httpMethod.toString()), params, requestModel, consumes, responses)
            }
            Path(endpoint, operations)
        } ?: emptyList()
}