package apifi

import apifi.codegen.CodeGenerator
import apifi.parser.SpecFileParser
import io.swagger.v3.parser.OpenAPIV3Parser
import java.io.File

fun main(args: Array<String>) {
    val specFile = args[0]
    val outputDir = File(args[1])
    outputDir.mkdirs()
    val basePackageName = args[2]

    if (!File(specFile).isFile || !outputDir.isDirectory) {
        println("invalid spec file or output directory")
    } else {
        val openApi = OpenAPIV3Parser().read(specFile)
        val spec = SpecFileParser.parse(openApi)
        CodeGenerator.generate(spec, basePackageName).forEach {
            val outFileParentDir = File(outputDir, it.location)
            outFileParentDir.mkdirs()
            File(outFileParentDir, it.name).writeText(it.content)
        }
    }
}
