package apifi

import apifi.codegen.CodeGenerator
import apifi.parser.SpecFileParser
import com.squareup.kotlinpoet.FileSpec
import io.swagger.v3.parser.OpenAPIV3Parser
import java.io.File

fun main(args: Array<String>) {
    val specFile = File(args[0])
    val outputDir = File(args[1])
    outputDir.mkdirs()
    val basePackageName = args[2]

    if (!specFile.isFile || !outputDir.isDirectory) {
        println("invalid spec file or output directory")
    } else {
        val openApi = OpenAPIV3Parser().read(specFile.absolutePath)
        val spec = SpecFileParser.parse(openApi, specFile.nameWithoutExtension)
        CodeGenerator.generate(spec, "$basePackageName.${specFile.nameWithoutExtension}").forEach {
            writeToFile(it, outputDir)
        }
    }
}

private fun writeToFile(fileSpec: FileSpec, outputDir: File) {
    val outFileParentDir = File(outputDir, fileSpec.packageName.replace(".", File.separator))
    outFileParentDir.mkdirs()
    File(outFileParentDir, fileSpec.name).writeText(fileSpec.toString())
}