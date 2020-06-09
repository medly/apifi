package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.parser.models.Operation
import apifi.parser.models.Response

object OperationStatementBuilder {
	fun build(operation: Operation, modelMapping: Map<String, String>): String {
		val queryParamNames = operation.queryParams().map { it.name }
		val pathParamNames = operation.pathParams().map { it.name }
		val requestBodyParams = operation.requestParams(modelMapping)
		val requestParamNames = operation.request?.let { req ->
			if (req.consumes?.contains("multipart/form-data") == true) "java.io.File.createTempFile(body.filename, \"\").also { it.writeBytes(body.bytes) }" else requestBodyParams.joinToString { it.name }
		}?.let { listOf(it) } ?: emptyList()
		val serviceCall = "service.${operation.name}(${(queryParamNames + pathParamNames + requestParamNames).joinToString()})"
		return operation.response?.let { res ->
			"""val response = $serviceCall
			${if (res.size == 1) "return HttpResponse.ok(response.body)"
		else """return when(response.status) {
				${res.joinToString("\n\t\t\t\t") {
			val responseTypeClass = modelMapping["ResponseType"]
			val responseType = it.type.toKotlinPoetType(modelMapping)
			when (it.statusCode) {
				"200" -> "$responseTypeClass.SUCCESS -> HttpResponse.ok(response.body as $responseType)"
				"400" -> "$responseTypeClass.BAD_REQUEST -> HttpResponse.badRequest(response.body as $responseType)"
				"401" -> "$responseTypeClass.FORBIDDEN -> HttpResponse.unauthorized(response.body as $responseType)"
				"404" -> "$responseTypeClass.NOT_FOUND -> HttpResponse.notFound(response.body as $responseType)"
				"default" -> "else -> HttpResponse.ok(response.body as $responseType)"
				else -> ""
			}
		}}${if (hasAllStatusCodes(operation.response)) "" else "\n\t\t\t\telse -> HttpResponse.ok(response.body)"}
			}"""}"""} ?: serviceCall
	}

	private fun hasAllStatusCodes(response: List<Response>) =
			response.any { it.statusCode == "default" } || (listOf("200", "400", "401", "404") - response.map { it.statusCode }).isEmpty()
}
