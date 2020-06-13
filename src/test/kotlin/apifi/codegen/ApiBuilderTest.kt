package apifi.codegen

import apifi.parser.models.*
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
            val api = ApiBuilder.build("pets", listOf(path), emptyList(), "apifi.gen", modelMapping())
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
            val api = ApiBuilder.build("pets", listOf(path1, path2), emptyList(), "apifi.gen", modelMapping())
            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs.size shouldBe 3
            apiClass.funSpecs[0].toString() shouldBe "@io.micronaut.http.annotation.Get(value = \"/pets\")\n" +
                    "fun getOpName(httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>) = HttpResponse.ok(controller.getOpName())\n"
            apiClass.funSpecs[1].toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")\n" +
                    "fun postOpName(httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>) = HttpResponse.ok(controller.postOpName())\n"
            apiClass.funSpecs[2].toString() shouldBe "@io.micronaut.http.annotation.Get(value = \"/pets/{petId}\")\n" +
                    "fun getPet(httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>) = HttpResponse.ok(controller.getPet())\n"
        }
        it("generate api method with query, path and header params") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), listOf(queryParam, pathParam, headerParam), null, null)

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), emptyList(), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].parameters.map { it.toString() } shouldContainExactlyInAnyOrder
                    listOf("@io.micronaut.http.annotation.QueryValue limit: kotlin.Int",
                            "@io.micronaut.http.annotation.PathVariable petId: kotlin.Int",
                            "@io.micronaut.http.annotation.Header(value = \"x-header\") xHeader: kotlin.String",
                            "httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>")
        }

        it("generate api method with request and response") {
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), emptyList(), Request("Pet", listOf("application/json", "text/plain")), listOf("PetResponse"))

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), emptyList(), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].annotations[0].toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")"
            apiClass.funSpecs[0].annotations[1].toString() shouldBe "@io.micronaut.http.annotation.Consumes(\"application/json\", \"text/plain\")"
            apiClass.funSpecs[0].parameters.map { it.toString() } shouldContainExactlyInAnyOrder
                    listOf("@io.micronaut.http.annotation.Body body: models.Pet",
                            "httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>")
            apiClass.funSpecs[0].returnType.toString() shouldBe "io.micronaut.http.HttpResponse<models.PetResponse>"
        }

        it("generate api method block with all blocks when security dependencies are present") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "listPets", emptyList(), listOf(queryParam, pathParam, headerParam), Request("Pet", listOf("application/json")), listOf("PetResponse"))

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), listOf(SecurityDependency("httpBasic", "security", SecurityDefinitionType.BASIC_AUTH)), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].body.toString().trimIndent() shouldBe "return basicauthorizer.authorize(httpRequest.headers.authorization){HttpResponse.ok(controller.listPets(limit, petId, body))}"
        }

        it("generate api method block with all blocks when security dependencies are not present") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), listOf(queryParam, pathParam, headerParam), Request("Pet", null), listOf("PetResponse"))

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), emptyList(), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].body.toString().trimIndent() shouldBe "return HttpResponse.ok(controller.createPet(limit, petId, body))"
        }

        it("inject controller & security dependencies") {
            val operation = Operation(PathItem.HttpMethod.POST, "listPets", listOf(), emptyList(), Request("Pet", null), listOf("PetResponse"))

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), listOf(SecurityDependency("BasicAuthorizer", "security", SecurityDefinitionType.BASIC_AUTH)), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            val controllerClass = api.members[1] as TypeSpec
            apiClass.name shouldBe "PetsApi"
            controllerClass.name shouldBe "PetsController"

            apiClass.propertySpecs[0].name shouldBe "controller"
            apiClass.propertySpecs[0].type.toString() shouldBe "apifi.gen.PetsController"
            apiClass.propertySpecs[0].modifiers shouldContain KModifier.PRIVATE

            apiClass.toString() shouldContain "@io.micronaut.http.annotation.Controller\n" +
                    "class PetsApi @javax.inject.Inject constructor(\n" +
                    "  private val controller: apifi.gen.PetsController,\n" +
                    "  private val basicauthorizer: security.BasicAuthorizer\n" +
                    ")"
        }

        it("should convert multipart file to java file") {
            val operation = Operation(PathItem.HttpMethod.POST, "uploadDocument", emptyList(), emptyList(), Request("io.micronaut.http.multipart.CompleteFileUpload", listOf("multipart/form-data")), null)

            val api = ApiBuilder.build("pets", listOf(Path("/pets", listOf(operation))), listOf(SecurityDependency("BasicAuthorizer", "security", SecurityDefinitionType.BASIC_AUTH)), "apifi.gen", modelMapping())

            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs[0].body.toString().trimIndent() shouldBe "return basicauthorizer.authorize(httpRequest.headers.authorization){HttpResponse.ok(controller.uploadDocument(java.io.File.createTempFile(body.filename, \"\").also { it.writeBytes(body.bytes) }))}"
        }
    }


})

fun modelMapping() = listOf("Pet" to "models.Pet", "PetResponse" to "models.PetResponse")
