package apifi.parser

import apifi.parser.ModelParser.parseReference
import io.swagger.v3.oas.models.responses.ApiResponses

object ResponseBodyParser {
    fun parse(responses: ApiResponses?): List<String>? {
        val responseSchemas = responses?.mapNotNull { r ->
            r.value.content?.entries?.map { v -> (r.key to v.value.schema) }
        }?.flatten()
        return responseSchemas?.map { parseReference(it.second) }
    }

}