package apifi.codegen

import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

class ResponseModelBuilderTest : DescribeSpec({

  describe("Response Model Builder") {
      val fileSpec = ResponseModelBuilder.build("com.abc")

      it("should generate ResponseType enum") {
        fileSpec.members[0].toString().trimIndent() shouldBe "enum class ResponseType {\n" +
                "  SUCCESS,\n" +
                "\n" +
                "  BAD_REQUEST,\n" +
                "\n" +
                "  FORBIDDEN,\n" +
                "\n" +
                "  NOT_FOUND\n" +
                "}"

      }
      it("should generate Response<T> class that uses ResponseType enum") {
          fileSpec.members[1].toString().trimIndent() shouldBe "data class Response<T>(\n" +
                  "  status: com.abc.models.ResponseType,\n" +
                  "  body: T,\n" +
                  "  headers: kotlin.collections.Map<kotlin.CharSequence, kotlin.CharSequence>\n" +
                  ")"
      }
  }
})