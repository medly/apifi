package apifi.codegen

import apifi.parser.models.*
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.DescribeSpec
import io.swagger.v3.oas.models.PathItem

class ControllerInterfaceBuilderTest : DescribeSpec({

    describe("Controller Interface Builder") {
        it("should generate controller interface with operations") {
            val path1 = Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "getOpName", emptyTags(), emptyParams(), null, emptyResponses(), SecurityDefinitionType.BASIC_AUTH),
                    Operation(PathItem.HttpMethod.POST, "postOpName", emptyTags(), emptyParams(), null, emptyResponses(), SecurityDefinitionType.BASIC_AUTH)
            ))
            val path2 = Path("/pets/{petId}", listOf(
                    Operation(PathItem.HttpMethod.GET, "getPet", emptyTags(), emptyParams(), null, emptyResponses(), SecurityDefinitionType.BASIC_AUTH)
            ))
            val controllerInterface = ControllerInterfaceBuilder.build(listOf(path1, path2), "Pets")
            controllerInterface.name shouldBe "PetsController"
            controllerInterface.funSpecs.size shouldBe 3
            controllerInterface.toString().replace("\n", "") shouldBe "interface PetsController {  fun getOpName()  fun postOpName()  fun getPet()}"
        }

        it("should generate controller interface methods with methods including params") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("pet-id", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)

            val path = Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "opName", emptyTags(), listOf(queryParam, pathParam, headerParam), Request("Pet", listOf("application/json")), listOf(Response("200", "PetResponse")), SecurityDefinitionType.BASIC_AUTH)
            ))
            val controllerInterface = ControllerInterfaceBuilder.build(listOf(path), "Pets")

            controllerInterface.funSpecs.size shouldBe 1
            controllerInterface.funSpecs[0].toString().replace("\n", "") shouldBe "abstract fun opName(  limit: kotlin.Int,  petId: kotlin.Int,  body: Pet): PetResponse"
        }

        it("should generate controller interface method for multipart content type") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)

            val path = Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "opName", emptyTags(), listOf(queryParam, pathParam, headerParam), Request(micronautMultipartFileUploadPackage, listOf("multipart/form-data")), listOf(Response("200", "PetResponse")), SecurityDefinitionType.BASIC_AUTH)
            ))
            val controllerInterface = ControllerInterfaceBuilder.build(listOf(path), "Pets")

            controllerInterface.funSpecs.size shouldBe 1
            controllerInterface.funSpecs[0].toString().replace("\n", "") shouldBe "abstract fun opName(  limit: kotlin.Int,  petId: kotlin.Int,  body: java.io.File): PetResponse"
        }
    }

})