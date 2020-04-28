package apifi.parser

import apifi.helpers.toCodeGenModel
import apifi.parser.models.*
import io.swagger.v3.oas.models.Paths

object PathsParser {
    fun parse(paths: Paths?): List<Path>? =
        paths?.map { (endpoint, config) ->
            val operations = config.readOperationsMap().map { (httpMethod, operation) ->
                val params = operation.parameters?.map { param ->
                    Param(param.name, param.schema.toCodeGenModel().dataType, param.required, ParamType.fromString(param.`in`))
                }
                val requestModel = RequestBodyParser.parse(operation.requestBody)
                val responses = ResponseBodyParser.parse(operation.responses)
                Operation(httpMethod, params, requestModel, responses)
            }
            Path(endpoint, operations)
        }
}