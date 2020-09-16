package apifi.codegen

import apifi.parser.models.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.swagger.v3.oas.models.PathItem

class ApiMethodBuilderTest : StringSpec({

    val securityProvider = mockk<SecurityProvider>()
    val apiMethodBuilder = ApiMethodBuilder(testModelMapping(), securityProvider)
    beforeTest {
        every { securityProvider.shouldAuthenticate(any()) } returns false
    }

    afterTest {
        clearAllMocks()
    }

    "generate api method based on spec operation method and url" {
        val operation1 = Operation(PathItem.HttpMethod.GET, "getOpName", emptyTags(), emptyParams(), null, emptyResponses(), SecurityDefinitionType.BASIC_AUTH)
        val operation2 = Operation(PathItem.HttpMethod.POST, "postOpName", emptyTags(), emptyParams(), null, emptyResponses(), SecurityDefinitionType.BASIC_AUTH)
        val operation3 = Operation(PathItem.HttpMethod.GET, "getPet", emptyTags(), emptyParams(), null, emptyResponses(), SecurityDefinitionType.BASIC_AUTH)

        forAll(
            row(apiMethodBuilder.methodFor("/pets", operation1), "@io.micronaut.http.annotation.Get(value = \"/pets\")\n" +
                "fun getOpName() = HttpResponse.ok(controller.getOpName())\n"),
            row(apiMethodBuilder.methodFor("/pets", operation2), "@io.micronaut.http.annotation.Post(value = \"/pets\")\n" +
                "fun postOpName() = HttpResponse.ok(controller.postOpName())\n"),
            row(apiMethodBuilder.methodFor("/pets/{petId}", operation3), "@io.micronaut.http.annotation.Get(value = \"/pets/{petId}\")\n" +
                "fun getPet() = HttpResponse.ok(controller.getPet())\n")
        ) { actualSpec, expectedToString ->
            actualSpec.toString() shouldBe expectedToString
        }
    }

    "generate api method with query, path and header params" {
        val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
        val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
        val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
        val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyTags(), listOf(queryParam, pathParam, headerParam), null, emptyResponses())

        val api = apiMethodBuilder.methodFor("/pets", operation)

        api.parameters.map { it.toString() } shouldContainExactlyInAnyOrder
            listOf("@io.micronaut.http.annotation.QueryValue limit: kotlin.Int",
                "@io.micronaut.http.annotation.PathVariable petId: kotlin.Int",
                "@io.micronaut.http.annotation.Header(value = \"x-header\") xHeader: kotlin.String")
    }

    "generate api method with request and response" {
        val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyTags(), emptyParams(), Request("Pet", listOf("application/json", "text/plain")), listOf(Response("200", "PetResponse")))

        val api = apiMethodBuilder.methodFor("/pets", operation)

        api.annotations[0].toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")"
        api.annotations[1].toString() shouldBe "@io.micronaut.http.annotation.Consumes(\"application/json\", \"text/plain\")"
        api.parameters.map { it.toString() } shouldContainExactlyInAnyOrder listOf("@io.micronaut.http.annotation.Body body: models.Pet")
        api.returnType.toString() shouldBe "io.micronaut.http.HttpResponse<models.PetResponse>"
    }

    "should add @throws annotation for all non 200 responses returned from an operation" {
        val request = Request("Pet", listOf("application/json", "text/plain"))
        val responses = listOf(Response("200", "PetResponse"), Response("400", "kotlin.String"), Response("403", "kotlin.String"))
        val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyTags(), emptyParams(), request, responses)

        val api = apiMethodBuilder.methodFor("/pets", operation)

        api.annotations[0].toString() shouldBe "@kotlin.jvm.Throws(apifi.micronaut.exceptions.BadRequestException::class)"
        api.annotations[1].toString() shouldBe "@kotlin.jvm.Throws(apifi.micronaut.exceptions.ForbiddenException::class)"
    }

    "should add @throws for InternalServerException if no specific exception for an http status is found" {
        val request = Request("Pet", listOf("application/json", "text/plain"))
        val responses = listOf(Response("200", "PetResponse"), Response("301", "kotlin.String"))
        val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyTags(), emptyParams(), request, responses)

        val api = apiMethodBuilder.methodFor("/pets", operation)

        api.annotations.map { it.toString() } shouldContain "@kotlin.jvm.Throws(apifi.micronaut.exceptions.InternalServerErrorException::class)"
    }

    "should add security parameters if required" {
        val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyTags(), emptyParams(), Request("Pet"), emptyResponses())
        every { securityProvider.shouldAuthenticate(operation) } returns true
        val api = apiMethodBuilder.methodFor("/pets", operation)

        api.toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")\n" +
            "fun createPet(@io.micronaut.http.annotation.Body body: models.Pet, authentication: io.micronaut.security.authentication.Authentication?) = HttpResponse.ok(controller.createPet(body, authentication))\n"
    }

})