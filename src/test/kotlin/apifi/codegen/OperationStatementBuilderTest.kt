package apifi.codegen

import apifi.parser.models.*
import com.squareup.kotlinpoet.TypeSpec
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.swagger.v3.oas.models.PathItem

class OperationStatementBuilderTest : StringSpec() {
	init {
		"should generate statement with non exhaustive statuses and default" {
			val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), emptyList(), Request("Pet", listOf("application/json", "text/plain")), listOf(
					Response("200", "PetResponse"),
					Response("400", "BadRequestError"),
					Response("401", "UnauthorizedError"),
					Response("default", "Pet")
			))
			val statement = OperationStatementBuilder.build(operation, modelMapping())
			statement.trimIndent() shouldBe """val response = service.createPet(body)
			return when(response.status) {
				models.ResponseType.SUCCESS -> HttpResponse.ok(response.body as models.PetResponse)
				models.ResponseType.BAD_REQUEST -> HttpResponse.badRequest(response.body as models.Error)
				models.ResponseType.FORBIDDEN -> HttpResponse.unauthorized(response.body as String)
				else -> HttpResponse.ok(response.body as models.Pet)
			}""".trimIndent()
		}

		"should generate statement with non exhaustive statuses without default" {
			val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), emptyList(), Request("Pet", listOf("application/json", "text/plain")), listOf(
					Response("200", "PetResponse"),
					Response("400", "BadRequestError"),
					Response("401", "UnauthorizedError")
			))
			val statement = OperationStatementBuilder.build(operation, modelMapping())
			statement.trimIndent() shouldBe """val response = service.createPet(body)
			return when(response.status) {
				models.ResponseType.SUCCESS -> HttpResponse.ok(response.body as models.PetResponse)
				models.ResponseType.BAD_REQUEST -> HttpResponse.badRequest(response.body as models.Error)
				models.ResponseType.FORBIDDEN -> HttpResponse.unauthorized(response.body as String)
				else -> HttpResponse.ok(response.body)
			}""".trimIndent()
		}

		"should generate statement with exhaustive statuses without default" {
			val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), emptyList(), Request("Pet", listOf("application/json", "text/plain")), listOf(
					Response("200", "PetResponse"),
					Response("400", "BadRequestError"),
					Response("401", "UnauthorizedError"),
					Response("404", "NotFoundError")
			))
			val statement = OperationStatementBuilder.build(operation, modelMapping())
			statement.trimIndent() shouldBe """val response = service.createPet(body)
			return when(response.status) {
				models.ResponseType.SUCCESS -> HttpResponse.ok(response.body as models.PetResponse)
				models.ResponseType.BAD_REQUEST -> HttpResponse.badRequest(response.body as models.Error)
				models.ResponseType.FORBIDDEN -> HttpResponse.unauthorized(response.body as String)
				models.ResponseType.NOT_FOUND -> HttpResponse.notFound(response.body as models.Error)
			}""".trimIndent()
		}

		"should generate statement with only one status code" {
			val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), emptyList(), Request("Pet", listOf("application/json", "text/plain")), listOf(
					Response("200", "PetResponse")
			))
			val statement = OperationStatementBuilder.build(operation, modelMapping())
			statement.trimIndent() shouldBe """val response = service.createPet(body)
			return HttpResponse.ok(response.body)""".trimIndent()
		}

		"should convert multipart file to java file" {
			val operation = Operation(PathItem.HttpMethod.POST, "uploadDocument", emptyList(), emptyList(), Request("io.micronaut.http.multipart.CompleteFileUpload", listOf("multipart/form-data")), null)
			OperationStatementBuilder.build(operation, modelMapping()).trimIndent() shouldBe "service.uploadDocument(java.io.File.createTempFile(body.filename, \"\").also { it.writeBytes(body.bytes) })"
		}
	}

	private fun modelMapping() = mapOf(
			"Pet" to "models.Pet",
			"PetResponse" to "models.PetResponse",
			"ResponseType" to "models.ResponseType",
			"BadRequestError" to "models.Error",
			"NotFoundError" to "models.Error",
			"UnauthorizedError" to "String"
	)
}