package apifi.parser

import apifi.parser.ModelParser.parseReference
import io.swagger.v3.oas.models.parameters.RequestBody

object RequestBodyParser {
    fun parse(requestBody: RequestBody?) =
        requestBody?.content?.entries?.firstOrNull()?.value?.schema?.let { parseReference(it) }

}