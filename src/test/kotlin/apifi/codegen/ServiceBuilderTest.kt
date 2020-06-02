package apifi.codegen

import apifi.parser.models.*
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.oas.models.PathItem

class ServiceBuilderTest : DescribeSpec({

    describe("Service Builder") {
        it("should generate service class with operations") {
            val path1 = Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "getOpName", emptyList(), null, null, null, SecurityDefinitionType.BASIC_AUTH),
                    Operation(PathItem.HttpMethod.POST, "postOpName", emptyList(), null, null, null, SecurityDefinitionType.BASIC_AUTH)
            ))
            val path2 = Path("/pets/{petId}", listOf(
                    Operation(PathItem.HttpMethod.GET, "getPet", emptyList(), null, null, null, SecurityDefinitionType.BASIC_AUTH)
            ))
            val serviceClass = ServiceBuilder.build(listOf(path1, path2), "Pets")
            serviceClass.name shouldBe "PetsService"
            serviceClass.funSpecs.size shouldBe 3
            serviceClass.toString().replace("\n", "") shouldBe "interface PetsService {  fun getOpName()  fun postOpName()  fun getPet()}"
        }

        it("should generate service class methods with methods including params") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)

            val path = Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "opName", emptyList(), listOf(queryParam, pathParam, headerParam), Request("Pet", listOf("application/json")), listOf("PetResponse"), SecurityDefinitionType.BASIC_AUTH)
            ))
            val serviceClass = ServiceBuilder.build(listOf(path), "Pets")

            serviceClass.funSpecs.size shouldBe 1
            serviceClass.funSpecs[0].toString().replace("\n", "") shouldBe "abstract fun opName(  limit: kotlin.Int,  petId: kotlin.Int,  body: Pet): PetResponse"
        }

        it("should generate service class method for multipart content type") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)

            val path = Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "opName", emptyList(), listOf(queryParam, pathParam, headerParam), Request("io.micronaut.http.multipart.CompleteFileUpload", listOf("multipart/form-data")), listOf("PetResponse"), SecurityDefinitionType.BASIC_AUTH)
            ))
            val serviceClass = ServiceBuilder.build(listOf(path), "Pets")

            serviceClass.funSpecs.size shouldBe 1
            serviceClass.funSpecs[0].toString().replace("\n", "") shouldBe "abstract fun opName(  limit: kotlin.Int,  petId: kotlin.Int,  body: java.io.File): PetResponse"
        }
    }

})