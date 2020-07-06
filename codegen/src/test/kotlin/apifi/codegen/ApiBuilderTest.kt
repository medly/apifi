package apifi.codegen

import apifi.models.*
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.oas.models.PathItem

class ApiBuilderTest : DescribeSpec({

    describe("Api Builder") {
        it("generate api class with controller annotation") {
            val path = Path("/pets", listOf(Operation(PathItem.HttpMethod.GET, "listPets", emptyList(), null, null, null)))
            val api = ApiBuilder.build("pets", listOf(path), "apifi.gen", modelMapping())
            val apiClass = api.members[0] as TypeSpec
            apiClass.name shouldBe "PetsApi"
            apiClass.annotationSpecs[0].toString() shouldBe "@io.micronaut.http.annotation.Controller"
        }

        it("generate api method based on spec operation method and url") {
            val path1 = Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "getOpName", emptyList(), null, null, null, SecurityDefinitionType.BASIC_AUTH),
                    Operation(PathItem.HttpMethod.POST, "postOpName", emptyList(), null, null, null, SecurityDefinitionType.BASIC_AUTH)
            ))
            val path2 = Path("/pets/{petId}", listOf(
                    Operation(PathItem.HttpMethod.GET, "getPet", emptyList(), null, null, null, SecurityDefinitionType.BASIC_AUTH)
            ))
            val api = ApiBuilder.build("pets", listOf(path1, path2), "apifi.gen", modelMapping())
            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs.size shouldBe 3
            apiClass.funSpecs[0].toString() shouldBe "@io.micronaut.http.annotation.Get(value = \"/pets\")\n" +
                    "fun getOpName() = HttpResponse.ok(controller.getOpName())\n"
            apiClass.funSpecs[1].toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")\n" +
                    "fun postOpName() = HttpResponse.ok(controller.postOpName())\n"
            apiClass.funSpecs[2].toString() shouldBe "@io.micronaut.http.annotation.Get(value = \"/pets/{petId}\")\n" +
                    "fun getPet() = HttpResponse.ok(controller.getPet())\n"
        }

        it("generate api method with query, path and header params") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), listOf(queryParam, pathParam, headerParam), null, null)

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].parameters.map { it.toString() } shouldContainExactlyInAnyOrder
                    listOf("@io.micronaut.http.annotation.QueryValue limit: kotlin.Int",
                            "@io.micronaut.http.annotation.PathVariable petId: kotlin.Int",
                            "@io.micronaut.http.annotation.Header(value = \"x-header\") xHeader: kotlin.String")
        }

        it("generate api method with request and response") {
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), emptyList(), Request("Pet", listOf("application/json", "text/plain")), listOf(Response("200", "PetResponse")))

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].annotations[0].toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")"
            apiClass.funSpecs[0].annotations[1].toString() shouldBe "@io.micronaut.http.annotation.Consumes(\"application/json\", \"text/plain\")"
            apiClass.funSpecs[0].parameters.map { it.toString() } shouldContainExactlyInAnyOrder listOf("@io.micronaut.http.annotation.Body body: models.Pet")
            apiClass.funSpecs[0].returnType.toString() shouldBe "io.micronaut.http.HttpResponse<models.PetResponse>"
        }

        it("generate api method block with all blocks when security dependencies are present") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "listPets", emptyList(), listOf(queryParam, pathParam, headerParam), Request("Pet", listOf("application/json")), listOf(Response("200", "PetResponse")))

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].body.toString().trimIndent() shouldBe "return HttpResponse.ok(controller.listPets(limit, petId, body))"
        }

        it("generate api method block with all blocks when security dependencies are not present") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), listOf(queryParam, pathParam, headerParam), Request("Pet", null), listOf(Response("200", "PetResponse")))

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].body.toString().trimIndent() shouldBe "return HttpResponse.ok(controller.createPet(limit, petId, body))"
        }

        it("inject controller") {
            val operation = Operation(PathItem.HttpMethod.POST, "listPets", listOf(), emptyList(), Request("Pet", null), listOf(Response("200", "PetResponse")))

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            val controllerClass = api.members[1] as TypeSpec
            apiClass.name shouldBe "PetsApi"
            controllerClass.name shouldBe "PetsController"

            apiClass.propertySpecs[0].name shouldBe "controller"
            apiClass.propertySpecs[0].type.toString() shouldBe "apifi.gen.PetsController"
            apiClass.propertySpecs[0].modifiers shouldContain KModifier.PRIVATE

            apiClass.toString() shouldContain "@io.micronaut.http.annotation.Controller\n" +
                    "class PetsApi @javax.inject.Inject constructor(\n" +
                    "  private val controller: apifi.gen.PetsController\n" +
                    ")"
        }

        it("should add @throws annotation for all non 200 responses returned from an operation") {
            val request = Request("Pet", listOf("application/json", "text/plain"))
            val responses = listOf(Response("200", "PetResponse"), Response("400", "kotlin.String"), Response("403", "kotlin.String"))
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), emptyList(), request, responses)

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].annotations[0].toString() shouldBe "@kotlin.jvm.Throws(apifi.gen.exceptions.BadRequestException::class)"
            apiClass.funSpecs[0].annotations[1].toString() shouldBe "@kotlin.jvm.Throws(apifi.gen.exceptions.ForbiddenException::class)"
        }

        it("should add @throws for InternalServerException if no specific exception for an http status is found") {
            val request = Request("Pet", listOf("application/json", "text/plain"))
            val responses = listOf(Response("200", "PetResponse"), Response("301", "kotlin.String"))
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), emptyList(), request, responses)

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].annotations[0].toString() shouldBe "@kotlin.jvm.Throws(apifi.gen.exceptions.InternalServerErrorException::class)"
        }
    }


})

fun modelMapping() = mapOf("Pet" to "models.Pet", "PetResponse" to "models.PetResponse")
